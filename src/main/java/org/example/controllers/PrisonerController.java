package org.example.controllers;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.control.*;
import org.example.models.Prisoner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.example.services.PrisonerService;
import org.example.services.CellService;
import org.example.config.SpringFXMLLoader;

import java.io.IOException;
import java.util.List;

@Component
public class PrisonerController {

    private final PrisonerService prisonerService;
    private final CellService cellService;
    private final ApplicationContext applicationContext;
    private final SpringFXMLLoader springFXMLLoader;
    private Prisoner selectedPrisoner;

    @FXML private TextField prisonerSearchField;
    @FXML private TableView<Prisoner> prisonersTable;
    @FXML private TableColumn<Prisoner, String> prisonerIdCol;
    @FXML private TableColumn<Prisoner, String> prisonerNameCol;
    @FXML private TableColumn<Prisoner, String> prisonerGenderCol;
    @FXML private TableColumn<Prisoner, String> dobCol;
    @FXML private TableColumn<Prisoner, String> sentenceStartCol;
    @FXML private TableColumn<Prisoner, String> sentenceEndCol;
    @FXML private TableColumn<Prisoner, Void> prisonerActionsCol;

    @Autowired
    public PrisonerController(PrisonerService prisonerService, 
                            CellService cellService, 
                            ApplicationContext applicationContext,
                            SpringFXMLLoader springFXMLLoader) {
        this.prisonerService = prisonerService;
        this.cellService = cellService;
        this.applicationContext = applicationContext;
        this.springFXMLLoader = springFXMLLoader;
    }

    @FXML
    public void initialize() {
        try {
            System.out.println("Initializing PrisonerController...");
            if (prisonerService == null || cellService == null || springFXMLLoader == null) {
                System.err.println("Services not properly injected: prisonerService=" + (prisonerService != null) + 
                                 ", cellService=" + (cellService != null) +
                                 ", springFXMLLoader=" + (springFXMLLoader != null));
                throw new IllegalStateException("Services not properly injected");
            }
            System.out.println("Services are properly injected");
            setupTableColumns();
            System.out.println("Table columns setup complete");
            loadPrisoners();
            System.out.println("Prisoners loaded");
            setupSearch();
            System.out.println("Search setup complete");
            setupTableClickHandler();
            System.out.println("Table click handler setup complete");
            System.out.println("PrisonerController initialization complete.");
        } catch (Exception e) {
            System.err.println("Error initializing PrisonerController: " + e.getMessage());
            e.printStackTrace();
            showError("Initialization Error", "Failed to initialize prisoners view: " + e.getMessage());
        }
    }

    private void setupTableColumns() {
        try {
            System.out.println("Setting up table columns...");
            
            prisonerIdCol.setCellValueFactory(cellData -> 
                new ReadOnlyStringWrapper(String.valueOf(cellData.getValue().getId())));
                
            prisonerNameCol.setCellValueFactory(cellData -> 
                new ReadOnlyStringWrapper(cellData.getValue().getName()));
                
            prisonerGenderCol.setCellValueFactory(cellData -> 
                new ReadOnlyStringWrapper(cellData.getValue().getGender()));
                
            dobCol.setCellValueFactory(cellData -> 
                new ReadOnlyStringWrapper(cellData.getValue().getDateOfBirth().toString()));
                    
            sentenceStartCol.setCellValueFactory(cellData -> 
                new ReadOnlyStringWrapper(cellData.getValue().getSentenceStart().toString()));
                
            sentenceEndCol.setCellValueFactory(cellData -> 
                new ReadOnlyStringWrapper(cellData.getValue().getSentenceEnd().toString()));
            
            prisonerActionsCol.setCellFactory(col -> new TableCell<Prisoner, Void>() {
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
            System.out.println("Table columns setup complete.");
        } catch (Exception e) {
            System.err.println("Error setting up table columns: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    private void setupSearch() {
        prisonerSearchField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null || newValue.isEmpty()) {
                loadPrisoners();
            } else {
                handleSearch();
            }
        });
    }

    private void loadPrisoners() {
        try {
            System.out.println("Loading prisoners data...");
            List<Prisoner> prisoners = prisonerService.getAllPrisoners();
            System.out.println("Found " + prisoners.size() + " prisoners");
            if (prisoners.isEmpty()) {
                System.out.println("No prisoners found in the database");
            } else {
                System.out.println("First prisoner: " + prisoners.get(0).getName());
            }
            prisonersTable.setItems(FXCollections.observableArrayList(prisoners));
            System.out.println("Prisoners data loaded successfully.");
        } catch (Exception e) {
            System.err.println("Error loading prisoners: " + e.getMessage());
            e.printStackTrace();
            showError("Data Loading Error", "Failed to load prisoners data: " + e.getMessage());
        }
    }

    @FXML
    private void handleSearch() {
        String searchText = prisonerSearchField.getText().toLowerCase();
        if (searchText.isEmpty()) {
            loadPrisoners();
            return;
        }

        try {
            List<Prisoner> allPrisoners = prisonerService.getAllPrisoners();
            List<Prisoner> filteredPrisoners = allPrisoners.stream()
                .filter(prisoner -> prisoner.getName().toLowerCase().contains(searchText))
                .toList();
            prisonersTable.setItems(FXCollections.observableArrayList(filteredPrisoners));
        } catch (Exception e) {
            System.err.println("Error searching prisoners: " + e.getMessage());
            e.printStackTrace();
            showError("Search Error", "Failed to search prisoners: " + e.getMessage());
        }
    }

    @FXML
    private void goBack() {
        try {
            // Close current window
            Stage currentStage = (Stage) prisonersTable.getScene().getWindow();
            currentStage.close();

            // Load dashboard using SpringFXMLLoader
            Parent root = springFXMLLoader.load("/fxml/dashboard.fxml");
            
            Stage stage = new Stage();
            stage.setTitle("Prison Management System - Dashboard");
            stage.setScene(new Scene(root));
            stage.show();
            
            System.out.println("Successfully navigated back to dashboard");
        } catch (IOException e) {
            System.err.println("Error returning to dashboard: " + e.getMessage());
            e.printStackTrace();
            showError("Navigation Error", "Failed to return to dashboard: " + e.getMessage());
        }
    }

    private void handleDeletePrisoner(Prisoner prisoner) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Prisoner");
        alert.setHeaderText("Delete Prisoner");
        alert.setContentText("Are you sure you want to delete this prisoner?");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    prisonerService.deletePrisoner(prisoner.getId());
                    loadPrisoners(); // Refresh the table
                    showSuccess("Success", "Prisoner deleted successfully");
                } catch (Exception e) {
                    System.err.println("Error deleting prisoner: " + e.getMessage());
                    e.printStackTrace();
                    showError("Error", "Failed to delete prisoner: " + e.getMessage());
                }
            }
        });
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showSuccess(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void setPrisoner(Prisoner prisoner) {
        this.selectedPrisoner = prisoner;
        loadPrisonerDetails();
    }

    private void loadPrisonerDetails() {
        // Implementation for loading prisoner details
    }

    private void setupTableClickHandler() {
        // Add double-click handler for prisoners table
        prisonersTable.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                Prisoner selectedPrisoner = prisonersTable.getSelectionModel().getSelectedItem();
                if (selectedPrisoner != null) {
                    handlePrisonerDetails(selectedPrisoner);
                }
            }
        });

        // Add context menu for right-click options
        prisonersTable.setContextMenu(createContextMenu());
    }

    private ContextMenu createContextMenu() {
        ContextMenu contextMenu = new ContextMenu();
        
        MenuItem viewDetailsItem = new MenuItem("View Details");
        viewDetailsItem.setOnAction(event -> {
            Prisoner selectedPrisoner = prisonersTable.getSelectionModel().getSelectedItem();
            if (selectedPrisoner != null) {
                handlePrisonerDetails(selectedPrisoner);
            }
        });
        
        MenuItem editPrisonerItem = new MenuItem("Edit Prisoner");
        editPrisonerItem.setOnAction(event -> {
            Prisoner selectedPrisoner = prisonersTable.getSelectionModel().getSelectedItem();
            if (selectedPrisoner != null) {
                handleEditPrisoner(selectedPrisoner);
            }
        });
        
        MenuItem deletePrisonerItem = new MenuItem("Delete Prisoner");
        deletePrisonerItem.setOnAction(event -> {
            Prisoner selectedPrisoner = prisonersTable.getSelectionModel().getSelectedItem();
            if (selectedPrisoner != null) {
                handleDeletePrisoner(selectedPrisoner);
            }
        });
        
        contextMenu.getItems().addAll(viewDetailsItem, editPrisonerItem, deletePrisonerItem);
        return contextMenu;
    }

    private void handlePrisonerDetails(Prisoner prisoner) {
        try {
            System.out.println("Opening prisoner details for: " + prisoner.getName());
            PrisonerDetailsController controller = springFXMLLoader.loadAndGetController("/fxml/prisoner-details.fxml", PrisonerDetailsController.class);
            controller.setPrisoner(prisoner);
            if (controller.getRoot() == null) {
                System.err.println("[ERROR] PrisonerDetailsController.getRoot() is null!");
                showError("Navigation Error", "Failed to open prisoner details: root node is null");
                return;
            }
            Stage stage = new Stage();
            stage.setTitle("Prisoner Details - " + prisoner.getName());
            stage.setScene(new Scene(controller.getRoot()));
            stage.show();
            System.out.println("Prisoner details window opened successfully");
        } catch (Exception e) {
            System.err.println("Error opening prisoner details: " + e.getMessage());
            e.printStackTrace();
            showError("Navigation Error", "Failed to open prisoner details: " + e.getMessage());
        }
    }

    private void handleEditPrisoner(Prisoner prisoner) {
        try {
            System.out.println("Opening edit prisoner for: " + prisoner.getName());
            
            // Load the edit prisoner controller
            EditPrisonerController controller = springFXMLLoader.loadAndGetController("/fxml/edit-prisoner.fxml", EditPrisonerController.class);
            controller.setPrisoner(prisoner);
            
            // Create and show the edit prisoner window
            Stage stage = new Stage();
            stage.setTitle("Edit Prisoner - " + prisoner.getName());
            stage.setScene(new Scene(controller.getRoot()));
            stage.show();
            
            System.out.println("Edit prisoner window opened successfully");
        } catch (Exception e) {
            System.err.println("Error opening edit prisoner: " + e.getMessage());
            e.printStackTrace();
            showError("Navigation Error", "Failed to open edit prisoner: " + e.getMessage());
        }
    }
}
