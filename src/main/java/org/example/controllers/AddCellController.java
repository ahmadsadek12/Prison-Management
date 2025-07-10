package org.example.controllers;

import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import org.example.models.Block;
import org.example.models.Cell;
import org.example.services.CellService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AddCellController {
    private static final Logger logger = LoggerFactory.getLogger(AddCellController.class);

    @FXML
    private TextField cellTypeField;

    @FXML
    private Spinner<Integer> capacitySpinner;

    @FXML
    private Label titleLabel;

    @FXML
    private Parent root;

    private final CellService cellService;
    private Block currentBlock;
    private Cell cellToEdit;

    @Autowired
    public AddCellController(CellService cellService) {
        this.cellService = cellService;
    }

    public void initData(Block block, Cell cell) {
        this.currentBlock = block;
        this.cellToEdit = cell;
        if (cell != null) {
            titleLabel.setText("Edit Cell");
            cellTypeField.setText(cell.getType());
            capacitySpinner.getValueFactory().setValue(cell.getCapacity());
        } else {
            titleLabel.setText("Add New Cell");
            cellTypeField.clear();
            capacitySpinner.getValueFactory().setValue(1);
        }
        
        // Add listener to cell type field to automatically adjust capacity for solitary cells
        cellTypeField.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                handleCellTypeChange(newValue);
            }
        });
    }

    private void handleCellTypeChange(String cellType) {
        if (cellType != null && isSolitaryCell(cellType)) {
            // For solitary cells, automatically set capacity to 1 and disable the spinner
            capacitySpinner.getValueFactory().setValue(1);
            capacitySpinner.setDisable(true);
        } else {
            // For other cell types, enable the spinner
            capacitySpinner.setDisable(false);
        }
    }

    private boolean isSolitaryCell(String cellType) {
        if (cellType == null) return false;
        String lowerType = cellType.toLowerCase().trim();
        return lowerType.contains("solitary") || 
               lowerType.contains("isolation") || 
               lowerType.contains("segregation") ||
               lowerType.equals("solitary") ||
               lowerType.equals("isolation") ||
               lowerType.equals("segregation");
    }

    @FXML
    private void handleSaveCell() {
        try {
            String cellType = cellTypeField.getText().trim();
            Integer capacity = capacitySpinner.getValue();

            if (cellType.isEmpty()) {
                showError("Validation Error", "Cell type cannot be empty");
                return;
            }

            // Validate solitary cell capacity
            if (isSolitaryCell(cellType) && capacity != 1) {
                showError("Validation Error", "Solitary cells can only have a capacity of 1");
                return;
            }

            if (cellToEdit != null) {
                // Check if capacity is being reduced and there are prisoners
                if (capacity < cellToEdit.getCapacity() && !cellToEdit.getPrisoners().isEmpty()) {
                    int excessPrisoners = Math.max(0, cellToEdit.getPrisoners().size() - capacity);
                    if (excessPrisoners > 0) {
                        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                        alert.setTitle("Capacity Reduction");
                        alert.setHeaderText("Reducing Cell Capacity");
                        alert.setContentText(String.format(
                            "Reducing capacity from %d to %d will require reallocating %d prisoner(s) to other available cells of the same type. Continue?",
                            cellToEdit.getCapacity(), capacity, excessPrisoners
                        ));
                        
                        alert.showAndWait().ifPresent(response -> {
                            if (response == ButtonType.OK) {
                                saveCell(cellType, capacity);
                            }
                        });
                        return;
                    }
                }
            }
            
            // No confirmation needed, proceed with save
            saveCell(cellType, capacity);
        } catch (Exception e) {
            logger.error("Error saving cell", e);
            showError("Error", "Failed to save cell: " + e.getMessage());
        }
    }

    private void saveCell(String cellType, Integer capacity) {
        try {
            if (cellToEdit != null) {
                // Update existing cell
                cellToEdit.setType(cellType);
                cellToEdit.setCapacity(capacity);
                cellService.updateCell(cellToEdit.getId(), cellToEdit);
                logger.info("Updated cell: {} with capacity: {} in block: {}", cellType, capacity, currentBlock.getId());
            } else {
                // Create new cell
                Cell cell = new Cell();
                cell.setType(cellType);
                cell.setCapacity(capacity);
                currentBlock.addCell(cell);
                cellService.createCell(cell);
                logger.info("Created new cell: {} with capacity: {} in block: {}", cellType, capacity, currentBlock.getId());
            }

            closeDialog();
        } catch (Exception e) {
            logger.error("Error saving cell", e);
            showError("Error", "Failed to save cell: " + e.getMessage());
        }
    }

    @FXML
    private void handleCancelAddCell() {
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