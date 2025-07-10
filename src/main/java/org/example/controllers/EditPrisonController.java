package org.example.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.example.models.Prison;
import org.example.services.PrisonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.logging.Logger;
import java.util.logging.Level;

@Component
public class EditPrisonController {
    private static final Logger LOGGER = Logger.getLogger(EditPrisonController.class.getName());

    @FXML private TextField prisonNameField;
    @FXML private TextField prisonStateField;
    @FXML private TextField prisonCityField;
    @FXML private TextField prisonStreetField;
    @FXML private TextField prisonCountryField;

    private final PrisonService prisonService;
    private Prison prison;

    @Autowired
    public EditPrisonController(PrisonService prisonService) {
        this.prisonService = prisonService;
    }

    public void setPrison(Prison prison) {
        LOGGER.info("Setting prison data for editing: " + prison.getName());
        this.prison = prison;
        populateFields();
    }

    private void populateFields() {
        if (prison != null) {
            prisonNameField.setText(prison.getName());
            prisonStateField.setText(prison.getState());
            prisonCityField.setText(prison.getCity());
            prisonStreetField.setText(prison.getStreet());
            prisonCountryField.setText(prison.getCountry());
        }
    }

    @FXML
    private void handleSavePrisonChanges() {
        LOGGER.info("Saving prison changes...");
        try {
            if (prison != null) {
                Prison updatedPrison = new Prison();
                updatedPrison.setName(prisonNameField.getText());
                updatedPrison.setState(prisonStateField.getText());
                updatedPrison.setCity(prisonCityField.getText());
                updatedPrison.setStreet(prisonStreetField.getText());
                updatedPrison.setCountry(prisonCountryField.getText());

                prisonService.updatePrison(prison.getId(), updatedPrison);
                LOGGER.info("Prison updated successfully");
                closeDialog();
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error saving prison changes", e);
            // Show error dialog
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Failed to save changes");
            alert.setContentText("An error occurred while saving prison changes: " + e.getMessage());
            alert.showAndWait();
        }
    }

    @FXML
    private void handleCancelEditPrison() {
        LOGGER.info("Canceling prison edit");
        closeDialog();
    }

    private void closeDialog() {
        Stage stage = (Stage) prisonNameField.getScene().getWindow();
        stage.close();
    }
} 