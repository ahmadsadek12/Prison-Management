package org.example.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextArea;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.stage.Stage;
import org.example.models.Prisoner;
import org.example.models.Visitor;
import org.example.models.VisitorLog;
import org.example.services.VisitorService;
import org.example.services.VisitorLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Component
@Scope("prototype")
public class AddVisitorLogController {
    @FXML
    private ComboBox<Visitor> visitorComboBox;
    @FXML
    private TextField newVisitorNameField;
    @FXML
    private Label newVisitorNameLabel;
    @FXML
    private ComboBox<String> relationshipComboBox;
    @FXML
    private DatePicker visitDatePicker;
    @FXML
    private ComboBox<String> statusComboBox;
    @FXML
    private TextArea commentsArea;

    private final VisitorService visitorService;
    private final VisitorLogService visitorLogService;
    private Prisoner prisoner;
    private boolean isNewVisitor = false;
    private VisitorLog editingLog;
    private javafx.scene.Parent root;

    @Autowired
    public AddVisitorLogController(VisitorService visitorService, VisitorLogService visitorLogService) {
        this.visitorService = visitorService;
        this.visitorLogService = visitorLogService;
    }

    @FXML
    public void initialize() {
        relationshipComboBox.getItems().addAll(
            "Family",
            "Friend",
            "Lawyer",
            "Other"
        );
        
        statusComboBox.getItems().addAll(
            "PENDING",
            "APPROVED",
            "REJECTED",
            "COMPLETED"
        );

        // Set default values
        visitDatePicker.setValue(LocalDate.now());
        statusComboBox.setValue("PENDING");

        // Set up visitor ComboBox to display names properly
        visitorComboBox.setCellFactory(param -> new ListCell<Visitor>() {
            @Override
            protected void updateItem(Visitor visitor, boolean empty) {
                super.updateItem(visitor, empty);
                if (empty || visitor == null) {
                    setText(null);
                } else {
                    setText(visitor.getName() + " (" + visitor.getRelationship() + ")");
                }
            }
        });
        
        visitorComboBox.setButtonCell(new ListCell<Visitor>() {
            @Override
            protected void updateItem(Visitor visitor, boolean empty) {
                super.updateItem(visitor, empty);
                if (empty || visitor == null) {
                    setText("Select a visitor...");
                } else {
                    setText(visitor.getName() + " (" + visitor.getRelationship() + ")");
                }
            }
        });

        // Load existing visitors
        loadVisitors();
    }

    private void loadVisitors() {
        try {
            List<Visitor> visitors = visitorService.getAllVisitors();
            visitorComboBox.getItems().addAll(visitors);
        } catch (Exception e) {
            System.err.println("Error loading visitors: " + e.getMessage());
        }
    }

    public void setPrisoner(Prisoner prisoner) {
        this.prisoner = prisoner;
    }

    @FXML
    private void handleNewVisitor() {
        isNewVisitor = true;
        visitorComboBox.setVisible(false);
        newVisitorNameLabel.setVisible(true);
        newVisitorNameField.setVisible(true);
    }

    public void setVisitorLog(VisitorLog log) {
        this.editingLog = log;
        if (log != null) {
            try {
                // Load the visitor by ID and set it in the combo box
                visitorService.getVisitorById(log.getVisitorId()).ifPresent(visitor -> {
                    visitorComboBox.setValue(visitor);
                });
                
                // Set other fields from the log
                relationshipComboBox.setValue(log.getRelationship());
                visitDatePicker.setValue(log.getDate());
                statusComboBox.setValue(log.getStatus());
                commentsArea.setText(log.getNotes());
            } catch (Exception e) {
                System.err.println("Error setting visitor log for editing: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleSave() {
        if (prisoner != null) {
            try {
                Visitor visitor;
                if (isNewVisitor) {
                    // Create new visitor
                    String name = newVisitorNameField.getText().trim();
                    String relationship = relationshipComboBox.getValue();
                    if (name.isEmpty() || relationship == null) {
                        // TODO: Show error dialog
                        return;
                    }
                    visitor = visitorService.createVisitor(name, relationship);
                } else if (editingLog != null) {
                    // For editing, get visitor from combo box or load by ID from existing log
                    visitor = visitorComboBox.getValue();
                    if (visitor == null) {
                        // If no visitor selected in combo box, load from existing log
                        visitor = visitorService.getVisitorById(editingLog.getVisitorId()).orElse(null);
                        if (visitor == null) {
                            System.err.println("Could not find visitor for editing log");
                            return;
                        }
                    }
                } else {
                    visitor = visitorComboBox.getValue();
                    if (visitor == null) {
                        // TODO: Show error dialog
                        return;
                    }
                }
                if (editingLog == null) {
                // Create visitor log
                VisitorLog visitorLog = visitorLogService.createVisitorLog(
                    visitor,
                    prisoner,
                    visitDatePicker.getValue(),
                    LocalTime.now(),
                    commentsArea.getText()
                );
                // Update status if not PENDING
                String status = statusComboBox.getValue();
                if (!"PENDING".equals(status)) {
                    switch (status) {
                        case "APPROVED":
                            visitorLogService.approveVisitorLog(visitorLog.getId());
                            break;
                        case "REJECTED":
                            visitorLogService.rejectVisitorLog(visitorLog.getId());
                            break;
                        case "COMPLETED":
                            visitorLogService.completeVisitorLog(visitorLog.getId());
                            break;
                    }
                }
                } else {
                    // Update existing log
                    editingLog.setPrisonerId(prisoner.getId());
                    editingLog.setVisitorId(visitor.getId());
                    editingLog.setDate(visitDatePicker.getValue());
                    editingLog.setRelationship(relationshipComboBox.getValue());
                    editingLog.setNotes(commentsArea.getText());
                    editingLog.setStatus(statusComboBox.getValue());
                    visitorLogService.updateVisitorLog(editingLog.getId(), editingLog);
                }
                // Close the window
                Stage stage = (Stage) visitorComboBox.getScene().getWindow();
                stage.close();
            } catch (Exception e) {
                System.err.println("Error saving visitor log: " + e.getMessage());
                // TODO: Show error dialog
            }
        }
    }

    @FXML
    private void handleCancel() {
        // Close the window without saving
        Stage stage = (Stage) visitorComboBox.getScene().getWindow();
        stage.close();
    }

    public void setRoot(javafx.scene.Parent root) { this.root = root; }
    public javafx.scene.Parent getRoot() { return root; }
} 