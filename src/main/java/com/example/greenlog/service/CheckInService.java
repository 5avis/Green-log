package com.example.greenlog.service;

import com.example.greenlog.dto.CheckInRequest;
import com.example.greenlog.entity.CheckIn;
import com.example.greenlog.entity.Tree;
import com.example.greenlog.entity.Volunteer;
import com.example.greenlog.exception.DeadTreeException;
import com.example.greenlog.exception.ResourceNotFoundException;
import com.example.greenlog.repository.CheckInRepository;
import com.example.greenlog.repository.TreeRepository;
import com.example.greenlog.repository.VolunteerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@Transactional
public class CheckInService {

    private final CheckInRepository checkInRepository;
    private final TreeRepository treeRepository;
    private final VolunteerRepository volunteerRepository;
    private final StatsService statsService;

    @Autowired
    public CheckInService(CheckInRepository checkInRepository,
                          TreeRepository treeRepository,
                          VolunteerRepository volunteerRepository,
                          StatsService statsService) {
        this.checkInRepository = checkInRepository;
        this.treeRepository = treeRepository;
        this.volunteerRepository = volunteerRepository;
        this.statsService = statsService;
    }

    /**
     * Core grading endpoint 2 logic + Strict Business Rules:
     * 1. "Dead Tree" Rule: A tree marked 'DEAD' in a check-in cannot receive further check-ins.
     *    The service layer MUST block this and throw a custom exception before hitting the DB.
     * 2. Auto-Updating Stats: Survival rate must be recalculated automatically whenever a new check-in is recorded.
     */
    public CheckIn submitCheckIn(CheckInRequest request) {
        if (request.getTreeId() == null) {
            throw new IllegalArgumentException("Tree ID is required for submitting a check-in.");
        }

        Tree tree = treeRepository.findById(request.getTreeId())
                .orElseThrow(() -> new ResourceNotFoundException("Tree not found with id: " + request.getTreeId()));

        // STRICT BUSINESS RULE 1: Dead Tree Rule
        if ("DEAD".equalsIgnoreCase(tree.getStatus())) {
            throw new DeadTreeException("A tree marked 'DEAD' cannot receive further check-ins. Tree ID #"
                    + tree.getId() + " (" + tree.getSpecies() + ") is already dead.");
        }

        String reportedStatus = request.getStatusReported() != null ? request.getStatusReported().trim().toUpperCase() : "ALIVE";
        if (!"ALIVE".equals(reportedStatus) && !"DEAD".equals(reportedStatus)) {
            throw new IllegalArgumentException("Status reported must be either 'ALIVE' or 'DEAD'. Received: " + request.getStatusReported());
        }

        Volunteer volunteer = null;
        if (request.getVolunteerId() != null) {
            volunteer = volunteerRepository.findById(request.getVolunteerId())
                    .orElseThrow(() -> new ResourceNotFoundException("Volunteer not found with id: " + request.getVolunteerId()));
        }

        LocalDate date = request.getCheckInDate() != null ? request.getCheckInDate() : LocalDate.now();

        CheckIn checkIn = CheckIn.builder()
                .checkInDate(date)
                .statusReported(reportedStatus)
                .tree(tree)
                .volunteer(volunteer)
                .build();

        // Update current status of the tree
        tree.setStatus(reportedStatus);
        treeRepository.save(tree);

        CheckIn savedCheckIn = checkInRepository.save(checkIn);

        // STRICT BUSINESS RULE 2: Recalculate survival stats automatically
        statsService.recalculateSurvivalStats();

        return savedCheckIn;
    }

    @Transactional(readOnly = true)
    public List<CheckIn> getAllCheckIns() {
        return checkInRepository.findAllByOrderByCheckInDateDescIdDesc();
    }

    @Transactional(readOnly = true)
    public List<CheckIn> getCheckInsByTreeId(Long treeId) {
        return checkInRepository.findByTreeIdOrderByCheckInDateDesc(treeId);
    }

    @Transactional(readOnly = true)
    public CheckIn getCheckInById(Long id) {
        return checkInRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("CheckIn not found with id: " + id));
    }

    public void deleteCheckIn(Long id) {
        CheckIn checkIn = getCheckInById(id);
        checkInRepository.delete(checkIn);
        statsService.recalculateSurvivalStats();
    }
}
