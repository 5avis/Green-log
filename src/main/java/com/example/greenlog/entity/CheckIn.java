package com.example.greenlog.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "check_ins")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CheckIn {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Check-in date is required")
    @Column(name = "check_in_date", nullable = false)
    private LocalDate checkInDate;

    @NotBlank(message = "Status reported is required")
    @Column(name = "status_reported", nullable = false)
    private String statusReported; // "ALIVE" or "DEAD"

    @NotNull(message = "Tree reference is required")
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "tree_id", nullable = false)
    @JsonIgnoreProperties({"checkIns", "plantationDrive"})
    private Tree tree;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "volunteer_id")
    @JsonIgnoreProperties("checkIns")
    private Volunteer volunteer;

    // Helper getters for JSON serialization and API consumers
    public Long getTreeId() {
        return tree != null ? tree.getId() : null;
    }

    public String getTreeSpecies() {
        return tree != null ? tree.getSpecies() : null;
    }

    public Long getVolunteerId() {
        return volunteer != null ? volunteer.getId() : null;
    }

    public String getVolunteerName() {
        return volunteer != null ? volunteer.getName() : null;
    }

    // Explicit getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public Tree getTree() {
        return tree;
    }

    public void setTree(Tree tree) {
        this.tree = tree;
    }

    public Volunteer getVolunteer() {
        return volunteer;
    }

    public void setVolunteer(Volunteer volunteer) {
        this.volunteer = volunteer;
    }
}
