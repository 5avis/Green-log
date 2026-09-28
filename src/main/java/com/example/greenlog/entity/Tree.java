package com.example.greenlog.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "trees")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Tree {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Species is required")
    @Column(nullable = false)
    private String species;

    @NotBlank(message = "GPS location is required")
    @Column(name = "location_gps", nullable = false)
    private String locationGps;

    @NotNull(message = "Date planted is required")
    @Column(name = "date_planted", nullable = false)
    private LocalDate datePlanted;

    @Column(nullable = false)
    @Builder.Default
    private String status = "ALIVE";

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "plantation_drive_id")
    @JsonIgnoreProperties("trees")
    private PlantationDrive plantationDrive;

    @OneToMany(mappedBy = "tree", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    @Builder.Default
    private List<CheckIn> checkIns = new ArrayList<>();

    // Helper method to return plantation_drive_id in JSON
    public Long getPlantationDriveId() {
        return plantationDrive != null ? plantationDrive.getId() : null;
    }

    public String getPlantationDriveName() {
        return plantationDrive != null ? plantationDrive.getName() : null;
    }

    // Explicit getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSpecies() {
        return species;
    }

    public void setSpecies(String species) {
        this.species = species;
    }

    public String getLocationGps() {
        return locationGps;
    }

    public void setLocationGps(String locationGps) {
        this.locationGps = locationGps;
    }

    public LocalDate getDatePlanted() {
        return datePlanted;
    }

    public void setDatePlanted(LocalDate datePlanted) {
        this.datePlanted = datePlanted;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public PlantationDrive getPlantationDrive() {
        return plantationDrive;
    }

    public void setPlantationDrive(PlantationDrive plantationDrive) {
        this.plantationDrive = plantationDrive;
    }

    public List<CheckIn> getCheckIns() {
        return checkIns;
    }

    public void setCheckIns(List<CheckIn> checkIns) {
        this.checkIns = checkIns;
    }
}
