package com.example.greenlog.controller;

import com.example.greenlog.entity.PlantationDrive;
import com.example.greenlog.entity.Tree;
import com.example.greenlog.service.PlantationDriveService;
import com.example.greenlog.service.TreeService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/drives")
@CrossOrigin(origins = "*")
public class PlantationDriveController {

    private final PlantationDriveService driveService;
    private final TreeService treeService;

    @Autowired
    public PlantationDriveController(PlantationDriveService driveService, TreeService treeService) {
        this.driveService = driveService;
        this.treeService = treeService;
    }

    @GetMapping
    public ResponseEntity<List<PlantationDrive>> getAllDrives() {
        return ResponseEntity.ok(driveService.getAllDrives());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PlantationDrive> getDriveById(@PathVariable Long id) {
        return ResponseEntity.ok(driveService.getDriveById(id));
    }

    @PostMapping
    public ResponseEntity<PlantationDrive> createDrive(@Valid @RequestBody PlantationDrive drive) {
        PlantationDrive created = driveService.createDrive(drive);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PlantationDrive> updateDrive(@PathVariable Long id, @Valid @RequestBody PlantationDrive drive) {
        return ResponseEntity.ok(driveService.updateDrive(id, drive));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDrive(@PathVariable Long id) {
        driveService.deleteDrive(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/trees")
    public ResponseEntity<List<Tree>> getTreesByDriveId(@PathVariable Long id) {
        return ResponseEntity.ok(treeService.getTreesByDriveId(id));
    }
}
