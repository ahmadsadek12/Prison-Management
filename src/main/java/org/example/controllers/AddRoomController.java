package org.example.controllers;

import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.example.models.Block;
import org.example.models.Room;
import org.example.models.Department;
import org.example.services.RoomService;
import org.example.services.DepartmentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import javafx.util.StringConverter;

import java.util.List;
import java.util.Optional;

@Component
public class AddRoomController {
    private static final Logger logger = LoggerFactory.getLogger(AddRoomController.class);

    @FXML private TextField roomTypeField;
    @FXML private TextArea descriptionArea;
    @FXML private ComboBox<Department> departmentComboBox;
    @FXML private Parent root;

    private final RoomService roomService;
    private final DepartmentService departmentService;
    private Block currentBlock;
    private Runnable onSaveCallback;

    @Autowired
    public AddRoomController(RoomService roomService, DepartmentService departmentService) {
        this.roomService = roomService;
        this.departmentService = departmentService;
    }

    public void setBlock(Block block) {
        this.currentBlock = block;
        loadDepartments();
    }

    public void setOnSaveCallback(Runnable callback) {
        this.onSaveCallback = callback;
    }

    private void loadDepartments() {
        List<Department> departments = departmentService.getAllDepartments();
        departmentComboBox.setItems(javafx.collections.FXCollections.observableArrayList(departments));
        departmentComboBox.setConverter(new StringConverter<Department>() {
            @Override
            public String toString(Department department) {
                return department == null ? "" : department.getType();
            }

            @Override
            public Department fromString(String string) {
                return null; // Not needed for ComboBox
            }
        });
        if (!departments.isEmpty()) {
            departmentComboBox.setValue(departments.get(0));
        }
    }

    @FXML
    private void handleAddDepartment() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Add Department");
        dialog.setHeaderText("Add New Department");
        dialog.setContentText("Enter department type:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(type -> {
            try {
                Department department = new Department(type);
                department = departmentService.createDepartment(department);
                loadDepartments();
                departmentComboBox.setValue(department);
            } catch (Exception e) {
                showError("Error", "Failed to create department: " + e.getMessage());
            }
        });
    }

    @FXML
    private void handleSaveRoom() {
        try {
            String roomType = roomTypeField.getText().trim();
            String description = descriptionArea.getText().trim();
            Department selectedDepartment = departmentComboBox.getValue();

            if (roomType.isEmpty()) {
                showError("Validation Error", "Room type cannot be empty");
                return;
            }

            if (selectedDepartment == null) {
                showError("Validation Error", "Please select a department");
                return;
            }

            if (currentBlock == null) {
                showError("Validation Error", "No block selected for the room");
                return;
            }

            // Create and save the room
            Room room = new Room(roomType, description);
            room = roomService.createRoom(room);
            
            // Assign the room to the block and department
            roomService.assignToBlock(room.getId(), currentBlock, selectedDepartment);
            
            logger.info("Created new room: {} in block: {} and department: {}", 
                roomType, currentBlock.getId(), selectedDepartment.getId());

            if (onSaveCallback != null) {
                onSaveCallback.run();
            }
            closeDialog();
        } catch (Exception e) {
            logger.error("Error saving room", e);
            showError("Error", "Failed to save room: " + e.getMessage());
        }
    }

    @FXML
    private void handleCancelAddRoom() {
        closeDialog();
    }

    private void closeDialog() {
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

    public Parent getRoot() {
        return root;
    }

    public void setRoot(Parent root) {
        this.root = root;
    }
} 