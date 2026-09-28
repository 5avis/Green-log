package com.example.greenlog.dto;

import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SurvivalStatsResponse {

    private long totalTrees;
    private long aliveTrees;
    private long deadTrees;
    private double overallSurvivalRate;
    private long totalDrives;
    private long totalVolunteers;
    private long totalCheckIns;

    @Builder.Default
    private List<DriveSurvivalStat> driveStats = new ArrayList<>();

    @Builder.Default
    private List<SpeciesSurvivalStat> speciesStats = new ArrayList<>();

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DriveSurvivalStat {
        private Long driveId;
        private String driveName;
        private String location;
        private long totalTrees;
        private long aliveTrees;
        private long deadTrees;
        private double survivalRate;

        public Long getDriveId() { return driveId; }
        public void setDriveId(Long driveId) { this.driveId = driveId; }
        public String getDriveName() { return driveName; }
        public void setDriveName(String driveName) { this.driveName = driveName; }
        public String getLocation() { return location; }
        public void setLocation(String location) { this.location = location; }
        public long getTotalTrees() { return totalTrees; }
        public void setTotalTrees(long totalTrees) { this.totalTrees = totalTrees; }
        public long getAliveTrees() { return aliveTrees; }
        public void setAliveTrees(long aliveTrees) { this.aliveTrees = aliveTrees; }
        public long getDeadTrees() { return deadTrees; }
        public void setDeadTrees(long deadTrees) { this.deadTrees = deadTrees; }
        public double getSurvivalRate() { return survivalRate; }
        public void setSurvivalRate(double survivalRate) { this.survivalRate = survivalRate; }
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SpeciesSurvivalStat {
        private String species;
        private long totalTrees;
        private long aliveTrees;
        private long deadTrees;
        private double survivalRate;

        public String getSpecies() { return species; }
        public void setSpecies(String species) { this.species = species; }
        public long getTotalTrees() { return totalTrees; }
        public void setTotalTrees(long totalTrees) { this.totalTrees = totalTrees; }
        public long getAliveTrees() { return aliveTrees; }
        public void setAliveTrees(long aliveTrees) { this.aliveTrees = aliveTrees; }
        public long getDeadTrees() { return deadTrees; }
        public void setDeadTrees(long deadTrees) { this.deadTrees = deadTrees; }
        public double getSurvivalRate() { return survivalRate; }
        public void setSurvivalRate(double survivalRate) { this.survivalRate = survivalRate; }
    }

    public long getTotalTrees() { return totalTrees; }
    public void setTotalTrees(long totalTrees) { this.totalTrees = totalTrees; }
    public long getAliveTrees() { return aliveTrees; }
    public void setAliveTrees(long aliveTrees) { this.aliveTrees = aliveTrees; }
    public long getDeadTrees() { return deadTrees; }
    public void setDeadTrees(long deadTrees) { this.deadTrees = deadTrees; }
    public double getOverallSurvivalRate() { return overallSurvivalRate; }
    public void setOverallSurvivalRate(double overallSurvivalRate) { this.overallSurvivalRate = overallSurvivalRate; }
    public long getTotalDrives() { return totalDrives; }
    public void setTotalDrives(long totalDrives) { this.totalDrives = totalDrives; }
    public long getTotalVolunteers() { return totalVolunteers; }
    public void setTotalVolunteers(long totalVolunteers) { this.totalVolunteers = totalVolunteers; }
    public long getTotalCheckIns() { return totalCheckIns; }
    public void setTotalCheckIns(long totalCheckIns) { this.totalCheckIns = totalCheckIns; }
    public List<DriveSurvivalStat> getDriveStats() { return driveStats; }
    public void setDriveStats(List<DriveSurvivalStat> driveStats) { this.driveStats = driveStats; }
    public List<SpeciesSurvivalStat> getSpeciesStats() { return speciesStats; }
    public void setSpeciesStats(List<SpeciesSurvivalStat> speciesStats) { this.speciesStats = speciesStats; }
}
