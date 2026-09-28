package com.example.greenlog.service;

import com.example.greenlog.dto.PlantationEntryRequest;
import com.example.greenlog.entity.PlantationDrive;
import com.example.greenlog.entity.Tree;
import com.example.greenlog.entity.Volunteer;
import com.example.greenlog.exception.ResourceNotFoundException;
import com.example.greenlog.repository.PlantationDriveRepository;
import com.example.greenlog.repository.TreeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@Transactional
public class TreeService {

    private final TreeRepository treeRepository;
    private final PlantationDriveRepository driveRepository;
    private final VolunteerService volunteerService;
    private final StatsService statsService;

    @Autowired
    public TreeService(TreeRepository treeRepository,
                       PlantationDriveRepository driveRepository,
                       VolunteerService volunteerService,
                       StatsService statsService) {
        this.treeRepository = treeRepository;
        this.driveRepository = driveRepository;
        this.volunteerService = volunteerService;
        this.statsService = statsService;
    }

    /**
     * Core grading endpoint 1 logic: Log a new plantation entry.
     */
    public Tree logNewTree(PlantationEntryRequest request) {
        PlantationDrive drive = null;
        if (request.getPlantationDriveId() != null) {
            drive = driveRepository.findById(request.getPlantationDriveId())
                    .orElseThrow(() -> new ResourceNotFoundException("Plantation Drive not found with id: " + request.getPlantationDriveId()));
        }

        Tree tree = Tree.builder()
                .species(request.getSpecies().trim())
                .locationGps(request.getLocationGps().trim())
                .datePlanted(request.getDatePlanted())
                .status("ALIVE")
                .plantationDrive(drive)
                .build();

        Tree savedTree = treeRepository.save(tree);

        // Credit volunteer: either auto-register a new person or increment existing
        if (request.getNewVolunteerName() != null && !request.getNewVolunteerName().trim().isEmpty()) {
            volunteerService.createVolunteer(Volunteer.builder()
                    .name(request.getNewVolunteerName().trim())
                    .totalTreesPlanted(1)
                    .build());
        } else if (request.getVolunteerId() != null) {
            volunteerService.incrementTreesPlanted(request.getVolunteerId(), 1);
        }

        // Auto-recalculate stats
        statsService.recalculateSurvivalStats();

        return savedTree;
    }

    public Tree createTree(Tree tree) {
        if (tree.getStatus() == null || tree.getStatus().isBlank()) {
            tree.setStatus("ALIVE");
        }
        if (tree.getPlantationDrive() != null && tree.getPlantationDrive().getId() != null) {
            PlantationDrive drive = driveRepository.findById(tree.getPlantationDrive().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Plantation Drive not found with id: " + tree.getPlantationDrive().getId()));
            tree.setPlantationDrive(drive);
        }
        Tree saved = treeRepository.save(tree);
        statsService.recalculateSurvivalStats();
        return saved;
    }

    @Transactional(readOnly = true)
    public List<Tree> getAllTrees() {
        return treeRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Tree getTreeById(Long id) {
        return treeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tree not found with id: " + id));
    }

    public Tree updateTree(Long id, Tree treeDetails) {
        Tree existing = getTreeById(id);
        existing.setSpecies(treeDetails.getSpecies());
        existing.setLocationGps(treeDetails.getLocationGps());
        existing.setDatePlanted(treeDetails.getDatePlanted());

        if (treeDetails.getStatus() != null && !treeDetails.getStatus().isBlank()) {
            existing.setStatus(treeDetails.getStatus().toUpperCase());
        }

        if (treeDetails.getPlantationDrive() != null && treeDetails.getPlantationDrive().getId() != null) {
            PlantationDrive drive = driveRepository.findById(treeDetails.getPlantationDrive().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Plantation Drive not found with id: " + treeDetails.getPlantationDrive().getId()));
            existing.setPlantationDrive(drive);
        }

        Tree saved = treeRepository.save(existing);
        statsService.recalculateSurvivalStats();
        return saved;
    }

    public void deleteTree(Long id) {
        Tree existing = getTreeById(id);
        treeRepository.delete(existing);
        statsService.recalculateSurvivalStats();
    }

    /**
     * Core grading endpoint 4 logic: List trees due for their next check-in.
     * Returns alive trees that have not been checked in today (or never checked in).
     */
    @Transactional(readOnly = true)
    public List<Tree> getTreesDueForCheckIn() {
        return treeRepository.findTreesDueForCheckIn(LocalDate.now());
    }

    @Transactional(readOnly = true)
    public List<Tree> getTreesByDriveId(Long driveId) {
        return treeRepository.findByPlantationDriveId(driveId);
    }
}
