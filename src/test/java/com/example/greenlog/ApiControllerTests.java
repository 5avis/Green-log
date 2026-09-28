package com.example.greenlog;

import com.example.greenlog.controller.*;
import com.example.greenlog.dto.*;
import com.example.greenlog.entity.*;
import com.example.greenlog.exception.DeadTreeException;
import com.example.greenlog.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class ApiControllerTests {

    @Autowired
    private PlantationDriveController driveController;

    @Autowired
    private TreeController treeController;

    @Autowired
    private VolunteerController volunteerController;

    @Autowired
    private CheckInController checkInController;

    @Autowired
    private StatsController statsController;

    @Autowired
    private GlobalExceptionHandler exceptionHandler;

    @Test
    @DisplayName("Grading Endpoint 1: POST /api/trees/log creates plantation entry")
    void testLogPlantationEntryEndpoint() {
        PlantationDrive drive = PlantationDrive.builder()
                .name("Green Highway Drive")
                .location("Highway 44")
                .date(LocalDate.now())
                .build();
        ResponseEntity<PlantationDrive> driveResponse = driveController.createDrive(drive);
        assertEquals(HttpStatus.CREATED, driveResponse.getStatusCode());
        assertNotNull(driveResponse.getBody());
        Long driveId = driveResponse.getBody().getId();

        PlantationEntryRequest request = PlantationEntryRequest.builder()
                .species("Teak")
                .locationGps("12.9100, 77.6000")
                .datePlanted(LocalDate.now())
                .plantationDriveId(driveId)
                .build();

        ResponseEntity<Tree> response = treeController.logNewPlantationEntry(request);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Teak", response.getBody().getSpecies());
        assertEquals("ALIVE", response.getBody().getStatus());
        assertEquals(driveId, response.getBody().getPlantationDriveId());
    }

    @Test
    @DisplayName("Grading Endpoint 2 & Dead Tree Rule: Clean 400 JSON error when checking in dead tree")
    void testCheckInDeadTreeThrowsCleanJsonError() {
        // Create tree
        PlantationEntryRequest request = PlantationEntryRequest.builder()
                .species("Mahogany")
                .locationGps("12.9200, 77.6100")
                .datePlanted(LocalDate.now().minusDays(5))
                .build();
        ResponseEntity<Tree> treeResp = treeController.logNewPlantationEntry(request);
        Long treeId = treeResp.getBody().getId();

        // First check-in: mark DEAD
        CheckInRequest firstCheckIn = CheckInRequest.builder()
                .treeId(treeId)
                .checkInDate(LocalDate.now())
                .statusReported("DEAD")
                .build();

        ResponseEntity<CheckIn> checkInResp = checkInController.submitCheckIn(firstCheckIn);
        assertEquals(HttpStatus.CREATED, checkInResp.getStatusCode());
        assertEquals("DEAD", checkInResp.getBody().getStatusReported());

        // Second check-in: attempt check-in on dead tree -> MUST throw DeadTreeException
        CheckInRequest secondCheckIn = CheckInRequest.builder()
                .treeId(treeId)
                .checkInDate(LocalDate.now())
                .statusReported("ALIVE")
                .build();

        DeadTreeException exception = assertThrows(DeadTreeException.class, () -> {
            checkInController.submitCheckIn(secondCheckIn);
        });

        // Verify GlobalExceptionHandler produces a clean HTTP 400 Bad Request JSON response
        ResponseEntity<ErrorResponse> errorResp = exceptionHandler.handleDeadTreeException(exception);
        assertEquals(HttpStatus.BAD_REQUEST, errorResp.getStatusCode());
        assertNotNull(errorResp.getBody());
        assertEquals(400, errorResp.getBody().getStatus());
        assertEquals("Bad Request", errorResp.getBody().getError());
        assertTrue(errorResp.getBody().getMessage().contains("cannot receive further check-ins"));
    }

    @Test
    @DisplayName("Grading Endpoint 3: GET /api/stats/survival-rates")
    void testSurvivalRatesEndpoint() {
        ResponseEntity<SurvivalStatsResponse> response = statsController.getSurvivalRates();
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().getDriveStats());
        assertNotNull(response.getBody().getSpeciesStats());
    }

    @Test
    @DisplayName("Grading Endpoint 4: GET /api/trees/due-for-checkin")
    void testTreesDueForCheckInEndpoint() {
        PlantationEntryRequest request = PlantationEntryRequest.builder()
                .species("Cedar")
                .locationGps("12.9300, 77.6200")
                .datePlanted(LocalDate.now().minusDays(20))
                .build();
        treeController.logNewPlantationEntry(request);

        ResponseEntity<List<Tree>> response = treeController.getTreesDueForCheckIn();
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isEmpty());
    }

    @Test
    @DisplayName("Grading Endpoint 5: GET /api/volunteers/leaderboard")
    void testVolunteerLeaderboardEndpoint() {
        volunteerController.createVolunteer(Volunteer.builder()
                .name("Bob Green")
                .totalTreesPlanted(15)
                .build());

        volunteerController.createVolunteer(Volunteer.builder()
                .name("Alice Planter")
                .totalTreesPlanted(42)
                .build());

        ResponseEntity<List<Volunteer>> response = volunteerController.getLeaderboard();
        assertEquals(HttpStatus.OK, response.getStatusCode());
        List<Volunteer> list = response.getBody();
        assertNotNull(list);
        assertTrue(list.size() >= 2);

        // Verify descending sort order for entire leaderboard
        for (int i = 0; i < list.size() - 1; i++) {
            assertTrue(list.get(i).getTotalTreesPlanted() >= list.get(i + 1).getTotalTreesPlanted(),
                    "Leaderboard must be sorted in descending order of total trees planted");
        }
    }
}
