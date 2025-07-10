package org.example.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.control.TextArea;
import javafx.scene.control.DatePicker;
import javafx.stage.Stage;
import org.example.models.Prisoner;
import org.example.models.Crime;
import org.example.services.CrimeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.context.annotation.Scope;
import java.time.LocalDate;

@Component
@Scope("prototype")
public class AddCrimeController {
    @FXML
    private TextField nameField;
    @FXML
    private TextArea descriptionField;
    @FXML
    private TextField sentenceDurationField;
    @FXML
    private DatePicker sentenceStartDatePicker;

    private final CrimeService crimeService;
    private Prisoner prisoner;
    private javafx.scene.Parent root;

    @Autowired
    public AddCrimeController(CrimeService crimeService) {
        this.crimeService = crimeService;
    }

    @FXML
    public void initialize() {
        sentenceStartDatePicker.setValue(LocalDate.now());
    }

    public void setPrisoner(Prisoner prisoner) {
        this.prisoner = prisoner;
    }

    public void setRoot(javafx.scene.Parent root) { this.root = root; }
    public javafx.scene.Parent getRoot() { return root; }

    @FXML
    private void handleSave() {
        try {
            Crime crime = new Crime();
            crime.setName(nameField.getText());
            crime.setDescription(descriptionField.getText());
            crime.setSentenceDuration(Integer.parseInt(sentenceDurationField.getText()));
            crime.setSentenceStartDate(java.sql.Date.valueOf(sentenceStartDatePicker.getValue()));
            crime.setPrisoner(prisoner);

            crimeService.saveCrime(crime);
            closeDialog();
        } catch (Exception e) {
            System.err.println("Error saving crime: " + e.getMessage());
        }
    }

    @FXML
    private void handleCancel() {
        closeDialog();
    }

    private void closeDialog() {
        Stage stage = (Stage) nameField.getScene().getWindow();
        stage.close();
    }
} 