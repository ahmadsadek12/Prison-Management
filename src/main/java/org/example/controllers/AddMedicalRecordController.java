package org.example.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.control.TextArea;
import javafx.scene.control.ComboBox;
import javafx.stage.Stage;
import org.example.models.Prisoner;
import org.example.models.MedicalRecord;
import org.example.services.MedicalRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.context.annotation.Scope;

@Component
@Scope("prototype")
public class AddMedicalRecordController {
    @FXML
    private TextField diagnosisField;
    @FXML
    private TextArea treatmentField;
    @FXML
    private TextArea doctorNotesField;
    @FXML
    private ComboBox<String> statusComboBox;

    private final MedicalRecordService medicalRecordService;
    private Prisoner prisoner;
    private MedicalRecord editingRecord;
    private javafx.scene.Parent root;

    @Autowired
    public AddMedicalRecordController(MedicalRecordService medicalRecordService) {
        this.medicalRecordService = medicalRecordService;
    }

    @FXML
    public void initialize() {
        statusComboBox.getItems().addAll(
            "ACTIVE",
            "ARCHIVED",
            "PENDING",
            "COMPLETED"
        );
        statusComboBox.setValue("ACTIVE");
    }

    public void setPrisoner(Prisoner prisoner) {
        this.prisoner = prisoner;
    }

    public void setMedicalRecord(MedicalRecord record) {
        this.editingRecord = record;
        if (record != null) {
            diagnosisField.setText(record.getDiagnosis());
            treatmentField.setText(record.getTreatment());
            doctorNotesField.setText(record.getDoctorNotes());
            statusComboBox.setValue(record.getStatus());
        }
    }

    @FXML
    private void handleSave() {
        try {
            System.out.println("=== Medical Record Save Started ===");
            
            if (editingRecord == null) {
                // Create new record
                System.out.println("Creating new medical record...");
                MedicalRecord record = new MedicalRecord();
                record.setDiagnosis(diagnosisField.getText());
                record.setTreatment(treatmentField.getText());
                record.setDoctorNotes(doctorNotesField.getText());
                record.setStatus(statusComboBox.getValue());
                record.setPrisoner(prisoner);
                
                System.out.println("Creating new medical record for prisoner: " + prisoner.getName());
                MedicalRecord savedRecord = medicalRecordService.createMedicalRecord(record);
                System.out.println("Medical record created with ID: " + savedRecord.getId());
            } else {
                // Update existing record
                System.out.println("Updating existing medical record...");
                System.out.println("Original record ID: " + editingRecord.getId());
                System.out.println("Original diagnosis: " + editingRecord.getDiagnosis());
                
                // Update the existing record directly to preserve the @DBRef
                editingRecord.setDiagnosis(diagnosisField.getText());
                editingRecord.setTreatment(treatmentField.getText());
                editingRecord.setDoctorNotes(doctorNotesField.getText());
                editingRecord.setStatus(statusComboBox.getValue());
                // Keep the existing prisoner reference (don't change it)
                
                System.out.println("New diagnosis: " + diagnosisField.getText());
                System.out.println("New status: " + statusComboBox.getValue());
                if (editingRecord.getPrisoner() != null) {
                    System.out.println("Prisoner reference preserved: " + editingRecord.getPrisoner().getName());
                } else {
                    System.out.println("WARNING: Prisoner reference is null, using current prisoner");
                    editingRecord.setPrisoner(prisoner);
                }
                
                System.out.println("Updating medical record with ID: " + editingRecord.getId());
                MedicalRecord savedRecord = medicalRecordService.updateMedicalRecord(editingRecord.getId(), editingRecord);
                System.out.println("Medical record updated successfully: " + savedRecord.getId());
            }
            
            System.out.println("=== Medical Record Save Completed ===");
            closeDialog();
        } catch (Exception e) {
            System.err.println("Error saving medical record: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleCancel() {
        closeDialog();
    }

    private void closeDialog() {
        Stage stage = (Stage) diagnosisField.getScene().getWindow();
        stage.close();
    }

    public void setRoot(javafx.scene.Parent root) { this.root = root; }
    public javafx.scene.Parent getRoot() { return root; }
} 