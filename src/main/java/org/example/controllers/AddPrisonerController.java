package org.example.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.example.models.Cell;
import org.example.models.Prisoner;
import org.example.services.PrisonerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Arrays;

@Component
public class AddPrisonerController {
    @FXML private VBox root;
    @FXML private TextField nameField;
    @FXML private DatePicker dobField;
    @FXML private ComboBox<String> genderField;
    @FXML private DatePicker sentenceStartField;
    @FXML private DatePicker sentenceEndField;

    @Autowired private PrisonerService prisonerService;

    private Cell currentCell;

    @FXML
    public void initialize() {
        // Initialize gender ComboBox with options
        genderField.getItems().addAll(Arrays.asList("Male", "Female"));
    }

    public void initData(Cell cell) {
        this.currentCell = cell;
        // Set default values
        sentenceStartField.setValue(LocalDate.now());
        sentenceEndField.setValue(LocalDate.now().plusYears(1)); // Default 1 year sentence
    }

    @FXML
    private void handleSave() {
        try {
            // Validate inputs
            if (nameField.getText().trim().isEmpty()) {
                showError("Validation Error", "Please enter a name");
                return;
            }
            if (dobField.getValue() == null) {
                showError("Validation Error", "Please select a date of birth");
                return;
            }
            if (genderField.getValue() == null) {
                showError("Validation Error", "Please select a gender");
                return;
            }
            if (sentenceStartField.getValue() == null) {
                showError("Validation Error", "Please select a sentence start date");
                return;
            }
            if (sentenceEndField.getValue() == null) {
                showError("Validation Error", "Please select a sentence end date");
                return;
            }
            if (sentenceEndField.getValue().isBefore(sentenceStartField.getValue())) {
                showError("Validation Error", "Sentence end date must be after start date");
                return;
            }

            // Create new prisoner
            Prisoner prisoner = new Prisoner();
            prisoner.setName(nameField.getText().trim());
            prisoner.setDateOfBirth(dobField.getValue());
            prisoner.setGender(genderField.getValue());
            prisoner.setSentenceStart(sentenceStartField.getValue());
            prisoner.setSentenceEnd(sentenceEndField.getValue());
            prisoner.setCell(currentCell);

            // Save prisoner
            prisonerService.createPrisoner(prisoner);

            // Close dialog
            Stage stage = (Stage) root.getScene().getWindow();
            stage.close();

        } catch (Exception e) {
            showError("Error", "Failed to save prisoner: " + e.getMessage());
        }
    }

    @FXML
    private void handleCancel() {
        Stage stage = (Stage) root.getScene().getWindow();
        stage.close();
    }

    private void showError(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    public VBox getRoot() {
        return root;
    }

    public void setRoot(VBox root) {
        this.root = root;
    }
} 