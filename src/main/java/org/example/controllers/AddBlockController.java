package org.example.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.Stage;
import org.example.models.Block;
import org.example.models.Prison;
import org.example.services.BlockService;
import org.example.services.PrisonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.logging.Logger;
import java.util.logging.Level;

@Component
public class AddBlockController {
    private static final Logger LOGGER = Logger.getLogger(AddBlockController.class.getName());

    @FXML
    private TextField blockTypeField;

    @Autowired
    private BlockService blockService;

    @Autowired
    private PrisonService prisonService;

    @FXML
    private void handleSaveBlock() {
        try {
            String type = blockTypeField.getText().trim();
            if (type.isEmpty()) {
                showError("Validation Error", "Block type cannot be empty");
                return;
            }

            // Get the first prison (assuming we're working with a single prison for now)
            Prison currentPrison = prisonService.getAllPrisons().get(0);
            if (currentPrison == null) {
                showError("Error", "No prison found in the system");
                return;
            }

            // Create and save the new block
            Block block = new Block(type, currentPrison);
            blockService.createBlock(block);
            
            LOGGER.info("Block created successfully: " + type);
            showSuccess("Success", "Block added successfully");
            
            // Close the dialog
            Stage stage = (Stage) blockTypeField.getScene().getWindow();
            stage.close();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error creating block", e);
            showError("Error", "Failed to create block: " + e.getMessage());
        }
    }

    @FXML
    private void handleCancelBlock() {
        Stage stage = (Stage) blockTypeField.getScene().getWindow();
        stage.close();
    }

    private void showError(String title, String content) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void showSuccess(String title, String content) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
} 