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
public class CheckInRequest {

    @NotNull(message = "Tree ID is required")
    private Long treeId;

    @NotNull(message = "Check-in date is required")
    private LocalDate checkInDate;

    @NotBlank(message = "Status reported is required (ALIVE or DEAD)")
    private String statusReported;

    private Long volunteerId;

    // Explicit getters and setters
    public Long getTreeId() {
        return treeId;
    }

    public void setTreeId(Long treeId) {
        this.treeId = treeId;
    }

    public LocalDate getCheckInDate() {
        return checkInDate;
    }

    public void setCheckInDate(LocalDate checkInDate) {
        this.checkInDate = checkInDate;
    }

    public String getStatusReported() {
        return statusReported;
    }

    public void setStatusReported(String statusReported) {
        this.statusReported = statusReported;
    }

    public Long getVolunteerId() {
        return volunteerId;
    }

    public void setVolunteerId(Long volunteerId) {
        this.volunteerId = volunteerId;
    }
}
