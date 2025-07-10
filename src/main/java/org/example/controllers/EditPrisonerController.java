package org.example.controllers;

import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.TextField;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;
import org.example.models.Prisoner;
import org.example.services.PrisonerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.context.annotation.Scope;
import java.time.LocalDate;

@Component
@Scope("prototype")
public class EditPrisonerController {
    @FXML
    private TextField nameField;
    @FXML
    private DatePicker dobField;
    @FXML
    private ComboBox<String> genderComboBox;
    @FXML
    private DatePicker sentenceStartPicker;
    @FXML
    private DatePicker sentenceEndPicker;
    @FXML
    private ComboBox<String> blockComboBox;

    private Prisoner prisoner;
    private Parent root;
    
    @Autowired
    private PrisonerService prisonerService;

    public Parent getRoot() {
        return root;
    }

    public void setRoot(Parent root) {
        this.root = root;
    }

    @FXML
    public void initialize() {
        genderComboBox.getItems().addAll("MALE", "FEMALE");
        blockComboBox.getItems().addAll("A", "B", "C", "D");
    }

    public void setPrisoner(Prisoner prisoner) {
        this.prisoner = prisoner;
        populateFields();
    }

    private void populateFields() {
        if (prisoner != null) {
            nameField.setText(prisoner.getName());
            dobField.setValue(prisoner.getDateOfBirth());
            genderComboBox.setValue(prisoner.getGender());
            sentenceStartPicker.setValue(prisoner.getSentenceStart());
            sentenceEndPicker.setValue(prisoner.getSentenceEnd());
            blockComboBox.setValue(prisoner.getCell().getBlock().getType());
        }
    }

    @FXML
    private void handleSave() {
        if (prisoner != null) {
            try {
                // Update prisoner fields
                prisoner.setName(nameField.getText());
                prisoner.setDateOfBirth(dobField.getValue());
                prisoner.setGender(genderComboBox.getValue());
                prisoner.setSentenceStart(sentenceStartPicker.getValue());
                prisoner.setSentenceEnd(sentenceEndPicker.getValue());
                // TODO: Update block/cell
                
                // Save to database
                prisonerService.updatePrisoner(prisoner.getId(), prisoner);
                
                System.out.println("Prisoner saved successfully. Closing window...");
                
                // Show success message
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Success");
                alert.setHeaderText("Prisoner Updated");
                alert.setContentText("Prisoner details have been saved successfully.");
                alert.showAndWait();
                
                // Close the window
                Stage stage = (Stage) nameField.getScene().getWindow();
                if (stage != null) {
                    stage.close();
                } else {
                    System.err.println("Stage is null, cannot close window");
                }
            } catch (Exception e) {
                System.err.println("Error saving prisoner: " + e.getMessage());
                e.printStackTrace();
                // Even if there's an error, try to close the window
                try {
                    Stage stage = (Stage) nameField.getScene().getWindow();
                    if (stage != null) {
                        stage.close();
                    }
                } catch (Exception closeException) {
                    System.err.println("Error closing window: " + closeException.getMessage());
                }
            }
        } else {
            System.err.println("Prisoner is null, cannot save");
        }
    }

    @FXML
    private void handleCancel() {
        // Close the window without saving
        Stage stage = (Stage) nameField.getScene().getWindow();
        stage.close();
    }
} 