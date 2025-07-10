package org.example.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.Scene;
import javafx.scene.Parent;
import javafx.stage.Stage;
import javafx.stage.Modality;
import org.example.models.Block;
import org.example.models.Department;
import org.example.models.Prison;
import org.example.models.Staff;
import org.example.services.BlockService;
import org.example.services.DepartmentService;
import org.example.services.PrisonService;
import org.example.services.StaffService;
import org.example.services.PrisonerService;
import org.example.config.SpringFXMLLoader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import jakarta.annotation.PostConstruct;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.scene.control.cell.PropertyValueFactory;
import java.util.List;
import java.util.stream.Collectors;
import java.util.logging.Logger;
import java.util.logging.Level;
import org.springframework.context.annotation.Scope;
import javafx.scene.input.MouseEvent;

@Component
@Scope("prototype")
public class PrisonController {
    private static final Logger LOGGER = Logger.getLogger(PrisonController.class.getName());

    @FXML private Label prisonNameLabel;
    @FXML private Label wardenNameLabel;
    @FXML private Label capacityLabel;
    @FXML private Label availableCellsLabel;
    @FXML private TableView<Block> blocksTable;
    @FXML private TableColumn<Block, String> blockTypeCol;
    @FXML private TableColumn<Block, Integer> cellsCol;
    @FXML private TableColumn<Block, Integer> prisonersCol;
    @FXML private TableColumn<Block, Double> expensesCol;
    @FXML private TableColumn<Block, String> medicalDeptCol;
    @FXML private TableColumn<Block, Void> actionsCol;
    @FXML private TextField searchField;

    private final ApplicationContext applicationContext;
    private final SpringFXMLLoader springFXMLLoader;
    private final BlockService blockService;
    private final DepartmentService departmentService;
    private final PrisonService prisonService;
    private final StaffService staffService;
    private final PrisonerService prisonerService;

    private Prison currentPrison;

    @Autowired
    public PrisonController(ApplicationContext applicationContext,
                          SpringFXMLLoader springFXMLLoader,
                          BlockService blockService,
                          DepartmentService departmentService,
                          PrisonService prisonService,
                          StaffService staffService,
                          PrisonerService prisonerService) {
        this.applicationContext = applicationContext;
        this.springFXMLLoader = springFXMLLoader;
        this.blockService = blockService;
        this.departmentService = departmentService;
        this.prisonService = prisonService;
        this.staffService = staffService;
        this.prisonerService = prisonerService;
        LOGGER.info("PrisonController constructor called with dependencies");
    }

    @PostConstruct
    public void init() {
        LOGGER.info("PostConstruct: Initializing PrisonController...");
        if (applicationContext == null || springFXMLLoader == null || blockService == null ||
            departmentService == null || prisonService == null || staffService == null || prisonerService == null) {
            LOGGER.severe("Spring dependencies not properly initialized");
            throw new IllegalStateException("Spring dependencies not properly initialized");
        }
        LOGGER.info("Spring dependencies initialized successfully");
    }

    @FXML
    public void initialize() {
        LOGGER.info("FXML initialize: Starting initialization of PrisonController...");
        
        // Verify Spring dependencies
        LOGGER.info("Verifying Spring dependencies...");
        verifySpringDependencies();
        
        // Verify FXML component injection
        LOGGER.info("Verifying FXML component injection...");
        verifyFXMLInjection();
        
        // Set up UI components
        LOGGER.info("Setting up UI components...");
            setupUIComponents();
        
        // Load initial data
        LOGGER.info("Loading initial data...");
            loadInitialData();
        
            LOGGER.info("FXML initialization completed successfully");
    }

    private void verifySpringDependencies() {
        LOGGER.info("Starting Spring dependencies verification");
        if (applicationContext == null || springFXMLLoader == null || blockService == null ||
            departmentService == null || prisonService == null || staffService == null || prisonerService == null) {
            LOGGER.severe("Spring dependencies not properly initialized");
            throw new IllegalStateException("Spring dependencies not properly initialized");
        }
        LOGGER.info("All Spring dependencies verified successfully");
    }

    private void verifyFXMLInjection() {
        LOGGER.info("Starting FXML component injection verification");
        if (prisonNameLabel == null || wardenNameLabel == null || capacityLabel == null || 
            availableCellsLabel == null || blocksTable == null || blockTypeCol == null || 
            cellsCol == null || prisonersCol == null || expensesCol == null || 
            medicalDeptCol == null || actionsCol == null) {
            LOGGER.warning("FXML component injection issues found:");
            if (prisonNameLabel == null) LOGGER.warning("prisonNameLabel");
            if (wardenNameLabel == null) LOGGER.warning("wardenNameLabel");
            if (capacityLabel == null) LOGGER.warning("capacityLabel");
            if (availableCellsLabel == null) LOGGER.warning("availableCellsLabel");
            if (blocksTable == null) LOGGER.warning("blocksTable");
            if (blockTypeCol == null) LOGGER.warning("blockTypeCol");
            if (cellsCol == null) LOGGER.warning("cellsCol");
            if (prisonersCol == null) LOGGER.warning("prisonersCol");
            if (expensesCol == null) LOGGER.warning("expensesCol");
            if (medicalDeptCol == null) LOGGER.warning("medicalDeptCol");
            if (actionsCol == null) LOGGER.warning("actionsCol");
            throw new IllegalStateException("FXML components not properly injected");
        }
        LOGGER.info("All FXML components verified successfully");
    }

    private void setupUIComponents() {
        LOGGER.info("Starting UI components setup");
        
        // Initialize labels with default values
        LOGGER.info("Initializing labels with default values");
        if (prisonNameLabel != null) prisonNameLabel.setText("Loading...");
        if (wardenNameLabel != null) wardenNameLabel.setText("Loading...");
        if (capacityLabel != null) capacityLabel.setText("Loading...");
        if (availableCellsLabel != null) availableCellsLabel.setText("Loading...");
        
        // Initialize blocks table
        LOGGER.info("Initializing blocks table");
        if (blocksTable != null) {
            setupBlocksTable();
        } else {
            LOGGER.severe("blocksTable is null - cannot initialize table");
        }
        
        // Setup search functionality
        LOGGER.info("Setting up search functionality");
        setupSearch();
        
        LOGGER.info("UI components setup completed successfully");
    }

    private void setupBlocksTable() {
        // Set up columns
        blockTypeCol.setCellValueFactory(new PropertyValueFactory<>("type"));
        cellsCol.setCellValueFactory(new PropertyValueFactory<>("numberOfCells"));
        prisonersCol.setCellValueFactory(new PropertyValueFactory<>("numberOfPrisoners"));
        expensesCol.setCellValueFactory(new PropertyValueFactory<>("totalExpenses"));
        medicalDeptCol.setCellValueFactory(new PropertyValueFactory<>("medicalDepartment"));
        
        // Add double-click event handler
        blocksTable.setRowFactory(tv -> {
            TableRow<Block> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (!row.isEmpty())) {
                    Block block = row.getItem();
                    handleBlockDetails(event);
                }
            });
            return row;
        });
        
        // Set up actions column with only delete button
        actionsCol.setCellFactory(col -> new TableCell<Block, Void>() {
            private final Button deleteButton = new Button("Delete");
            {
                deleteButton.setOnAction(event -> {
                    Block block = getTableView().getItems().get(getIndex());
                    handleDeleteBlock(block);
                });
            }
            
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : deleteButton);
            }
        });
    }

    private void loadInitialData() {
        LOGGER.info("Starting initial data load");
        try {
            // Load prisons from database
            LOGGER.info("Loading prisons from database...");
            List<Prison> prisons = prisonService.getAllPrisons();
            
            if (!prisons.isEmpty()) {
                // Set the first prison as current
                currentPrison = prisons.get(0);
                LOGGER.info("Updating prison info for prison: " + currentPrison.getName());
                updatePrisonInfo(currentPrison);

            // Load blocks data
                LOGGER.info("Loading blocks data...");
                loadBlocksData();
            } else {
                LOGGER.warning("No prisons found in database");
                showError("No Prisons", "No prisons found in the database.");
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error loading initial data", e);
            showError("Error", "Failed to load initial data: " + e.getMessage());
        }
    }

    private void loadBlocksData() {
        if (blocksTable == null || currentPrison == null) {
            LOGGER.warning("Cannot load blocks data - required components are null");
            return;
        }

        // Load blocks in background thread
        new Thread(() -> {
            try {
                List<Block> blocks = blockService.getBlocksByPrisonId(currentPrison.getId());
                
                // Update UI on JavaFX thread
                Platform.runLater(() -> {
                    try {
                blocksTable.setItems(FXCollections.observableArrayList(blocks));
                    } catch (Exception e) {
                        LOGGER.log(Level.SEVERE, "Error updating blocks table", e);
            }
                });
        } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Error loading blocks data", e);
                Platform.runLater(() -> 
                    displayError("Data Loading Error", "Failed to load blocks data"));
            }
        }).start();
    }

    private void updatePrisonInfo(Prison prison) {
        if (prison == null) {
            LOGGER.severe("Cannot update prison info - prison is null");
            return;
        }

        LOGGER.info("Updating prison info for prison: " + prison.getName());
        try {
            // Update prison name
            updatePrisonName(prison);
            
            // Update warden name
            updateWardenName(prison);
            
            // Update capacity
            updateCapacity(prison);
            
            // Update available cells
            updateAvailableCells(prison);

            LOGGER.info("Prison info updated successfully");
        } catch (Exception e) {
            String errorMsg = "Error updating prison info: " + e.getMessage();
            LOGGER.log(Level.SEVERE, errorMsg, e);
            throw new RuntimeException(errorMsg, e);
        }
    }

    private void updatePrisonName(Prison prison) {
            if (prisonNameLabel != null) {
            try {
                prisonNameLabel.setText(prison.getName());
                LOGGER.info("Updated prison name label to: " + prison.getName());
            } catch (Exception e) {
                LOGGER.warning("Failed to update prison name label: " + e.getMessage());
            }
        } else {
            LOGGER.warning("prisonNameLabel is null - cannot update prison name");
        }
    }

    private void updateWardenName(Prison prison) {
            if (wardenNameLabel != null) {
            try {
                String wardenName = staffService.getStaffByRoleAndPrisonId("Warden", prison.getId())
                    .map(Staff::getName)
                    .orElse("No Warden Assigned");
                wardenNameLabel.setText(wardenName);
                LOGGER.info("Updated warden name label to: " + wardenName);
            } catch (Exception e) {
                LOGGER.warning("Failed to update warden name label: " + e.getMessage());
                wardenNameLabel.setText("Error loading warden");
            }
        } else {
            LOGGER.warning("wardenNameLabel is null - cannot update warden name");
        }
    }

    private void updateCapacity(Prison prison) {
        if (capacityLabel != null) {
            try {
                int totalCapacity = calculateTotalCapacity(prison);
                capacityLabel.setText(String.valueOf(totalCapacity));
                LOGGER.info("Updated capacity label to: " + totalCapacity);
            } catch (Exception e) {
                LOGGER.warning("Failed to update capacity label: " + e.getMessage());
                capacityLabel.setText("Error loading capacity");
            }
        } else {
            LOGGER.warning("capacityLabel is null - cannot update capacity");
        }
    }

    private int calculateTotalCapacity(Prison prison) {
        LOGGER.info("Calculating total capacity for prison ID: " + prison.getId());
        List<Block> blocks = blockService.getBlocksByPrisonId(prison.getId());
        int totalCapacity = blocks.stream()
            .flatMap(block -> block.getCells().stream())
            .mapToInt(cell -> cell.getCapacity())
            .sum();
        LOGGER.info("Total capacity calculated: " + totalCapacity);
        return totalCapacity;
    }

    private void updateAvailableCells(Prison prison) {
        if (availableCellsLabel != null) {
            try {
                int availableCells = calculateAvailableCells(prison);
                availableCellsLabel.setText(String.valueOf(availableCells));
                LOGGER.info("Updated available cells label to: " + availableCells);
            } catch (Exception e) {
                LOGGER.warning("Failed to update available cells label: " + e.getMessage());
                availableCellsLabel.setText("Error loading availability");
            }
        } else {
            LOGGER.warning("availableCellsLabel is null - cannot update available cells");
        }
    }

    private int calculateAvailableCells(Prison prison) {
        LOGGER.info("Calculating available cells for prison ID: " + prison.getId());
        List<Block> blocks = blockService.getBlocksByPrisonId(prison.getId());
        int availableCells = blocks.stream()
            .flatMap(block -> block.getCells().stream())
            .filter(cell -> !cell.getType().equalsIgnoreCase("Solitary"))
            .mapToInt(cell -> cell.getCapacity() - cell.getPrisoners().size())
            .sum();
        LOGGER.info("Available cells calculated: " + availableCells);
        return availableCells;
    }

    @FXML
    private void handleEditBlock() {
        try {
            Block selectedBlock = blocksTable.getSelectionModel().getSelectedItem();
            if (selectedBlock == null) {
                showError("Validation Error", "Please select a block to edit");
                return;
            }

            LOGGER.info("Opening edit block dialog for block: " + selectedBlock.getId());
            EditBlockController controller = springFXMLLoader.loadAndGetController("/fxml/edit-block.fxml", EditBlockController.class);
            controller.setBlock(selectedBlock);
            
            Stage dialogStage = new Stage();
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.initOwner(blocksTable.getScene().getWindow());
            dialogStage.setTitle("Edit Block");
            dialogStage.setScene(new Scene(controller.getRoot()));
            
            dialogStage.showAndWait();
            
            // Refresh the blocks table after dialog is closed
            loadBlocksData();
        } catch (Exception e) {
            LOGGER.severe("Failed to open edit block dialog: " + e.getMessage());
            showError("Error", "Failed to open edit block dialog");
        }
    }

    private void handleDeleteBlock(Block block) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Block");
        alert.setHeaderText("Delete Block");
        alert.setContentText("Are you sure you want to delete this block?");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    blockService.deleteBlock(block.getId());
                    // Refresh the blocks table
                    if (currentPrison != null) {
                        List<Block> blocks = blockService.getBlocksByPrisonId(currentPrison.getId());
                        blocksTable.setItems(FXCollections.observableArrayList(blocks));
                    }
                } catch (Exception e) {
                    LOGGER.log(Level.SEVERE, "Error deleting block", e);
                    showError("Error", "Could not delete block", e.getMessage());
                }
            }
        });
    }

    private void showError(String title, String header, String content) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(title);
            alert.setHeaderText(header);
            alert.setContentText(content != null ? content : "");
            alert.showAndWait();
        });
    }

    private void showError(String title, String header) {
        showError(title, header, null);
    }

    private void showSuccess(String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }

    @FXML
    public void handleStaff() {
        LOGGER.info("Loading staff view...");
        try {
            // Close current window
            Stage currentStage = (Stage) prisonNameLabel.getScene().getWindow();
            currentStage.close();

            // Load and show staff view
            Parent staffView = springFXMLLoader.load("/fxml/all_staff.fxml");
            Stage newStage = new Stage();
            newStage.setTitle("Staff Management");
            newStage.setScene(new Scene(staffView));
            newStage.show();
            
            LOGGER.info("Staff view loaded successfully");
        } catch (Exception e) {
            String errorMsg = "Failed to load staff view: " + e.getMessage();
            LOGGER.log(Level.SEVERE, errorMsg, e);
            displayError("Navigation Error", "Failed to load staff view", errorMsg);
        }
    }

    @FXML
    public void handlePrisoners() {
        LOGGER.info("Loading prisoners view...");
        try {
            // Close current window
            Stage currentStage = (Stage) prisonNameLabel.getScene().getWindow();
            currentStage.close();

            // Load and show prisoners view
            Parent prisonersView = springFXMLLoader.load("/fxml/all_prisoners.fxml");
            Stage newStage = new Stage();
            newStage.setTitle("Prisoners Management");
            newStage.setScene(new Scene(prisonersView));
            newStage.show();
            
            LOGGER.info("Prisoners view loaded successfully");
        } catch (Exception e) {
            String errorMsg = "Failed to load prisoners view: " + e.getMessage();
            LOGGER.log(Level.SEVERE, errorMsg, e);
            displayError("Navigation Error", "Failed to load prisoners view", errorMsg);
        }
    }

    @FXML
    public void handleAllWeapons() {
        try {
            LOGGER.info("Opening weapons management...");
            
            // Load the weapons management FXML and get controller using SpringFXMLLoader
            WeaponsManagementController controller = springFXMLLoader.loadAndGetController("/fxml/weapons-management.fxml", WeaponsManagementController.class);
            Parent root = controller.getRoot();
            
            // Create and show new scene
            Stage stage = new Stage();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("Weapons Management");
            stage.show();
            
            LOGGER.info("Successfully opened weapons management");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Unexpected error opening weapons management", e);
            showError("Unexpected Error", "An unexpected error occurred: " + e.getMessage());
        }
    }

    @FXML
    private void handleViewExpenses() {
        // TODO: Implement expenses view
    }

    @FXML
    private void handleAddBlock() {
        try {
            LOGGER.info("Opening add block dialog...");
            Parent root = springFXMLLoader.load("/fxml/add_block.fxml");
            
            Stage dialogStage = new Stage();
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.initOwner(blocksTable.getScene().getWindow());
            dialogStage.setTitle("Add New Block");
            dialogStage.setScene(new Scene(root));
            
            dialogStage.showAndWait();
            
            // Refresh the blocks table after dialog is closed
            loadBlocksData();
        } catch (Exception e) {
            LOGGER.severe("Failed to open add block dialog: " + e.getMessage());
            showError("Error", "Failed to open add block dialog");
        }
    }

    @FXML
    private void handleEditPrison() {
        try {
            LOGGER.info("Opening edit prison dialog...");
            Parent root = springFXMLLoader.load("/fxml/edit_prison.fxml");
            EditPrisonController controller = applicationContext.getBean(EditPrisonController.class);
            controller.setPrison(currentPrison);

            Stage dialogStage = new Stage();
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.initOwner(prisonNameLabel.getScene().getWindow());
            dialogStage.setTitle("Edit Prison");
            dialogStage.setScene(new Scene(root));
            
            dialogStage.showAndWait();
            
            // Refresh prison info after dialog is closed
            currentPrison = prisonService.getPrisonById(currentPrison.getId());
            updatePrisonInfo(currentPrison);
            loadBlocksData();
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to open edit prison dialog", e);
            showError("Error", "Failed to open edit prison dialog", e.getMessage());
        }
    }

    @FXML
    private void handleSearch() {
        String searchText = searchField.getText().toLowerCase();
        if (searchText.isEmpty()) {
            loadBlocksData();
            return;
        }

        try {
            List<Block> allBlocks = blockService.getBlocksByPrisonId(currentPrison.getId());
            List<Block> filteredBlocks = allBlocks.stream()
                .filter(block -> block.getType().toLowerCase().contains(searchText))
                .collect(Collectors.toList());
            blocksTable.setItems(FXCollections.observableArrayList(filteredBlocks));
            LOGGER.info("Blocks filtered by search text: " + searchText);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error searching blocks: " + e.getMessage(), e);
            displayError("Search Error", "Failed to search blocks: " + e.getMessage());
        }
    }

    private void setupSearch() {
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null || newValue.isEmpty()) {
                loadBlocksData();
            } else {
                handleSearch();
            }
        });
    }

    @FXML
    private void handleSaveBlock() {
        try {
            Block selectedBlock = blocksTable.getSelectionModel().getSelectedItem();
            if (selectedBlock == null) {
                showError("Validation Error", "Please select a block to edit");
                return;
            }
            
            String blockType = selectedBlock.getType();
            if (blockType.isEmpty()) {
                showError("Validation Error", "Block type cannot be empty");
                return;
            }
            
            // Get the first prison (assuming we're working with a single prison for now)
            Prison prison = prisonService.getAllPrisons().get(0);
            
            // Create and save the new block
            Block newBlock = new Block(blockType, prison);
            blockService.createBlock(newBlock);
            
            // Close the dialog
            Stage stage = (Stage) blocksTable.getScene().getWindow();
            stage.close();
            
            // Show success message
            displaySuccess("Success", "Block added successfully");
        } catch (Exception e) {
            showError("Error", "Failed to create block", e.getMessage());
        }
    }

    @FXML
    private void handleCancelBlock() {
        // Get the current stage from any available UI element
        Stage currentStage = null;
        if (blocksTable != null && blocksTable.getScene() != null) {
            currentStage = (Stage) blocksTable.getScene().getWindow();
        } else if (blockTypeCol != null && blockTypeCol.getTableView() != null) {
            currentStage = (Stage) blockTypeCol.getTableView().getScene().getWindow();
        }

        if (currentStage != null) {
            currentStage.close();
        }
    }

    @FXML
    private void handleSavePrison() {
        try {
            // Validate inputs
            if (prisonNameLabel.getText().trim().isEmpty()) {
                showError("Validation Error", "Prison name cannot be empty");
                return;
            }

            // Update prison details
            currentPrison.setName(prisonNameLabel.getText().trim());

            // Save to database
            prisonService.updatePrison(currentPrison.getId(), currentPrison);
            
            // Close the dialog
            Stage stage = (Stage) prisonNameLabel.getScene().getWindow();
            stage.close();

            // Refresh data
            loadInitialData();
            
            showSuccess("Success", "Prison details updated successfully");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error updating prison details", e);
            showError("Error", "Failed to update prison details", e.getMessage());
        }
    }

    @FXML
    private void handleCancelEditPrison() {
        Stage stage = (Stage) prisonNameLabel.getScene().getWindow();
        stage.close();
    }

    @FXML
    private void handleReports() {
        // TODO: Implement reports view
    }

    @FXML
    private void showAddBlockForm() {
        // TODO: Implement add block form dialog
    }

    private void displayError(String title, String header) {
        showError(title, header, null);
    }

    private void displayError(String title, String header, String content) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(title);
            alert.setHeaderText(header);
            alert.setContentText(content);
            alert.showAndWait();
        });
    }

    private void displaySuccess(String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }

    @Transactional
    private int calculatePrisonersInBlock(Block block) {
        LOGGER.info("Calculating prisoners for block ID: " + block.getId());
        int prisonerCount = block.getCells().stream()
                .mapToInt(cell -> {
                    int count = cell.getPrisoners().size();
                    LOGGER.info("Cell " + cell.getId() + " has " + count + " prisoners");
                    return count;
                })
                .sum();
        LOGGER.info("Total prisoners in block " + block.getId() + ": " + prisonerCount);
        return prisonerCount;
    }

    @Transactional
    private double calculateBlockExpenses(Block block) {
        LOGGER.info("Calculating expenses for block ID: " + block.getId());
        double totalExpenses = block.getContains2().stream()
                .map(contains2 -> {
                    Department dept = contains2.getDepartment();
                    LOGGER.info("Processing department: " + dept.getType());
                    return dept.getExpenses().stream()
                            .mapToDouble(expense -> {
                                LOGGER.info("Expense amount: $" + expense.getAmount());
                                return expense.getAmount();
                            })
                .sum();
                })
                .mapToDouble(Double::doubleValue)
                .sum();
        LOGGER.info("Total expenses for block " + block.getId() + ": $" + totalExpenses);
        return totalExpenses;
    }

    @Transactional
    private boolean hasMedicalDepartment(Block block) {
        LOGGER.info("Checking medical department for block ID: " + block.getId());
        boolean hasMedical = block.getContains2().stream()
                .anyMatch(contains2 -> {
                    boolean isMedical = "medical".equalsIgnoreCase(contains2.getDepartment().getType());
                    LOGGER.info("Department " + contains2.getDepartment().getType() + 
                              " is medical: " + isMedical);
                    return isMedical;
                });
        LOGGER.info("Block " + block.getId() + " has medical department: " + hasMedical);
        return hasMedical;
    }

    @FXML
    private void handleBackToDashboard() {
        try {
            // Close current window
            Stage currentStage = (Stage) prisonNameLabel.getScene().getWindow();
            currentStage.close();

            // Load FXML using Spring-managed FXMLLoader
            FXMLLoader loader = applicationContext.getBean(FXMLLoader.class);
            loader.setLocation(getClass().getResource("/fxml/dashboard.fxml"));
            Parent root = loader.load();
            
            Stage stage = new Stage();
            stage.setTitle("Prison Management System");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            showError("Error", "Failed to load dashboard", e.getMessage());
        }
    }

    @FXML
    private void handleBlockDetails(MouseEvent event) {
        if (event.getClickCount() == 2) {
            Block selectedBlock = blocksTable.getSelectionModel().getSelectedItem();
            if (selectedBlock != null) {
                try {
                    Block blockWithRelations = blockService.getBlockByIdWithRelations(selectedBlock.getId());
                    BlockDetailsController controller = springFXMLLoader.loadAndGetController("/fxml/block-details.fxml", BlockDetailsController.class);
                    controller.initData(blockWithRelations);
                    
                    // Close current window
                    Stage currentStage = (Stage) blocksTable.getScene().getWindow();
                    currentStage.close();
                    
                    // Show block details window
                    Stage newStage = new Stage();
                    newStage.setTitle("Block Details");
                    newStage.setScene(new Scene(controller.getRoot()));
                    newStage.show();
                } catch (Exception e) {
                    e.printStackTrace();
                    showError("Error loading block details", e.getMessage());
                }
            }
        }
    }

    private void initializeTableColumns() {
        if (blockTypeCol != null) {
            blockTypeCol.setCellValueFactory(new PropertyValueFactory<>("type"));
        }
        if (cellsCol != null) {
            cellsCol.setCellValueFactory(new PropertyValueFactory<>("numberOfCells"));
        }
        if (prisonersCol != null) {
            prisonersCol.setCellValueFactory(new PropertyValueFactory<>("numberOfPrisoners"));
        }
        if (expensesCol != null) {
            expensesCol.setCellValueFactory(new PropertyValueFactory<>("totalExpenses"));
        }
        if (medicalDeptCol != null) {
            medicalDeptCol.setCellValueFactory(new PropertyValueFactory<>("medicalDepartment"));
        }
    }

    private void loadPrisonData() {
        if (currentPrison != null) {
            List<Block> blocks = blockService.getBlocksByPrisonId(currentPrison.getId());
            blocksTable.setItems(FXCollections.observableArrayList(blocks));
        }
    }
} 