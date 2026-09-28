package com.example.greenlog;

import com.example.greenlog.dto.CheckInRequest;
import com.example.greenlog.dto.PlantationEntryRequest;
import com.example.greenlog.dto.SurvivalStatsResponse;
import com.example.greenlog.entity.PlantationDrive;
import com.example.greenlog.entity.Tree;
import com.example.greenlog.entity.Volunteer;
import com.example.greenlog.exception.DeadTreeException;
import com.example.greenlog.service.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class ServiceLayerTests {

    @Autowired
    private TreeService treeService;

    @Autowired
    private CheckInService checkInService;

    @Autowired
    private PlantationDriveService driveService;

    @Autowired
    private VolunteerService volunteerService;

    @Autowired
    private StatsService statsService;

    @Test
    @DisplayName("Test logging new plantation entry and updating volunteer trees planted")
    void testLogNewTree() {
        PlantationDrive drive = driveService.createDrive(PlantationDrive.builder()
                .name("Spring Valley Drive")
                .location("North Park")
                .date(LocalDate.now())
                .build());

        Volunteer volunteer = volunteerService.createVolunteer(Volunteer.builder()
                .name("Alice")
                .totalTreesPlanted(0)
                .build());

        PlantationEntryRequest request = PlantationEntryRequest.builder()
                .species("Neem")
                .locationGps("12.9716, 77.5946")
                .datePlanted(LocalDate.now())
                .plantationDriveId(drive.getId())
                .volunteerId(volunteer.getId())
                .build();

        Tree loggedTree = treeService.logNewTree(request);

        assertNotNull(loggedTree.getId());
        assertEquals("Neem", loggedTree.getSpecies());
        assertEquals("ALIVE", loggedTree.getStatus());
        assertEquals(drive.getId(), loggedTree.getPlantationDrive().getId());

        Volunteer updatedVolunteer = volunteerService.getVolunteerById(volunteer.getId());
        assertEquals(1, updatedVolunteer.getTotalTreesPlanted());
    }

    @Test
    @DisplayName("STRICT RULE 1: A tree marked DEAD cannot receive further check-ins")
    void testDeadTreeRuleEnforced() {
        Tree tree = treeService.createTree(Tree.builder()
                .species("Banyan")
                .locationGps("13.0827, 80.2707")
                .datePlanted(LocalDate.now().minusDays(10))
                .status("ALIVE")
                .build());

        // First check-in: mark tree as DEAD
        CheckInRequest firstCheckIn = CheckInRequest.builder()
                .treeId(tree.getId())
                .checkInDate(LocalDate.now().minusDays(2))
                .statusReported("DEAD")
                .build();

        checkInService.submitCheckIn(firstCheckIn);

        Tree deadTree = treeService.getTreeById(tree.getId());
        assertEquals("DEAD", deadTree.getStatus());

        // Second check-in: attempt check-in on dead tree -> MUST throw DeadTreeException
        CheckInRequest secondCheckIn = CheckInRequest.builder()
                .treeId(tree.getId())
                .checkInDate(LocalDate.now())
                .statusReported("ALIVE")
                .build();

        DeadTreeException exception = assertThrows(DeadTreeException.class, () -> {
            checkInService.submitCheckIn(secondCheckIn);
        });

        assertTrue(exception.getMessage().contains("cannot receive further check-ins"));
    }

    @Test
    @DisplayName("STRICT RULE 2: Survival stats auto-calculated per drive and species")
    void testSurvivalStatsRecalculation() {
        PlantationDrive drive = driveService.createDrive(PlantationDrive.builder()
                .name("Eco Drive 2026")
                .location("South Ridge")
                .date(LocalDate.now())
                .build());

        Tree tree1 = treeService.createTree(Tree.builder()
                .species("Oak")
                .locationGps("10.0, 20.0")
                .datePlanted(LocalDate.now())
                .status("ALIVE")
                .plantationDrive(drive)
                .build());

        Tree tree2 = treeService.createTree(Tree.builder()
                .species("Oak")
                .locationGps("10.1, 20.1")
                .datePlanted(LocalDate.now())
                .status("ALIVE")
                .plantationDrive(drive)
                .build());

        // Submit check-in marking tree2 as DEAD
        checkInService.submitCheckIn(CheckInRequest.builder()
                .treeId(tree2.getId())
                .checkInDate(LocalDate.now())
                .statusReported("DEAD")
                .build());

        SurvivalStatsResponse stats = statsService.calculateSurvivalStats();
        assertNotNull(stats);
        assertTrue(stats.getTotalTrees() >= 2);
    }
}
