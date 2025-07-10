package org.example.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.stage.Stage;
import javafx.scene.Parent;
import javafx.scene.Scene;
import org.example.models.Block;
import org.example.models.Cell;
import org.example.models.Room;
import org.example.models.Department;
import org.example.models.Contains2;
import org.example.services.BlockService;
import org.example.services.CellService;
import org.example.services.RoomService;
import org.example.services.DepartmentService;
import org.example.config.SpringFXMLLoader;
import org.springframework.stereotype.Component;
import org.springframework.context.annotation.Scope;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.logging.Logger;
import java.util.logging.Level;

@Component
@Scope("prototype")
public class BlockDetailsController {
    private static final Logger LOGGER = Logger.getLogger(BlockDetailsController.class.getName());

    @FXML private Label blockTypeLabel;
    @FXML private Label blockIdLabel;
    @FXML private Label totalCellsLabel;
    @FXML private Label occupiedCellsLabel;
    @FXML private Label totalExpensesLabel;
    @FXML private Label medicalDeptLabel;
    
    @FXML private TextField cellSearchField;
    @FXML private ComboBox<String> cellFilterComboBox;
    @FXML private TableView<Cell> cellsTable;
    @FXML private TableColumn<Cell, Integer> cellIdColumn;
    @FXML private TableColumn<Cell, String> cellTypeColumn;
    @FXML private TableColumn<Cell, Integer> cellCapacityColumn;
    @FXML private TableColumn<Cell, String> cellOccupancyColumn;
    @FXML private TableColumn<Cell, Void> cellActionsColumn;
    
    @FXML private TextField roomSearchField;
    @FXML private ComboBox<String> roomFilterComboBox;
    @FXML private TableView<Room> roomsTable;
    @FXML private TableColumn<Room, Integer> roomIdColumn;
    @FXML private TableColumn<Room, String> roomTypeColumn;
    @FXML private TableColumn<Room, String> roomDescriptionColumn;
    @FXML private TableColumn<Room, String> roomDepartmentColumn;
    @FXML private TableColumn<Room, Void> roomActionsColumn;

    private final BlockService blockService;
    private final CellService cellService;
    private final RoomService roomService;
    private final DepartmentService departmentService;
    private Block block;
    private javafx.scene.Parent root;

    @Autowired
    private SpringFXMLLoader springFXMLLoader;

    public BlockDetailsController(BlockService blockService, CellService cellService, RoomService roomService, DepartmentService departmentService) {
        this.blockService = blockService;
        this.cellService = cellService;
        this.roomService = roomService;
        this.departmentService = departmentService;
    }

    public javafx.scene.Parent getRoot() {
        return root;
    }

    public void setRoot(javafx.scene.Parent root) {
        this.root = root;
    }

    @FXML
    public void initialize() {
        LOGGER.info("Initializing BlockDetailsController");
        
        // Initialize cell columns
        cellIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        cellTypeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
        cellCapacityColumn.setCellValueFactory(new PropertyValueFactory<>("capacity"));
        cellOccupancyColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getPrisoners().size() + "/" + cellData.getValue().getCapacity()));
        
        // Initialize room columns
        roomIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        roomTypeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
        roomDescriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        roomDepartmentColumn.setCellValueFactory(roomData -> {
            Room room = roomData.getValue();
            String departmentType = room.getContains2Relations().stream()
                .map(Contains2::getDepartment)
                .filter(dept -> dept != null)
                .map(Department::getType)
                .findFirst()
                .orElse("N/A");
            return new SimpleStringProperty(departmentType);
        });

        // Add click handler for department column
        roomDepartmentColumn.setCellFactory(col -> new TableCell<Room, String>() {
            {
                setStyle("-fx-cursor: hand; -fx-text-fill: #2196F3; -fx-underline: true;");
                setOnMouseClicked(event -> {
                    Room room = getTableView().getItems().get(getIndex());
                    handleDepartmentDetails(room);
                });
                setOnMouseEntered(event -> setStyle("-fx-cursor: hand; -fx-text-fill: #1976D2; -fx-underline: true;"));
                setOnMouseExited(event -> setStyle("-fx-cursor: hand; -fx-text-fill: #2196F3; -fx-underline: true;"));
            }
            
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? "" : item);
                setGraphic(null);
            }
        });

        // Set up cell actions column
        cellActionsColumn.setCellFactory(col -> new TableCell<Cell, Void>() {
            private final Button deleteButton = new Button("Delete");
            {
                deleteButton.setOnAction(event -> {
                    Cell cell = getTableView().getItems().get(getIndex());
                    handleDeleteCell(cell);
                });
            }
            
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : deleteButton);
            }
        });

        // Set up room actions column
        roomActionsColumn.setCellFactory(col -> new TableCell<Room, Void>() {
            private final Button deleteButton = new Button("Delete");
            {
                deleteButton.setOnAction(event -> {
                    Room room = getTableView().getItems().get(getIndex());
                    handleDeleteRoom(room);
                });
            }
            
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : deleteButton);
            }
        });

        // Add double-click handler for cells
        cellsTable.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                Cell selectedCell = cellsTable.getSelectionModel().getSelectedItem();
                if (selectedCell != null) {
                    handleCellDetails(selectedCell);
                }
            }
        });
        
        // Add double-click handler for rooms
        roomsTable.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                Room selectedRoom = roomsTable.getSelectionModel().getSelectedItem();
                if (selectedRoom != null) {
                    handleRoomDetails(selectedRoom);
                }
            }
        });

        // Initialize filter combo boxes
        cellFilterComboBox.setItems(FXCollections.observableArrayList("All", "Available", "Full"));
        roomFilterComboBox.setItems(FXCollections.observableArrayList("All", "Medical", "Storage", "Office"));
        cellFilterComboBox.setValue("All");
        roomFilterComboBox.setValue("All");

        // Set up search fields
        setupSearchFields();
    }

    private void setupSearchFields() {
        cellSearchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filterCells(newValue);
        });

        roomSearchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filterRooms(newValue);
        });

        cellFilterComboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            filterCells(cellSearchField.getText());
        });

        roomFilterComboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            filterRooms(roomSearchField.getText());
        });
    }

    private void filterCells(String searchText) {
        if (block == null) return;
        
        cellsTable.getItems().setAll(block.getCells().stream()
            .filter(cell -> {
                boolean matchesSearch = searchText == null || searchText.isEmpty() ||
                    cell.getType().toLowerCase().contains(searchText.toLowerCase());
                
                String filter = cellFilterComboBox.getValue();
                boolean matchesFilter = filter.equals("All") ||
                    (filter.equals("Available") && cell.getPrisoners().size() < cell.getCapacity()) ||
                    (filter.equals("Full") && cell.getPrisoners().size() >= cell.getCapacity());
                
                return matchesSearch && matchesFilter;
            })
            .toList());
    }

    private void filterRooms(String searchText) {
        if (block == null) return;
        
        roomsTable.getItems().setAll(block.getRooms().stream()
            .filter(room -> {
                boolean matchesSearch = searchText == null || searchText.isEmpty() ||
                    room.getType().toLowerCase().contains(searchText.toLowerCase()) ||
                    (room.getDescription() != null && room.getDescription().toLowerCase().contains(searchText.toLowerCase()));
                
                String filter = roomFilterComboBox.getValue();
                boolean matchesFilter = filter.equals("All") ||
                    room.getType().equalsIgnoreCase(filter);
                
                return matchesSearch && matchesFilter;
            })
            .toList());
    }

    public void initData(Block block) {
        this.block = block;
        updateUI();
    }

    private void updateUI() {
        if (block != null) {
            blockTypeLabel.setText(block.getType());
            blockIdLabel.setText(String.valueOf(block.getId()));
            totalCellsLabel.setText(String.valueOf(block.getNumberOfCells()));
            occupiedCellsLabel.setText(String.valueOf(block.getNumberOfPrisoners()));
            totalExpensesLabel.setText(String.format("%.2f", block.getTotalExpenses()));
            medicalDeptLabel.setText(block.getMedicalDepartment() ? "Yes" : "No");

            // Update tables
            cellsTable.setItems(FXCollections.observableArrayList(block.getCells()));
            roomsTable.setItems(FXCollections.observableArrayList(block.getRooms()));
        }
    }

    @FXML
    private void handleClose() {
        try {
            // Close current window
            Stage currentStage = (Stage) blockTypeLabel.getScene().getWindow();
            currentStage.close();

            // Load and show dashboard
            Parent dashboard = springFXMLLoader.load("/fxml/dashboard.fxml");
            Stage newStage = new Stage();
            newStage.setTitle("Prison Dashboard");
            newStage.setScene(new Scene(dashboard));
            newStage.show();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error opening dashboard", e);
            showError("Error", "Failed to open dashboard: " + e.getMessage());
        }
    }

    @FXML
    private void handleAddCell() {
        try {
            AddCellController controller = springFXMLLoader.loadAndGetController("/fxml/add-cell.fxml", AddCellController.class);
            controller.initData(block, null);

            Stage stage = new Stage();
            stage.setTitle("Add New Cell");
            stage.setScene(new Scene(controller.getRoot()));
            stage.setResizable(false);

            // Refresh the cells table when the dialog is closed
            stage.setOnHidden(event -> {
                updateUI();
                filterCells(cellSearchField.getText());
            });

            stage.show();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error opening add cell dialog", e);
            showError("Error", "Failed to open add cell dialog: " + e.getMessage());
        }
    }

    @FXML
    private void handleAddRoom() {
        try {
            AddRoomController controller = springFXMLLoader.loadAndGetController("/fxml/add-room.fxml", AddRoomController.class);
            controller.setBlock(block);
            controller.setOnSaveCallback(() -> {
                block = blockService.getBlockByIdWithRelations(block.getId());
                updateUI();
                filterRooms(roomSearchField.getText());
            });

            Stage stage = new Stage();
            stage.setTitle("Add New Room");
            stage.setScene(new Scene(controller.getRoot()));
            stage.setResizable(false);
            stage.show();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error opening add room dialog", e);
            showError("Error", "Failed to open add room dialog: " + e.getMessage());
        }
    }

    @FXML
    private void handleEditBlock() {
        try {
            EditBlockController controller = springFXMLLoader.loadAndGetController("/fxml/edit-block.fxml", EditBlockController.class);
            controller.setBlock(block);

            Stage stage = new Stage();
            stage.setTitle("Edit Block");
            stage.setScene(new Scene(controller.getRoot()));
            stage.setResizable(false);

            // Refresh the block data when the dialog is closed
            stage.setOnHidden(event -> {
                block = blockService.getBlockByIdWithRelations(block.getId());
                updateUI();
            });

            stage.show();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error opening edit block dialog", e);
            showError("Error", "Failed to open edit block dialog: " + e.getMessage());
        }
    }

    @FXML
    private void handleGenerateReport() {
        LOGGER.info("Generate report button clicked");
        // TODO: Implement report generation
    }

    @FXML
    private void handleDeleteBlock() {
        if (block == null) {
            showError("Error", "No block selected for deletion");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Block");
        alert.setHeaderText("Delete Block");
        alert.setContentText("Are you sure you want to delete this block? This action cannot be undone.");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    blockService.deleteBlock(block.getId());
                    LOGGER.info("Block deleted successfully: " + block.getId());
                    handleClose(); // Close the details window
                } catch (Exception e) {
                    LOGGER.log(Level.SEVERE, "Error deleting block: " + block.getId(), e);
                    showError("Error", "Failed to delete block: " + e.getMessage());
                }
            }
        });
    }

    @FXML
    private void handleDeleteCell(Cell cell) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Cell");
        alert.setHeaderText("Delete Cell");
        alert.setContentText("Are you sure you want to delete this cell? This action cannot be undone.");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    cellService.deleteCell(cell.getId());
                    LOGGER.info("Cell deleted successfully: " + cell.getId());
                    // Refresh the block data
                    block = blockService.getBlockByIdWithRelations(block.getId());
                    updateUI();
                } catch (Exception e) {
                    LOGGER.log(Level.SEVERE, "Error deleting cell: " + cell.getId(), e);
                    showError("Error", "Failed to delete cell: " + e.getMessage());
                }
            }
        });
    }

    @FXML
    private void handleDeleteRoom(Room room) {
        if (room == null) {
            showError("Error", "No room selected for deletion");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Room");
        alert.setHeaderText("Delete Room");
        alert.setContentText("Are you sure you want to delete this room? This action cannot be undone.");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    // Delete the room (RoomService will handle all cleanup)
                    roomService.deleteRoom(room.getId());
                    LOGGER.info("Room deleted successfully: " + room.getId());
                    // Refresh the block data
                    block = blockService.getBlockByIdWithRelations(block.getId());
                    updateUI();
                    filterRooms(roomSearchField.getText());
                } catch (Exception e) {
                    LOGGER.log(Level.SEVERE, "Error deleting room: " + room.getId(), e);
                    showError("Error", "Failed to delete room: " + e.getMessage());
                }
            }
        });
    }

    private void showError(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void handleDepartmentDetails(Room room) {
        try {
            Department department = room.getContains2Relations().stream()
                .map(Contains2::getDepartment)
                .findFirst()
                .orElse(null);

            if (department == null) {
                showError("Error", "No department associated with this room");
                return;
            }

            // Get the department with all relationships eagerly loaded
            Department departmentWithRelations = departmentService.getDepartmentByIdWithRelations(department.getId());

            DepartmentDetailsController controller = springFXMLLoader.loadAndGetController("/fxml/department-details.fxml", DepartmentDetailsController.class);
            controller.initData(departmentWithRelations);

            Stage stage = new Stage();
            stage.setTitle("Department Details");
            stage.setScene(new Scene(controller.getRoot()));
            stage.show();
            
            // Close the block details window
            Stage blockStage = (Stage) blockTypeLabel.getScene().getWindow();
            blockStage.close();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error opening department details", e);
            showError("Error", "Failed to open department details: " + e.getMessage());
        }
    }

    private void handleCellDetails(Cell cell) {
        try {
            CellDetailsController controller = springFXMLLoader.loadAndGetController("/fxml/cell-details.fxml", CellDetailsController.class);
            controller.initData(cell);

            Stage stage = new Stage();
            stage.setTitle("Cell Details");
            stage.setScene(new Scene(controller.getRoot()));
            stage.show();
            
            // Close the block details window
            Stage blockStage = (Stage) blockTypeLabel.getScene().getWindow();
            blockStage.close();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error opening cell details", e);
            showError("Error", "Failed to open cell details: " + e.getMessage());
        }
    }

    private void handleRoomDetails(Room room) {
        try {
            // Try to load the room with all relations, but use safe method if there are data integrity issues
            Room roomWithRelations;
            try {
                roomWithRelations = roomService.getRoomByIdWithRelations(room.getId());
            } catch (Exception e) {
                LOGGER.warning("Failed to load room with relations, using safe method: " + e.getMessage());
                // Use safe method that doesn't try to fetch invalid departments
                roomWithRelations = roomService.getRoomByIdWithRelationsSafe(room.getId());
            }
            
            RoomDetailsController controller = springFXMLLoader.loadAndGetController("/fxml/room-details.fxml", RoomDetailsController.class);
            controller.initData(roomWithRelations);

            Stage stage = new Stage();
            stage.setTitle("Room Details");
            stage.setScene(new Scene(controller.getRoot()));
            stage.setResizable(false);
            stage.showAndWait(); // Make it modal

            // Refresh block data after room details window closes
            try {
                LOGGER.info("Refreshing block data after room details closed");
                
                // Refresh the block data from database
                block = blockService.getBlockByIdWithRelations(block.getId());
                
                updateUI();
                filterRooms(roomSearchField.getText());
                
                // Force table refresh
                roomsTable.getItems().clear();
                roomsTable.getItems().addAll(block.getRooms());
                
                LOGGER.info("Block data refreshed successfully");
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Error refreshing block data after room details closed", e);
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error opening room details", e);
            showError("Error", "Failed to open room details: " + e.getMessage());
        }
    }
} 