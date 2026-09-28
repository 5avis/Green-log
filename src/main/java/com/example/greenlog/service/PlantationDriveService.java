package com.example.greenlog.service;

import com.example.greenlog.entity.PlantationDrive;
import com.example.greenlog.exception.ResourceNotFoundException;
import com.example.greenlog.repository.PlantationDriveRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class PlantationDriveService {

    private final PlantationDriveRepository driveRepository;

    @Autowired
    public PlantationDriveService(PlantationDriveRepository driveRepository) {
        this.driveRepository = driveRepository;
    }

    public PlantationDrive createDrive(PlantationDrive drive) {
        return driveRepository.save(drive);
    }

    @Transactional(readOnly = true)
    public List<PlantationDrive> getAllDrives() {
        return driveRepository.findAll();
    }

    @Transactional(readOnly = true)
    public PlantationDrive getDriveById(Long id) {
        return driveRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Plantation Drive not found with id: " + id));
    }

    public PlantationDrive updateDrive(Long id, PlantationDrive updatedDrive) {
        PlantationDrive existing = getDriveById(id);
        existing.setName(updatedDrive.getName());
        existing.setLocation(updatedDrive.getLocation());
        existing.setDate(updatedDrive.getDate());
        return driveRepository.save(existing);
    }

    public void deleteDrive(Long id) {
        PlantationDrive existing = getDriveById(id);
        driveRepository.delete(existing);
    }
}
