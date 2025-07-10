package org.example.controllers;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.example.models.VisitorLog;
import org.example.models.Visitor;
import org.example.models.Prisoner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;
import org.example.services.VisitorLogService;
import org.example.services.VisitorService;
import org.example.services.PrisonerService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Component
@RestController
@RequestMapping("/api/visitor-logs")
@CrossOrigin(origins = "*")
public class VisitorController {

    private final VisitorLogService visitorLogService;
    private final VisitorService visitorService;
    private final PrisonerService prisonerService;

    @FXML private TableView<VisitorLog> visitorTable;
    @FXML private TableColumn<VisitorLog, String> visitorNameCol;
    @FXML private TableColumn<VisitorLog, String> relationshipCol;
    @FXML private TableColumn<VisitorLog, String> prisonerNameCol;
    @FXML private TableColumn<VisitorLog, String> dateCol;
    @FXML private TableColumn<VisitorLog, Void> actionsCol;

    @Autowired
    public VisitorController(VisitorLogService visitorLogService, VisitorService visitorService, PrisonerService prisonerService) {
        this.visitorLogService = visitorLogService;
        this.visitorService = visitorService;
        this.prisonerService = prisonerService;
    }

    @FXML
    public void initialize() {
        setupTableColumns();
        loadVisitors();
    }

    private void setupTableColumns() {
        visitorNameCol.setCellValueFactory(cellData -> {
            VisitorLog log = cellData.getValue();
            try {
                Visitor visitor = visitorService.getVisitorById(log.getVisitorId()).orElse(null);
                return new ReadOnlyStringWrapper(visitor != null ? visitor.getName() : "Unknown");
            } catch (Exception e) {
                return new ReadOnlyStringWrapper("Error loading visitor");
            }
        });

        relationshipCol.setCellValueFactory(cellData -> 
            new ReadOnlyStringWrapper(cellData.getValue().getRelationship()));

        prisonerNameCol.setCellValueFactory(cellData -> {
            VisitorLog log = cellData.getValue();
            try {
                Prisoner prisoner = prisonerService.getPrisonerById(log.getPrisonerId());
                return new ReadOnlyStringWrapper(prisoner != null ? prisoner.getName() : "Unknown");
            } catch (Exception e) {
                return new ReadOnlyStringWrapper("Error loading prisoner");
            }
        });

        dateCol.setCellValueFactory(cellData ->
            new ReadOnlyStringWrapper(cellData.getValue().getDate().toString()));
    }

    private void loadVisitors() {
        List<VisitorLog> visitorLogs = visitorLogService.getAllVisitorLogs();
        visitorTable.setItems(FXCollections.observableArrayList(visitorLogs));
    }

    @FXML
    private void showAddVisitorForm() {
        System.out.println("Show Add Visitor Form");
    }

    @FXML
    private void goBack() {
        System.out.println("Navigating back to Dashboard");
    }

    // REST Endpoints
    @GetMapping
    public ResponseEntity<List<VisitorLog>> getAllVisitorLogs() {
        return ResponseEntity.ok(visitorLogService.getAllVisitorLogs());
    }

    @GetMapping("/{id}")
    public ResponseEntity<VisitorLog> getVisitorLogById(@PathVariable String id) {
        return visitorLogService.getVisitorLogById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/prisoner/{prisonerId}")
    public ResponseEntity<List<VisitorLog>> getVisitorLogsByPrisoner(@PathVariable String prisonerId) {
        return ResponseEntity.ok(visitorLogService.getVisitorLogsByPrisoner(prisonerId));
    }

    @GetMapping("/visitor/{visitorId}")
    public ResponseEntity<List<VisitorLog>> getVisitorLogsByVisitor(@PathVariable String visitorId) {
        return ResponseEntity.ok(visitorLogService.getVisitorLogsByVisitor(visitorId));
    }

    @GetMapping("/date-range")
    public ResponseEntity<List<VisitorLog>> getVisitorLogsByDateRange(
            @RequestParam LocalDateTime start,
            @RequestParam LocalDateTime end) {
        return ResponseEntity.ok(visitorLogService.getVisitorLogsByDateRange(start, end));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<VisitorLog>> getVisitorLogsByStatus(@PathVariable String status) {
        return ResponseEntity.ok(visitorLogService.getVisitorLogsByStatus(status));
    }

    @GetMapping("/pending")
    public ResponseEntity<List<VisitorLog>> getPendingVisitorLogs() {
        return ResponseEntity.ok(visitorLogService.getPendingVisitorLogs());
    }

    @GetMapping("/approved")
    public ResponseEntity<List<VisitorLog>> getApprovedVisitorLogs() {
        return ResponseEntity.ok(visitorLogService.getApprovedVisitorLogs());
    }

    @GetMapping("/needing-followup")
    public ResponseEntity<List<VisitorLog>> getVisitorLogsNeedingFollowUp() {
        return ResponseEntity.ok(visitorLogService.getVisitorLogsNeedingFollowUp());
    }

    @GetMapping("/prisoner/{prisonerId}/needing-followup")
    public ResponseEntity<List<VisitorLog>> getPrisonerVisitsNeedingFollowUp(@PathVariable String prisonerId) {
        return ResponseEntity.ok(visitorLogService.getPrisonerVisitsNeedingFollowUp(prisonerId));
    }

    @GetMapping("/relationship/{relationship}")
    public ResponseEntity<List<VisitorLog>> getVisitorLogsByRelationship(@PathVariable String relationship) {
        return ResponseEntity.ok(visitorLogService.getVisitorLogsByRelationship(relationship));
    }

    @GetMapping("/prisoner/{prisonerId}/relationship/{relationship}")
    public ResponseEntity<List<VisitorLog>> getVisitorLogsByPrisonerAndRelationship(
            @PathVariable String prisonerId,
            @PathVariable String relationship) {
        return ResponseEntity.ok(visitorLogService.getVisitorLogsByPrisonerAndRelationship(prisonerId, relationship));
    }

    @PostMapping
    public ResponseEntity<VisitorLog> createVisitorLog(@RequestBody VisitorLogRequest request) {
        try {
            Visitor visitor = visitorService.getVisitorById(Integer.parseInt(request.getVisitorId()))
                .orElseThrow(() -> new RuntimeException("Visitor not found with ID: " + request.getVisitorId()));
            Prisoner prisoner = prisonerService.getPrisonerById(Integer.parseInt(request.getPrisonerId()));

            VisitorLog visitorLog = visitorLogService.createVisitorLog(
                visitor,
                prisoner,
                request.getVisitDate(),
                request.getVisitTime(),
                request.getComments()
            );
            return ResponseEntity.ok(visitorLog);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid ID format. Visitor and Prisoner IDs must be integers.");
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<VisitorLog> updateVisitorLog(@PathVariable String id, @RequestBody VisitorLogRequest request) {
        try {
            VisitorLog existingLog = visitorLogService.getVisitorLogById(id)
                .orElseThrow(() -> new RuntimeException("Visitor log not found with ID: " + id));

            Visitor visitor = visitorService.getVisitorById(Integer.parseInt(request.getVisitorId()))
                .orElseThrow(() -> new RuntimeException("Visitor not found with ID: " + request.getVisitorId()));
            Prisoner prisoner = prisonerService.getPrisonerById(Integer.parseInt(request.getPrisonerId()));

            existingLog.setVisitorId(visitor.getId());
            existingLog.setPrisonerId(prisoner.getId());
            existingLog.setDate(request.getVisitDate());
            existingLog.setDuration(60); // Default duration
            existingLog.setRelationship(visitor.getRelationship());
            existingLog.setNotes(request.getComments());

            return ResponseEntity.ok(visitorLogService.updateVisitorLog(id, existingLog));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid ID format. Visitor and Prisoner IDs must be integers.");
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteVisitorLog(@PathVariable String id) {
        visitorLogService.deleteVisitorLog(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/approve")
    public ResponseEntity<VisitorLog> approveVisitorLog(@PathVariable String id) {
        return ResponseEntity.ok(visitorLogService.approveVisitorLog(id));
    }

    @PostMapping("/{id}/reject")
    public ResponseEntity<VisitorLog> rejectVisitorLog(@PathVariable String id) {
        return ResponseEntity.ok(visitorLogService.rejectVisitorLog(id));
    }

    @PostMapping("/{id}/complete")
    public ResponseEntity<VisitorLog> completeVisitorLog(@PathVariable String id) {
        return ResponseEntity.ok(visitorLogService.completeVisitorLog(id));
    }

    // DTO classes
    private static class VisitorLogRequest {
        private String visitorId;
        private String prisonerId;
        private LocalDate visitDate;
        private LocalTime visitTime;
        private String comments;

        public String getVisitorId() {
            return visitorId;
        }

        public void setVisitorId(String visitorId) {
            this.visitorId = visitorId;
        }

        public String getPrisonerId() {
            return prisonerId;
        }

        public void setPrisonerId(String prisonerId) {
            this.prisonerId = prisonerId;
        }

        public LocalDate getVisitDate() {
            return visitDate;
        }

        public void setVisitDate(LocalDate visitDate) {
            this.visitDate = visitDate;
        }

        public LocalTime getVisitTime() {
            return visitTime;
        }

        public void setVisitTime(LocalTime visitTime) {
            this.visitTime = visitTime;
        }

        public String getComments() {
            return comments;
        }

        public void setComments(String comments) {
            this.comments = comments;
        }
    }
}
