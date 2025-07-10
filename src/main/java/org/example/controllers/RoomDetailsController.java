package org.example.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.Parent;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.example.models.Room;
import org.example.models.Department;
import org.example.models.Contains2;
import org.example.models.Equipment;
import org.example.models.EquipmentMaintenanceLog;
import org.example.services.EquipmentService;
import org.example.services.RoomService;
import org.example.services.EquipmentMaintenanceService;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;
import org.example.config.SpringFXMLLoader;
import java.util.List;
import java.util.Optional;
import javafx.scene.layout.HBox;
import org.springframework.context.ApplicationContext;
import javafx.stage.Modality;
import java.util.logging.Logger;
import java.util.logging.Level;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Pos;
import javafx.stage.Popup;

import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;

@Component
public class RoomDetailsController {
    @FXML private Label roomIdLabel;
    @FXML private Label roomTypeLabel;
    @FXML private Label roomDescriptionLabel;
    @FXML private Label roomDepartmentLabel;
    @FXML private Label roomBlocksLabel;
    
    // Equipment table
    @FXML private TableView<Equipment> equipmentTable;
    @FXML private TableColumn<Equipment, Integer> equipmentIdColumn;
    @FXML private TableColumn<Equipment, String> equipmentNameColumn;
    @FXML private TableColumn<Equipment, String> equipmentDescriptionColumn;
    @FXML private TableColumn<Equipment, Integer> equipmentAmountColumn;
    @FXML private TableColumn<Equipment, Void> equipmentActionsColumn;
    
    @Autowired
    private SpringFXMLLoader springFXMLLoader;
    
    @Autowired
    private EquipmentService equipmentService;
    
    @Autowired
    private RoomService roomService;
    
    @Autowired
    private ApplicationContext applicationContext;
    
    @Autowired
    private EquipmentMaintenanceService equipmentMaintenanceService;
    
    private Room room;
    private Parent root;
    private Runnable onUpdateCallback;
    
    // Static variable to track current popup
    private static Popup currentPopup = null;

    @Autowired
    public RoomDetailsController(EquipmentService equipmentService, RoomService roomService, 
                               ApplicationContext applicationContext, EquipmentMaintenanceService equipmentMaintenanceService) {
        this.equipmentService = equipmentService;
        this.roomService = roomService;
        this.applicationContext = applicationContext;
        this.equipmentMaintenanceService = equipmentMaintenanceService;
    }

    public Parent getRoot() {
        return root;
    }

    public void setRoot(Parent root) {
        this.root = root;
    }

    @FXML
    public void initialize() {
        setupEquipmentTable();
    }

    private void setupEquipmentTable() {
        // Initialize equipment table columns
        equipmentIdColumn.setCellValueFactory(cellData -> {
            Equipment equipment = cellData.getValue();
            return new SimpleIntegerProperty(equipment.getRoom().getId()).asObject();
        });
        equipmentNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        equipmentDescriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        equipmentAmountColumn.setCellValueFactory(new PropertyValueFactory<>("amount"));
        
        // Set up actions column
        equipmentActionsColumn.setCellFactory(col -> new TableCell<Equipment, Void>() {
            private final Button editButton = new Button("Edit");
            private final Button removeButton = new Button("Remove");
            private final HBox actionBox = new HBox(5, editButton, removeButton);
            {
                editButton.setOnAction(event -> {
                    Equipment equipment = getTableView().getItems().get(getIndex());
                    handleEditEquipment(equipment);
                });
                removeButton.setOnAction(event -> {
                    Equipment equipment = getTableView().getItems().get(getIndex());
                    handleRemoveEquipment(equipment);
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : actionBox);
            }
        });
        
        // Make equipment rows clickable to show maintenance information
        equipmentTable.setRowFactory(tv -> {
            TableRow<Equipment> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (!row.isEmpty()) {
                    Equipment equipment = row.getItem();
                    showEquipmentMaintenancePopup(equipment);
                }
            });
            return row;
        });
    }

    public void initData(Room room) {
        this.room = room;
        updateUI();
    }

    private void updateUI() {
        if (room != null) {
            roomIdLabel.setText(String.valueOf(room.getId()));
            roomTypeLabel.setText(room.getType());
            roomDescriptionLabel.setText(room.getDescription() != null ? room.getDescription() : "N/A");
            
            // Get department from Contains2 relation
            try {
            String departmentType = room.getContains2Relations().stream()
                .map(Contains2::getDepartment)
                    .filter(dept -> dept != null && dept.getId() != null && dept.getId() > 0)
                .map(Department::getType)
                .findFirst()
                .orElse("N/A");
            roomDepartmentLabel.setText(departmentType);
            } catch (Exception e) {
                roomDepartmentLabel.setText("N/A");
            }
            
            // Get blocks - handle lazy loading safely
            try {
            String blocks = room.getBlocks().stream()
                    .map(block -> {
                        try {
                            return block.getType();
                        } catch (Exception e) {
                            return "Unknown";
                        }
                    })
                .reduce((a, b) -> a + ", " + b)
                .orElse("N/A");
            roomBlocksLabel.setText(blocks);
            } catch (Exception e) {
                roomBlocksLabel.setText("N/A");
            }
            
            // Load equipment data
            loadEquipment();
        }
    }

    private void loadEquipment() {
        try {
            // Load equipment using the safe method that excludes invalid department references
            List<Equipment> validEquipment = roomService.getValidEquipmentByRoomId(room.getId());
            
            // Clear the table first to ensure proper refresh
            equipmentTable.getItems().clear();
            
            // Add the new equipment items
            ObservableList<Equipment> observableEquipment = FXCollections.observableArrayList(validEquipment);
            equipmentTable.setItems(observableEquipment);
            
            // Force table refresh
            equipmentTable.refresh();
        } catch (Exception e) {
            System.err.println("Error loading equipment: " + e.getMessage());
            showError("Error", "Failed to load equipment", e.getMessage());
        }
    }

    @FXML
    private void handleAddEquipment() {
        try {
            Dialog<Equipment> dialog = new Dialog<>();
            dialog.setTitle("Add Equipment");
            dialog.setHeaderText("Add new equipment to the room");

            ButtonType addButtonType = new ButtonType("Add", ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(addButtonType, ButtonType.CANCEL);

            VBox content = new VBox(10);
            TextField nameField = new TextField();
            nameField.setPromptText("Equipment Name");
            TextField descriptionField = new TextField();
            descriptionField.setPromptText("Description");
            TextField amountField = new TextField();
            amountField.setPromptText("Amount");

            content.getChildren().addAll(
                new Label("Name:"), nameField,
                new Label("Description:"), descriptionField,
                new Label("Amount:"), amountField
            );
            dialog.getDialogPane().setContent(content);
            nameField.requestFocus();

            dialog.setResultConverter(dialogButton -> {
                if (dialogButton == addButtonType) {
                    try {
                        String name = nameField.getText().trim();
                        String description = descriptionField.getText().trim();
                        int amount = Integer.parseInt(amountField.getText().trim());
                        if (name.isEmpty() || description.isEmpty() || amount <= 0) {
                            showError("Error", "Invalid input", "Please fill all fields with valid values.");
                            return null;
                        }
                        Equipment newEquipment = new Equipment(name, amount, description, room);
                        return newEquipment;
                    } catch (NumberFormatException e) {
                        showError("Error", "Invalid amount", "Please enter a valid number for amount.");
                        return null;
                    }
                }
                return null;
            });

            Optional<Equipment> result = dialog.showAndWait();
            result.ifPresent(equipment -> {
                try {
                    equipmentService.createEquipment(equipment); // Persist to DB
                    loadEquipment();
                } catch (Exception e) {
                    showError("Error", "Failed to add equipment", e.getMessage());
                }
            });
        } catch (Exception e) {
            showError("Error", "Failed to open add equipment dialog", e.getMessage());
        }
    }

    @FXML
    private void handleRemoveEquipment(Equipment equipment) {
        try {
            Dialog<Integer> dialog = new Dialog<>();
            dialog.setTitle("Remove Equipment");
            dialog.setHeaderText("How much equipment do you want to remove?");

            ButtonType removeButtonType = new ButtonType("Remove", ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(removeButtonType, ButtonType.CANCEL);

            VBox content = new VBox(10);
            Label infoLabel = new Label("Equipment: " + equipment.getName() + "\nCurrent amount: " + equipment.getAmount());
            TextField amountField = new TextField();
            amountField.setPromptText("Amount to remove");
            CheckBox removeAllCheckBox = new CheckBox("Remove All");

            content.getChildren().addAll(infoLabel, new Label("Amount to remove:"), amountField, removeAllCheckBox);
            dialog.getDialogPane().setContent(content);
            amountField.requestFocus();

            removeAllCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> {
                amountField.setDisable(newVal);
            });

            dialog.setResultConverter(dialogButton -> {
                if (dialogButton == removeButtonType) {
                    if (removeAllCheckBox.isSelected()) {
                        return equipment.getAmount(); // Remove all
                    }
                    try {
                        int amountToRemove = Integer.parseInt(amountField.getText().trim());
                        if (amountToRemove <= 0) {
                            showError("Error", "Invalid amount", "Please enter a positive number.");
                            return null;
                        }
                        if (amountToRemove > equipment.getAmount()) {
                            showError("Error", "Invalid amount", "Cannot remove more than available amount.");
                            return null;
                        }
                        return amountToRemove;
                    } catch (NumberFormatException e) {
                        showError("Error", "Invalid amount", "Please enter a valid number.");
                        return null;
                    }
                }
                return null;
            });

            Optional<Integer> result = dialog.showAndWait();
            result.ifPresent(amountToRemove -> {
                try {
                    if (amountToRemove == equipment.getAmount()) {
                        equipmentService.deleteEquipment(equipment.getId()); // Remove from DB
                    } else {
                        equipment.setAmount(equipment.getAmount() - amountToRemove);
                        equipmentService.updateEquipment(equipment.getId(), equipment); // Update in DB
                    }
                    loadEquipment();
                } catch (Exception e) {
                    showError("Error", "Failed to remove equipment", e.getMessage());
                }
            });
        } catch (Exception e) {
            showError("Error", "Failed to remove equipment", e.getMessage());
        }
    }

    // Edit equipment dialog and logic
    private void handleEditEquipment(Equipment equipment) {
        try {
            Dialog<Equipment> dialog = new Dialog<>();
            dialog.setTitle("Edit Equipment");
            dialog.setHeaderText("Edit equipment details");

            ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

            VBox content = new VBox(10);
            TextField nameField = new TextField(equipment.getName());
            nameField.setPromptText("Equipment Name");
            TextField descriptionField = new TextField(equipment.getDescription());
            descriptionField.setPromptText("Description");
            TextField amountField = new TextField(String.valueOf(equipment.getAmount()));
            amountField.setPromptText("Amount");

            content.getChildren().addAll(
                new Label("Name:"), nameField,
                new Label("Description:"), descriptionField,
                new Label("Amount:"), amountField
            );
            dialog.getDialogPane().setContent(content);
            nameField.requestFocus();

            dialog.setResultConverter(dialogButton -> {
                if (dialogButton == saveButtonType) {
                    try {
                        String name = nameField.getText().trim();
                        String description = descriptionField.getText().trim();
                        int amount = Integer.parseInt(amountField.getText().trim());
                        if (name.isEmpty() || description.isEmpty() || amount <= 0) {
                            showError("Error", "Invalid input", "Please fill all fields with valid values.");
                            return null;
                        }
                        equipment.setName(name);
                        equipment.setDescription(description);
                        equipment.setAmount(amount);
                        return equipment;
                    } catch (NumberFormatException e) {
                        showError("Error", "Invalid amount", "Please enter a valid number for amount.");
                        return null;
                    }
                }
                return null;
            });

            Optional<Equipment> result = dialog.showAndWait();
            result.ifPresent(eq -> {
                try {
                    equipmentService.updateEquipment(eq.getId(), eq); // Persist changes
                    loadEquipment();
                } catch (Exception e) {
                    showError("Error", "Failed to edit equipment", e.getMessage());
                }
            });
        } catch (Exception e) {
            showError("Error", "Failed to open edit dialog", e.getMessage());
        }
    }

    @FXML
    private void handleEditRoom() {
        try {
            // Use the correct pattern with loadAndGetController
            EditRoomController controller = springFXMLLoader.loadAndGetController("/fxml/edit-room.fxml", EditRoomController.class);
            controller.setRoom(room);

            Stage dialogStage = new Stage();
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.initOwner(roomIdLabel.getScene().getWindow());
            dialogStage.setTitle("Edit Room");
            dialogStage.setScene(new Scene(controller.getRoot()));
            
            dialogStage.showAndWait();
            
            // Refresh room data after dialog is closed
            room = roomService.getRoomByIdWithRelationsSafe(room.getId());
            updateUI();
            
        } catch (Exception e) {
            showError("Error", "Failed to open edit room dialog", e.getMessage());
        }
    }

    @FXML
    private void handleClose() {
        Stage stage = (Stage) roomIdLabel.getScene().getWindow();
        stage.close();
    }

    private void showError(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    public void setOnUpdateCallback(Runnable callback) {
        this.onUpdateCallback = callback;
    }

    private void showEquipmentMaintenancePopup(Equipment equipment) {
        try {
            // Close any existing popup
            if (currentPopup != null) {
                currentPopup.hide();
                currentPopup = null;
            }
            
            // Get maintenance records for this equipment
            List<EquipmentMaintenanceLog> maintenanceRecords = equipmentMaintenanceService
                .getMaintenanceRecordsByEquipmentId(equipment.getId());
            
            // Create popup content
            VBox content = new VBox(10);
            content.setStyle("-fx-padding: 20px; -fx-background-color: white; -fx-border-color: #ccc; -fx-border-radius: 5px;");
            
            // Header
            Label headerLabel = new Label(equipment.getName() + " - Maintenance Records");
            headerLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #333;");
            content.getChildren().add(headerLabel);
            
            // Equipment info
            VBox equipmentInfo = new VBox(5);
            equipmentInfo.setStyle("-fx-padding: 10px; -fx-background-color: #f5f5f5; -fx-border-radius: 3px;");
            
            Label descriptionLabel = new Label("Description: " + equipment.getDescription());
            Label amountLabel = new Label("Amount: " + equipment.getAmount());
            Label roomLabel = new Label("Room: " + room.getType());
            
            equipmentInfo.getChildren().addAll(descriptionLabel, amountLabel, roomLabel);
            content.getChildren().add(equipmentInfo);
            
            // Maintenance records table
            if (maintenanceRecords.isEmpty()) {
                Label noRecordsLabel = new Label("No maintenance records found");
                noRecordsLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #666; -fx-padding: 20px;");
                content.getChildren().add(noRecordsLabel);
            } else {
                // Create table for maintenance records
                TableView<EquipmentMaintenanceLog> maintenanceTable = new TableView<>();
                maintenanceTable.setPrefHeight(200);
                
                // Table columns
                TableColumn<EquipmentMaintenanceLog, String> typeColumn = new TableColumn<>("Type");
                typeColumn.setCellValueFactory(new PropertyValueFactory<>("maintenanceType"));
                typeColumn.setPrefWidth(100);
                
                TableColumn<EquipmentMaintenanceLog, String> technicianColumn = new TableColumn<>("Technician");
                technicianColumn.setCellValueFactory(new PropertyValueFactory<>("technician"));
                technicianColumn.setPrefWidth(120);
                
                TableColumn<EquipmentMaintenanceLog, String> statusColumn = new TableColumn<>("Status");
                statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
                statusColumn.setPrefWidth(100);
                
                TableColumn<EquipmentMaintenanceLog, String> dateColumn = new TableColumn<>("Date");
                dateColumn.setCellValueFactory(cellData -> {
                    EquipmentMaintenanceLog record = cellData.getValue();
                    String dateStr = record.getMaintenanceDate() != null ?
                        record.getMaintenanceDate().format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT)) : "N/A";
                    return new SimpleStringProperty(dateStr);
                });
                dateColumn.setPrefWidth(120);
                
                maintenanceTable.getColumns().addAll(typeColumn, technicianColumn, statusColumn, dateColumn);
                
                // Set table data
                ObservableList<EquipmentMaintenanceLog> recordsList = FXCollections.observableArrayList(maintenanceRecords);
                maintenanceTable.setItems(recordsList);
                
                content.getChildren().add(maintenanceTable);
            }
            
            // Action buttons
            HBox buttonBox = new HBox(10);
            buttonBox.setAlignment(Pos.CENTER_RIGHT);
            
            Button addButton = new Button("Add Maintenance");
            addButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-padding: 8px 16px;");
            addButton.setOnAction(e -> {
                // Close popup
                if (content.getScene() != null && content.getScene().getWindow() instanceof Popup) {
                    ((Popup) content.getScene().getWindow()).hide();
                    currentPopup = null;
                }
                // Open add maintenance dialog
                openAddMaintenanceDialog(equipment);
            });
            
            Button editButton = new Button("Edit Maintenance");
            editButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-padding: 8px 16px;");
            editButton.setOnAction(e -> {
                // Close popup
                if (content.getScene() != null && content.getScene().getWindow() instanceof Popup) {
                    ((Popup) content.getScene().getWindow()).hide();
                    currentPopup = null;
                }
                // Open edit maintenance dialog (if records exist)
                if (!maintenanceRecords.isEmpty()) {
                    openEditMaintenanceDialog(equipment, maintenanceRecords.get(0));
                }
            });
            
            Button closeButton = new Button("Close");
            closeButton.setStyle("-fx-background-color: #9E9E9E; -fx-text-fill: white; -fx-padding: 8px 16px;");
            closeButton.setOnAction(e -> {
                if (content.getScene() != null && content.getScene().getWindow() instanceof Popup) {
                    ((Popup) content.getScene().getWindow()).hide();
                    currentPopup = null;
                }
            });
            
            buttonBox.getChildren().addAll(addButton, editButton, closeButton);
            content.getChildren().add(buttonBox);
            
            // Create and show popup
            Popup popup = new Popup();
            popup.getContent().add(content);
            popup.setAutoHide(true);
            
            // Store reference to current popup
            currentPopup = popup;
            
            // Clear reference when popup is hidden
            popup.setOnHidden(event -> currentPopup = null);
            
            // Show popup near the equipment table
            popup.show(equipmentTable, equipmentTable.getScene().getWindow().getX() + equipmentTable.getScene().getWindow().getWidth() / 2, 
                      equipmentTable.getScene().getWindow().getY() + equipmentTable.getScene().getWindow().getHeight() / 2);
            
        } catch (Exception e) {
            Logger.getLogger(RoomDetailsController.class.getName()).log(Level.SEVERE, "Error showing equipment maintenance popup", e);
            showError("Error", "Failed to load maintenance information", e.getMessage());
        }
    }

    private void openAddMaintenanceDialog(Equipment equipment) {
        try {
            Dialog<EquipmentMaintenanceLog> dialog = new Dialog<>();
            dialog.setTitle("Add Maintenance Record");
            dialog.setHeaderText("Add new maintenance record for " + equipment.getName());

            ButtonType addButtonType = new ButtonType("Add", ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(addButtonType, ButtonType.CANCEL);

            VBox content = new VBox(10);
            
            ComboBox<String> typeComboBox = new ComboBox<>();
            typeComboBox.getItems().addAll("Routine", "Preventive", "Corrective", "Emergency");
            typeComboBox.setPromptText("Maintenance Type");
            
            TextField technicianField = new TextField();
            technicianField.setPromptText("Technician Name");

            TextField descriptionField = new TextField();
            descriptionField.setPromptText("Description");

            ComboBox<String> statusComboBox = new ComboBox<>();
            statusComboBox.getItems().addAll("Pending", "In Progress", "Completed");
            statusComboBox.setPromptText("Status");

            content.getChildren().addAll(
                new Label("Maintenance Type:"), typeComboBox,
                new Label("Technician:"), technicianField,
                new Label("Description:"), descriptionField,
                new Label("Status:"), statusComboBox
            );
            dialog.getDialogPane().setContent(content);
            typeComboBox.requestFocus();

            dialog.setResultConverter(dialogButton -> {
                if (dialogButton == addButtonType) {
                    try {
                        String type = typeComboBox.getValue();
                        String technician = technicianField.getText().trim();
                        String description = descriptionField.getText().trim();
                        String status = statusComboBox.getValue();
                        if (type == null || technician.isEmpty() || status == null || description.isEmpty()) {
                            showError("Error", "Invalid input", "Please fill all fields.");
                            return null;
                        }
                        EquipmentMaintenanceLog record = new EquipmentMaintenanceLog(
                            equipment.getId(),
                            java.time.LocalDateTime.now(),
                            type,
                            description,
                            technician,
                            status
                        );
                        return record;
                    } catch (Exception e) {
                        e.printStackTrace();
                        showError("Error", "Invalid input", e.toString());
                        return null;
                    }
                }
                return null;
            });

            Optional<EquipmentMaintenanceLog> result = dialog.showAndWait();
            result.ifPresent(record -> {
                try {
                    System.out.println("[DEBUG] Attempting to save maintenance record: " + record);
                    equipmentMaintenanceService.createMaintenanceRecord(record);
                    showEquipmentMaintenancePopup(equipment); // Refresh popup
                } catch (Exception e) {
                    e.printStackTrace();
                    System.out.println("[ERROR] Failed to add maintenance record: " + record);
                    showError("Error", "Failed to add maintenance record", e.toString());
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            showError("Error", "Failed to open add maintenance dialog", e.toString());
        }
    }

    private void openEditMaintenanceDialog(Equipment equipment, EquipmentMaintenanceLog record) {
        try {
            Dialog<EquipmentMaintenanceLog> dialog = new Dialog<>();
            dialog.setTitle("Edit Maintenance Record");
            dialog.setHeaderText("Edit maintenance record for " + equipment.getName());

            ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

            VBox content = new VBox(10);
            
            ComboBox<String> typeComboBox = new ComboBox<>();
            typeComboBox.getItems().addAll("Routine", "Preventive", "Corrective", "Emergency");
            typeComboBox.setValue(record.getType());
            
            TextField technicianField = new TextField(record.getTechnician());
            technicianField.setPromptText("Technician Name");

            TextField descriptionField = new TextField(record.getDescription());
            descriptionField.setPromptText("Description");

            ComboBox<String> statusComboBox = new ComboBox<>();
            statusComboBox.getItems().addAll("Pending", "In Progress", "Completed");
            statusComboBox.setValue(record.getStatus());

            content.getChildren().addAll(
                new Label("Maintenance Type:"), typeComboBox,
                new Label("Technician:"), technicianField,
                new Label("Description:"), descriptionField,
                new Label("Status:"), statusComboBox
            );
            dialog.getDialogPane().setContent(content);
            typeComboBox.requestFocus();

            dialog.setResultConverter(dialogButton -> {
                if (dialogButton == saveButtonType) {
                    try {
                        String type = typeComboBox.getValue();
                        String technician = technicianField.getText().trim();
                        String description = descriptionField.getText().trim();
                        String status = statusComboBox.getValue();
                        if (type == null || technician.isEmpty() || status == null || description.isEmpty()) {
                            showError("Error", "Invalid input", "Please fill all fields.");
                            return null;
                        }
                        record.setType(type);
                        record.setTechnician(technician);
                        record.setDescription(description);
                        record.setStatus(status);
                        // Do not update equipmentId or maintenanceDate here
                        return record;
                    } catch (Exception e) {
                        e.printStackTrace();
                        showError("Error", "Invalid input", e.toString());
                        return null;
                    }
                }
                return null;
            });

            Optional<EquipmentMaintenanceLog> result = dialog.showAndWait();
            result.ifPresent(updatedRecord -> {
                try {
                    System.out.println("[DEBUG] Attempting to update maintenance record: " + updatedRecord);
                    equipmentMaintenanceService.updateMaintenanceRecord(record.getId(), updatedRecord);
                    showEquipmentMaintenancePopup(equipment); // Refresh popup
                } catch (Exception e) {
                    e.printStackTrace();
                    System.out.println("[ERROR] Failed to update maintenance record: " + updatedRecord);
                    showError("Error", "Failed to update maintenance record", e.toString());
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            showError("Error", "Failed to open edit maintenance dialog", e.toString());
        }
    }
} 