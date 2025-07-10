package org.example.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.example.models.Department;
import org.example.services.DepartmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import javafx.scene.Parent;

@Component
public class EditDepartmentController {
    @FXML private TextField nameField;
    @FXML private ComboBox<String> statusComboBox;
    @FXML private TextArea descriptionArea;

    private Department department;
    private Stage dialogStage;
    private Runnable onSaveCallback;
    private Parent root;

    @Autowired
    private DepartmentService departmentService;

    @FXML
    public void initialize() {
        // Populate status combo box
        statusComboBox.getItems().addAll("ACTIVE", "INACTIVE");
    }

    public void initData(Department department, Runnable onSaveCallback) {
        this.department = department;
        this.onSaveCallback = onSaveCallback;
        nameField.setText(department.getType());
        statusComboBox.setValue(department.getStatus());
        descriptionArea.setText(department.getStatus()); // If you have a description field, use it here
    }

    public void setDialogStage(Stage stage) {
        this.dialogStage = stage;
    }

    @FXML
    private void handleSave() {
        try {
            String name = nameField.getText().trim();
            String status = statusComboBox.getValue();
            // String description = descriptionArea.getText().trim(); // Uncomment if you have a description field

            if (name.isEmpty() || status == null || status.isEmpty()) {
                showError("Validation Error", "Name and status are required.");
                return;
            }

            department.setType(name);
            department.setStatus(status);
            // department.setDescription(description); // Uncomment if you have a description field

            departmentService.updateDepartment(department.getId(), department);
            if (onSaveCallback != null) onSaveCallback.run();
            if (dialogStage != null) dialogStage.close();
        } catch (Exception e) {
            showError("Error", "Failed to save department: " + e.getMessage());
        }
    }

    @FXML
    private void handleCancel() {
        if (dialogStage != null) dialogStage.close();
    }

    private void showError(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    public Parent getRoot() {
        return root;
    }

    public void setRoot(Parent root) {
        this.root = root;
    }
} 