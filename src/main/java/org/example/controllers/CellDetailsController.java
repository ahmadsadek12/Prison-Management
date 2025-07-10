package org.example.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.Parent;
import javafx.stage.Stage;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import org.example.models.Cell;
import org.example.models.Prisoner;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;
import org.example.config.SpringFXMLLoader;
import javafx.scene.Scene;
import java.time.format.DateTimeFormatter;
import org.example.services.CellService;
import org.example.models.Block;
import org.example.services.BlockService;
import org.example.services.PrisonerService;

@Component
public class CellDetailsController {
    @FXML private Label cellIdLabel;
    @FXML private Label cellTypeLabel;
    @FXML private Label cellCapacityLabel;
    @FXML private Label cellOccupancyLabel;
    @FXML private Label cellBlockLabel;
    
    @FXML private TableView<Prisoner> prisonersTable;
    @FXML private TableColumn<Prisoner, Integer> prisonerIdColumn;
    @FXML private TableColumn<Prisoner, String> prisonerNameColumn;
    @FXML private TableColumn<Prisoner, String> prisonerGenderColumn;
    @FXML private TableColumn<Prisoner, Integer> prisonerAgeColumn;
    @FXML private TableColumn<Prisoner, String> prisonerSentenceStartColumn;
    @FXML private TableColumn<Prisoner, String> prisonerSentenceEndColumn;
    @FXML private TableColumn<Prisoner, Void> prisonerActionsColumn;
    
    @FXML private Button addPrisonerButton;
    
    @Autowired
    private SpringFXMLLoader springFXMLLoader;
    
    @Autowired
    private CellService cellService;
    
    @Autowired
    private BlockService blockService;
    
    @Autowired
    private PrisonerService prisonerService;
    
    private Cell cell;
    private Block block;
    private Parent root;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public Parent getRoot() {
        return root;
    }

    public void setRoot(Parent root) {
        this.root = root;
    }

    @FXML
    public void initialize() {
        // Initialize prisoner columns
        prisonerIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        prisonerNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        prisonerGenderColumn.setCellValueFactory(new PropertyValueFactory<>("gender"));
        prisonerAgeColumn.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getAge()).asObject());
        prisonerSentenceStartColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getSentenceStart().format(DATE_FORMATTER)));
        prisonerSentenceEndColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getSentenceEnd().format(DATE_FORMATTER)));

        // Set up prisoner actions column
        prisonerActionsColumn.setCellFactory(col -> new TableCell<Prisoner, Void>() {
            private final Button deleteButton = new Button("Delete");
            {
                deleteButton.setOnAction(event -> {
                    Prisoner prisoner = getTableView().getItems().get(getIndex());
                    handleDeletePrisoner(prisoner);
                });
            }
            
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : deleteButton);
            }
        });

        // Add double-click handler for prisoners table
        prisonersTable.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                Prisoner selectedPrisoner = prisonersTable.getSelectionModel().getSelectedItem();
                if (selectedPrisoner != null) {
                    handlePrisonerDetails(selectedPrisoner);
                }
            }
        });
    }

    public void initData(Cell cell) {
        this.cell = cell;
        this.block = cell.getBlock();
        updateUI();
    }

    private void updateUI() {
        if (cell != null) {
            cellIdLabel.setText(String.valueOf(cell.getId()));
            cellTypeLabel.setText(cell.getType());
            cellCapacityLabel.setText(String.valueOf(cell.getCapacity()));
            cellOccupancyLabel.setText(cell.getPrisoners().size() + "/" + cell.getCapacity());
            cellBlockLabel.setText(cell.getBlock() != null ? cell.getBlock().getType() : "N/A");
            
            // Update prisoners table
            prisonersTable.setItems(FXCollections.observableArrayList(cell.getPrisoners()));
            
            // Update Add Prisoner button state
            addPrisonerButton.setDisable(cell.getPrisoners().size() >= cell.getCapacity());
        }
    }

    @FXML
    private void handleDeletePrisoner(Prisoner prisoner) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Prisoner");
        alert.setHeaderText("Delete Prisoner");
        alert.setContentText("Are you sure you want to delete this prisoner? This action cannot be undone.");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    // Delete prisoner from database
                    prisonerService.deletePrisoner(prisoner.getId());
                    
                    // Refresh cell data to update UI
                    cellService.getCellById(cell.getId()).ifPresent(c -> {
                        cell = c;
                        updateUI();
                    });
                } catch (Exception e) {
                    showError("Error", "Failed to delete prisoner", e.getMessage());
                }
            }
        });
    }

    @FXML
    private void handleClose() {
        Stage stage = (Stage) cellIdLabel.getScene().getWindow();
        stage.close();
    }

    @FXML
    private void handleBack() {
        try {
            // Close current window
            Stage currentStage = (Stage) cellIdLabel.getScene().getWindow();
            currentStage.close();

            // Reopen the block details page
            if (block != null) {
                BlockDetailsController controller = springFXMLLoader.loadAndGetController("/fxml/block-details.fxml", BlockDetailsController.class);
                controller.initData(block);

                Stage stage = new Stage();
                stage.setTitle("Block Details");
                stage.setScene(new Scene(controller.getRoot()));
                stage.show();
            } else {
                // Fallback to dashboard if block is not available
                Parent dashboard = springFXMLLoader.load("/fxml/dashboard.fxml");
                Stage newStage = new Stage();
                newStage.setTitle("Prison Dashboard");
                newStage.setScene(new Scene(dashboard));
                newStage.show();
            }
        } catch (Exception e) {
            showError("Error", "Failed to return to block details", e.getMessage());
        }
    }

    private void showError(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    @FXML
    private void handleEditCell() {
        try {
            // Load the add-cell form
            AddCellController controller = springFXMLLoader.loadAndGetController("/fxml/add-cell.fxml", AddCellController.class);
            
            // Initialize with existing cell data
            controller.initData(cell.getBlock(), cell);
            
            // Create and show the stage
            Stage stage = new Stage();
            stage.setTitle("Edit Cell");
            stage.setScene(new Scene(controller.getRoot()));
            
            // Refresh the cell data when the dialog is closed
            stage.setOnHidden(event -> {
                cellService.getCellById(cell.getId()).ifPresent(c -> {
                    cell = c;
                    updateUI();
                });
            });
            
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleAddPrisoner() {
        try {
            AddPrisonerController controller = springFXMLLoader.loadAndGetController("/fxml/add-prisoner.fxml", AddPrisonerController.class);
            controller.initData(cell);
            
            Stage stage = new Stage();
            stage.setTitle("Add New Prisoner");
            stage.setScene(new Scene(controller.getRoot()));
            
            // Refresh the cell data when the dialog is closed
            stage.setOnHidden(event -> {
                cellService.getCellById(cell.getId()).ifPresent(c -> {
                    cell = c;
                    updateUI();
                });
            });
            
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleRefresh() {
        cellService.getCellById(cell.getId()).ifPresent(c -> {
            cell = c;
            updateUI();
        });
    }

    private void handlePrisonerDetails(Prisoner prisoner) {
        try {
            System.out.println("Opening prisoner details for: " + prisoner.getName());
            PrisonerDetailsController controller = springFXMLLoader.loadAndGetController("/fxml/prisoner-details.fxml", PrisonerDetailsController.class);
            controller.setPrisoner(prisoner);
            if (controller.getRoot() == null) {
                System.err.println("[ERROR] PrisonerDetailsController.getRoot() is null!");
                showError("Error", "Failed to open prisoner details", "Root node is null");
                return;
            }
            Stage stage = new Stage();
            stage.setTitle("Prisoner Details");
            stage.setScene(new Scene(controller.getRoot()));
            stage.show();
        } catch (Exception e) {
            System.err.println("Error opening prisoner details: " + e.getMessage());
            e.printStackTrace();
            showError("Error", "Failed to open prisoner details", e.getMessage());
        }
    }
} 