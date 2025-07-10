package org.example.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;
import javafx.scene.Parent;
import org.example.models.Room;
import org.example.services.RoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.context.annotation.Scope;
import java.util.logging.Logger;
import java.util.logging.Level;

@Component
@Scope("prototype")
public class EditRoomController {
    private static final Logger LOGGER = Logger.getLogger(EditRoomController.class.getName());

    @FXML private TextField roomTypeField;
    @FXML private TextArea descriptionArea;

    private final RoomService roomService;
    private Room room;
    private Parent root;

    @Autowired
    public EditRoomController(RoomService roomService) {
        this.roomService = roomService;
    }

    public void setRoot(Parent root) {
        this.root = root;
    }

    public Parent getRoot() {
        return root;
    }

    public void setRoom(Room room) {
        this.room = room;
        if (room != null) {
            roomTypeField.setText(room.getType());
            descriptionArea.setText(room.getDescription() != null ? room.getDescription() : "");
        }
    }

    @FXML
    private void handleSaveRoomChanges() {
        LOGGER.info("Saving room changes...");
        try {
            if (room != null) {
                String newType = roomTypeField.getText().trim();
                String newDescription = descriptionArea.getText().trim();
                
                if (newType.isEmpty()) {
                    javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
                    alert.setTitle("Validation Error");
                    alert.setHeaderText("Invalid input");
                    alert.setContentText("Room type cannot be empty");
                    alert.showAndWait();
                    return;
                }

                // Update the existing room object directly instead of creating a new one
                room.setType(newType);
                room.setDescription(newDescription.isEmpty() ? null : newDescription);

                roomService.updateRoom(room.getId(), room);
                LOGGER.info("Room updated successfully: " + room.getId());
                closeDialog();
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error saving room changes", e);
            // Show error dialog like EditPrisonController does
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Failed to save changes");
            alert.setContentText("An error occurred while saving room changes: " + e.getMessage());
            alert.showAndWait();
        }
    }

    @FXML
    private void handleCancelEditRoom() {
        LOGGER.info("Canceling room edit");
        closeDialog();
    }

    private void closeDialog() {
        Stage stage = (Stage) roomTypeField.getScene().getWindow();
        stage.close();
    }
} 