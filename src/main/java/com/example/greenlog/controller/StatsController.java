package com.example.greenlog.controller;

import com.example.greenlog.dto.SurvivalStatsResponse;
import com.example.greenlog.dto.SurvivalStatsResponse.DriveSurvivalStat;
import com.example.greenlog.dto.SurvivalStatsResponse.SpeciesSurvivalStat;
import com.example.greenlog.service.StatsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/stats")
@CrossOrigin(origins = "*")
public class StatsController {

    private final StatsService statsService;

    @Autowired
    public StatsController(StatsService statsService) {
        this.statsService = statsService;
    }

    /**
     * Core grading endpoint 3: GET: View survival rate per drive and per species (plus overall system metrics).
     */
    @GetMapping("/survival-rates")
    public ResponseEntity<SurvivalStatsResponse> getSurvivalRates() {
        return ResponseEntity.ok(statsService.calculateSurvivalStats());
    }

    @GetMapping
    public ResponseEntity<SurvivalStatsResponse> getOverallStats() {
        return ResponseEntity.ok(statsService.calculateSurvivalStats());
    }

    @GetMapping("/drives")
    public ResponseEntity<List<DriveSurvivalStat>> getDriveSurvivalRates() {
        return ResponseEntity.ok(statsService.calculateSurvivalStats().getDriveStats());
    }

    @GetMapping("/species")
    public ResponseEntity<List<SpeciesSurvivalStat>> getSpeciesSurvivalRates() {
        return ResponseEntity.ok(statsService.calculateSurvivalStats().getSpeciesStats());
    }

    @DeleteMapping("/purge-all")
    public ResponseEntity<Void> purgeAllData() {
        statsService.purgeAllData();
        return ResponseEntity.noContent().build();
    }
}
