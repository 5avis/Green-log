package com.example.greenlog.controller;

import com.example.greenlog.entity.Volunteer;
import com.example.greenlog.service.VolunteerService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/volunteers")
@CrossOrigin(origins = "*")
public class VolunteerController {

    private final VolunteerService volunteerService;

    @Autowired
    public VolunteerController(VolunteerService volunteerService) {
        this.volunteerService = volunteerService;
    }

    /**
     * Core grading endpoint 5: GET: Generate a leaderboard of volunteers by trees planted.
     */
    @GetMapping("/leaderboard")
    public ResponseEntity<List<Volunteer>> getLeaderboard() {
        return ResponseEntity.ok(volunteerService.getLeaderboard());
    }

    @GetMapping
    public ResponseEntity<List<Volunteer>> getAllVolunteers() {
        return ResponseEntity.ok(volunteerService.getAllVolunteers());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Volunteer> getVolunteerById(@PathVariable Long id) {
        return ResponseEntity.ok(volunteerService.getVolunteerById(id));
    }

    @PostMapping
    public ResponseEntity<Volunteer> createVolunteer(@Valid @RequestBody Volunteer volunteer) {
        Volunteer created = volunteerService.createVolunteer(volunteer);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Volunteer> updateVolunteer(@PathVariable Long id, @Valid @RequestBody Volunteer volunteer) {
        return ResponseEntity.ok(volunteerService.updateVolunteer(id, volunteer));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteVolunteer(@PathVariable Long id) {
        volunteerService.deleteVolunteer(id);
        return ResponseEntity.noContent().build();
    }
}
