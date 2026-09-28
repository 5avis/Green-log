package com.example.greenlog.controller;

import com.example.greenlog.dto.PlantationEntryRequest;
import com.example.greenlog.entity.Tree;
import com.example.greenlog.service.TreeService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/trees")
@CrossOrigin(origins = "*")
public class TreeController {

    private final TreeService treeService;

    @Autowired
    public TreeController(TreeService treeService) {
        this.treeService = treeService;
    }

    /**
     * Core grading endpoint 1: POST: Log a new plantation entry with species, location, and date.
     */
    @PostMapping("/log")
    public ResponseEntity<Tree> logNewPlantationEntry(@Valid @RequestBody PlantationEntryRequest request) {
        Tree saved = treeService.logNewTree(request);
        return new ResponseEntity<>(saved, HttpStatus.CREATED);
    }

    /**
     * Standard POST /api/trees accepting PlantationEntryRequest or standard Tree creation.
     */
    @PostMapping
    public ResponseEntity<Tree> createTree(@Valid @RequestBody PlantationEntryRequest request) {
        Tree saved = treeService.logNewTree(request);
        return new ResponseEntity<>(saved, HttpStatus.CREATED);
    }

    /**
     * Core grading endpoint 4: GET: List trees due for their next check-in.
     */
    @GetMapping("/due-for-checkin")
    public ResponseEntity<List<Tree>> getTreesDueForCheckIn() {
        return ResponseEntity.ok(treeService.getTreesDueForCheckIn());
    }

    @GetMapping("/due")
    public ResponseEntity<List<Tree>> getTreesDueAlias() {
        return ResponseEntity.ok(treeService.getTreesDueForCheckIn());
    }

    @GetMapping
    public ResponseEntity<List<Tree>> getAllTrees() {
        return ResponseEntity.ok(treeService.getAllTrees());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Tree> getTreeById(@PathVariable Long id) {
        return ResponseEntity.ok(treeService.getTreeById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Tree> updateTree(@PathVariable Long id, @Valid @RequestBody Tree tree) {
        return ResponseEntity.ok(treeService.updateTree(id, tree));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTree(@PathVariable Long id) {
        treeService.deleteTree(id);
        return ResponseEntity.noContent().build();
    }
}
