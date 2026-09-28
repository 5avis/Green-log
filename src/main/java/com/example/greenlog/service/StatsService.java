package com.example.greenlog.service;

import com.example.greenlog.dto.SurvivalStatsResponse;
import com.example.greenlog.dto.SurvivalStatsResponse.DriveSurvivalStat;
import com.example.greenlog.dto.SurvivalStatsResponse.SpeciesSurvivalStat;
import com.example.greenlog.entity.PlantationDrive;
import com.example.greenlog.entity.Tree;
import com.example.greenlog.repository.CheckInRepository;
import com.example.greenlog.repository.PlantationDriveRepository;
import com.example.greenlog.repository.TreeRepository;
import com.example.greenlog.repository.VolunteerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class StatsService {

    private static final Logger log = LoggerFactory.getLogger(StatsService.class);

    private final TreeRepository treeRepository;
    private final PlantationDriveRepository driveRepository;
    private final VolunteerRepository volunteerRepository;
    private final CheckInRepository checkInRepository;

    @Autowired
    public StatsService(TreeRepository treeRepository,
                        PlantationDriveRepository driveRepository,
                        VolunteerRepository volunteerRepository,
                        CheckInRepository checkInRepository) {
        this.treeRepository = treeRepository;
        this.driveRepository = driveRepository;
        this.volunteerRepository = volunteerRepository;
        this.checkInRepository = checkInRepository;
    }

    public SurvivalStatsResponse calculateSurvivalStats() {
        List<Tree> allTrees = treeRepository.findAll();
        List<PlantationDrive> allDrives = driveRepository.findAll();
        long totalVolunteers = volunteerRepository.count();
        long totalCheckIns = checkInRepository.count();

        long totalTrees = allTrees.size();
        long aliveTrees = allTrees.stream()
                .filter(t -> !"DEAD".equalsIgnoreCase(t.getStatus()))
                .count();
        long deadTrees = totalTrees - aliveTrees;

        double overallSurvivalRate = totalTrees > 0
                ? Math.round((aliveTrees * 100.0 / totalTrees) * 10.0) / 10.0
                : 0.0;

        // Drive-level stats
        Map<Long, List<Tree>> treesByDrive = allTrees.stream()
                .filter(t -> t.getPlantationDrive() != null)
                .collect(Collectors.groupingBy(t -> t.getPlantationDrive().getId()));

        List<DriveSurvivalStat> driveStats = new ArrayList<>();
        for (PlantationDrive drive : allDrives) {
            List<Tree> driveTrees = treesByDrive.getOrDefault(drive.getId(), Collections.emptyList());
            long driveTotal = driveTrees.size();
            long driveAlive = driveTrees.stream()
                    .filter(t -> !"DEAD".equalsIgnoreCase(t.getStatus()))
                    .count();
            long driveDead = driveTotal - driveAlive;
            double rate = driveTotal > 0
                    ? Math.round((driveAlive * 100.0 / driveTotal) * 10.0) / 10.0
                    : 0.0;

            driveStats.add(DriveSurvivalStat.builder()
                    .driveId(drive.getId())
                    .driveName(drive.getName())
                    .location(drive.getLocation())
                    .totalTrees(driveTotal)
                    .aliveTrees(driveAlive)
                    .deadTrees(driveDead)
                    .survivalRate(rate)
                    .build());
        }

        // Species-level stats
        Map<String, List<Tree>> treesBySpecies = allTrees.stream()
                .collect(Collectors.groupingBy(t -> t.getSpecies() != null ? t.getSpecies().trim() : "Unknown"));

        List<SpeciesSurvivalStat> speciesStats = new ArrayList<>();
        for (Map.Entry<String, List<Tree>> entry : treesBySpecies.entrySet()) {
            List<Tree> speciesTrees = entry.getValue();
            long spTotal = speciesTrees.size();
            long spAlive = speciesTrees.stream()
                    .filter(t -> !"DEAD".equalsIgnoreCase(t.getStatus()))
                    .count();
            long spDead = spTotal - spAlive;
            double rate = spTotal > 0
                    ? Math.round((spAlive * 100.0 / spTotal) * 10.0) / 10.0
                    : 0.0;

            speciesStats.add(SpeciesSurvivalStat.builder()
                    .species(entry.getKey())
                    .totalTrees(spTotal)
                    .aliveTrees(spAlive)
                    .deadTrees(spDead)
                    .survivalRate(rate)
                    .build());
        }

        // Sort species alphabetically
        speciesStats.sort(Comparator.comparing(SpeciesSurvivalStat::getSpecies));

        return SurvivalStatsResponse.builder()
                .totalTrees(totalTrees)
                .aliveTrees(aliveTrees)
                .deadTrees(deadTrees)
                .overallSurvivalRate(overallSurvivalRate)
                .totalDrives((long) allDrives.size())
                .totalVolunteers(totalVolunteers)
                .totalCheckIns(totalCheckIns)
                .driveStats(driveStats)
                .speciesStats(speciesStats)
                .build();
    }

    /**
     * Auto-recalculation hook called whenever a check-in is logged.
     */
    public SurvivalStatsResponse recalculateSurvivalStats() {
        SurvivalStatsResponse stats = calculateSurvivalStats();
        log.info("Auto-recalculated survival stats: Overall Survival Rate = {}% (Alive: {} / Total: {})",
                stats.getOverallSurvivalRate(), stats.getAliveTrees(), stats.getTotalTrees());
        return stats;
    }

    /**
     * Purges all data across all 4 tables in foreign key order.
     */
    @Transactional
    public void purgeAllData() {
        checkInRepository.deleteAll();
        treeRepository.deleteAll();
        volunteerRepository.deleteAll();
        driveRepository.deleteAll();
        log.info("Purged all data across check_ins, trees, volunteers, and plantation_drives.");
    }
}
