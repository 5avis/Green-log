package com.example.greenlog.controller;

import com.example.greenlog.dto.CheckInRequest;
import com.example.greenlog.entity.CheckIn;
import com.example.greenlog.service.CheckInService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/check-ins")
@CrossOrigin(origins = "*")
public class CheckInController {

    private final CheckInService checkInService;

    @Autowired
    public CheckInController(CheckInService checkInService) {
        this.checkInService = checkInService;
    }

    /**
     * Core grading endpoint 2: POST: Submit a survival check-in (alive/dead).
     */
    @PostMapping
    public ResponseEntity<CheckIn> submitCheckIn(@Valid @RequestBody CheckInRequest request) {
        CheckIn saved = checkInService.submitCheckIn(request);
        return new ResponseEntity<>(saved, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<CheckIn>> getAllCheckIns() {
        return ResponseEntity.ok(checkInService.getAllCheckIns());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CheckIn> getCheckInById(@PathVariable Long id) {
        return ResponseEntity.ok(checkInService.getCheckInById(id));
    }

    @GetMapping("/tree/{treeId}")
    public ResponseEntity<List<CheckIn>> getCheckInsByTreeId(@PathVariable Long treeId) {
        return ResponseEntity.ok(checkInService.getCheckInsByTreeId(treeId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCheckIn(@PathVariable Long id) {
        checkInService.deleteCheckIn(id);
        return ResponseEntity.noContent().build();
    }
}
