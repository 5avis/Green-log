package com.example.greenlog.repository;

import com.example.greenlog.entity.PlantationDrive;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PlantationDriveRepository extends JpaRepository<PlantationDrive, Long> {
}
