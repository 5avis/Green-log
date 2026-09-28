package com.example.greenlog.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlantationEntryRequest {

    @NotBlank(message = "Species is required")
    private String species;

    @NotBlank(message = "GPS location is required")
    private String locationGps;

    @NotNull(message = "Date planted is required")
    private LocalDate datePlanted;

    private Long plantationDriveId;

    private Long volunteerId;

    private String newVolunteerName;

    // Explicit getters and setters
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

    public Long getPlantationDriveId() {
        return plantationDriveId;
    }

    public void setPlantationDriveId(Long plantationDriveId) {
        this.plantationDriveId = plantationDriveId;
    }

    public Long getVolunteerId() {
        return volunteerId;
    }

    public void setVolunteerId(Long volunteerId) {
        this.volunteerId = volunteerId;
    }

    public String getNewVolunteerName() {
        return newVolunteerName;
    }

    public void setNewVolunteerName(String newVolunteerName) {
        this.newVolunteerName = newVolunteerName;
    }
}
