package org.example.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.scene.Parent;
import org.example.models.Block;
import org.example.services.BlockService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.logging.Logger;
import java.util.logging.Level;

@Component
public class EditBlockController {
    private static final Logger LOGGER = Logger.getLogger(EditBlockController.class.getName());

    @FXML private TextField blockTypeField;

    private final BlockService blockService;
    private Block block;
    private Parent root;

    @Autowired
    public EditBlockController(BlockService blockService) {
        this.blockService = blockService;
    }

    public void setRoot(Parent root) {
        this.root = root;
    }

    public Parent getRoot() {
        return root;
    }

    public void setBlock(Block block) {
        this.block = block;
        if (block != null) {
            blockTypeField.setText(block.getType());
        }
    }

    @FXML
    private void handleSaveBlockChanges() {
        try {
            if (block == null) {
                showError("Error", "No block selected for editing");
                return;
            }

            String newType = blockTypeField.getText().trim();
            if (newType.isEmpty()) {
                showError("Validation Error", "Block type cannot be empty");
                return;
            }

            // Update block type
            block.setType(newType);
            blockService.updateBlock(block.getId(), block);

            // Close the dialog
            Stage stage = (Stage) blockTypeField.getScene().getWindow();
            stage.close();

            LOGGER.info("Block updated successfully: " + block.getId());
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error updating block", e);
            showError("Error", "Failed to update block: " + e.getMessage());
        }
    }

    @FXML
    private void handleCancelEditBlock() {
        Stage stage = (Stage) blockTypeField.getScene().getWindow();
        stage.close();
    }

    private void showError(String title, String content) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
} 