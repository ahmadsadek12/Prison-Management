package org.example.controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.geometry.Pos;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.scene.Parent;
import javafx.scene.layout.VBox;
import org.example.models.Gun;
import org.example.models.Staff;
import org.example.services.GunService;
import org.example.services.GunAssignmentService;
import org.example.services.StaffService;
import org.example.config.SpringFXMLLoader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.logging.Level;

@Component
public class WeaponsManagementController implements Initializable {
    private static final Logger LOGGER = Logger.getLogger(WeaponsManagementController.class.getName());

    @FXML private TextField searchField;
    @FXML private ComboBox<String> filterTypeComboBox;
    @FXML private TableView<Gun> weaponsTable;
    @FXML private TableColumn<Gun, String> serialNumberColumn;
    @FXML private TableColumn<Gun, String> nameColumn;
    @FXML private TableColumn<Gun, String> typeColumn;
    @FXML private TableColumn<Gun, String> assignedToColumn;
    @FXML private TableColumn<Gun, Void> actionsColumn;

    @Autowired
    private GunService gunService;

    @Autowired
    private GunAssignmentService gunAssignmentService;

    @Autowired
    private StaffService staffService;

    @Autowired
    private SpringFXMLLoader springFXMLLoader;

    private ObservableList<Gun> allWeapons;
    private FilteredList<Gun> filteredWeapons;
    private Parent root;

    public Parent getRoot() {
        return root;
    }

    public void setRoot(Parent root) {
        this.root = root;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setupTable();
        setupSearchAndFilter();
        loadWeapons();
    }

    private void setupTable() {
        // Set up columns
        serialNumberColumn.setCellValueFactory(new PropertyValueFactory<>("serialNumber"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
        
        // Set up assigned to column
        assignedToColumn.setCellValueFactory(cellData -> {
            Gun gun = cellData.getValue();
            try {
                List<Staff> assignedStaff = gunAssignmentService.getStaffByGunId(gun.getSerialNumber());
                if (assignedStaff.isEmpty()) {
                    return new javafx.beans.property.SimpleStringProperty("Not Assigned");
                } else {
                    return new javafx.beans.property.SimpleStringProperty(assignedStaff.get(0).getName());
                }
            } catch (Exception e) {
                return new javafx.beans.property.SimpleStringProperty("Error");
            }
        });

        // Set up actions column
        actionsColumn.setCellFactory(col -> new TableCell<Gun, Void>() {
            private final Button editButton = new Button("Edit");
            private final Button deleteButton = new Button("Delete");
            private final HBox buttonBox = new HBox(5, editButton, deleteButton);

            {
                buttonBox.setAlignment(Pos.CENTER);
                
                // Style buttons
                editButton.setStyle("-fx-padding: 4 8 4 8; -fx-background-radius: 4; -fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-size: 10px; -fx-cursor: hand;");
                deleteButton.setStyle("-fx-padding: 4 8 4 8; -fx-background-radius: 4; -fx-background-color: #F44336; -fx-text-fill: white; -fx-font-size: 10px; -fx-cursor: hand;");
                
                editButton.setOnAction(event -> {
                    Gun gun = getTableView().getItems().get(getIndex());
                    handleEditWeapon(gun);
                });
                
                deleteButton.setOnAction(event -> {
                    Gun gun = getTableView().getItems().get(getIndex());
                    handleDeleteWeapon(gun);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(buttonBox);
                }
            }
        });
    }

    private void setupSearchAndFilter() {
        // Set up filter type combo box
        filterTypeComboBox.getItems().addAll("All", "Pistol", "Rifle", "Shotgun", "SMG", "Other");
        filterTypeComboBox.setValue("All");

        // Set up search functionality
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filterWeapons();
        });

        filterTypeComboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            filterWeapons();
        });
    }

    private void filterWeapons() {
        String searchText = searchField.getText().toLowerCase();
        String selectedType = filterTypeComboBox.getValue();

        filteredWeapons.setPredicate(weapon -> {
            boolean matchesSearch = weapon.getSerialNumber().toLowerCase().contains(searchText) ||
                                   weapon.getName().toLowerCase().contains(searchText) ||
                                   weapon.getType().toLowerCase().contains(searchText);
            
            boolean matchesType = "All".equals(selectedType) || weapon.getType().equals(selectedType);
            
            return matchesSearch && matchesType;
        });
    }

    private void loadWeapons() {
        try {
            List<Gun> weapons = gunService.getAllGuns();
            allWeapons = FXCollections.observableArrayList(weapons);
            filteredWeapons = new FilteredList<>(allWeapons, p -> true);
            weaponsTable.setItems(filteredWeapons);
            LOGGER.info("Loaded " + weapons.size() + " weapons");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error loading weapons", e);
            showError("Error", "Failed to load weapons", e.getMessage());
        }
    }

    @FXML
    private void handleAddWeapon() {
        try {
            // Create a simple dialog for adding a weapon
            Dialog<Gun> dialog = new Dialog<>();
            dialog.setTitle("Add New Weapon");
            dialog.setHeaderText("Enter weapon details");

            // Set button types
            ButtonType addButtonType = new ButtonType("Add", ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(addButtonType, ButtonType.CANCEL);

            // Create the custom content
            VBox content = new VBox(10);
            content.setPadding(new javafx.geometry.Insets(10));

            TextField serialNumberField = new TextField();
            serialNumberField.setPromptText("Serial Number");
            
            TextField nameField = new TextField();
            nameField.setPromptText("Weapon Name");
            
            ComboBox<String> typeComboBox = new ComboBox<>();
            typeComboBox.getItems().addAll("Pistol", "Rifle", "Shotgun", "SMG", "Other");
            typeComboBox.setPromptText("Select Type");

            content.getChildren().addAll(
                new Label("Serial Number:"), serialNumberField,
                new Label("Name:"), nameField,
                new Label("Type:"), typeComboBox
            );

            dialog.getDialogPane().setContent(content);

            // Convert the result
            dialog.setResultConverter(dialogButton -> {
                if (dialogButton == addButtonType) {
                    if (serialNumberField.getText().trim().isEmpty() ||
                        nameField.getText().trim().isEmpty() ||
                        typeComboBox.getValue() == null) {
                        showError("Validation Error", "All fields are required");
                        return null;
                    }
                    
                    Gun newWeapon = new Gun(
                        serialNumberField.getText().trim(),
                        typeComboBox.getValue(),
                        nameField.getText().trim()
                    );
                    return newWeapon;
                }
                return null;
            });

            Optional<Gun> result = dialog.showAndWait();
            result.ifPresent(weapon -> {
                try {
                    gunService.createGun(weapon);
                    showSuccess("Weapon added successfully");
                    loadWeapons(); // Refresh the table
                } catch (Exception e) {
                    LOGGER.log(Level.SEVERE, "Error adding weapon", e);
                    showError("Error", "Failed to add weapon", e.getMessage());
                }
            });

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error opening add weapon dialog", e);
            showError("Error", "Failed to open add weapon dialog", e.getMessage());
        }
    }

    private void handleEditWeapon(Gun weapon) {
        try {
            // Create a dialog for editing the weapon
            Dialog<Gun> dialog = new Dialog<>();
            dialog.setTitle("Edit Weapon");
            dialog.setHeaderText("Edit weapon details");

            // Set button types
            ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

            // Create the custom content
            VBox content = new VBox(10);
            content.setPadding(new javafx.geometry.Insets(10));

            TextField serialNumberField = new TextField(weapon.getSerialNumber());
            serialNumberField.setPromptText("Serial Number");
            serialNumberField.setEditable(false);
            serialNumberField.setStyle("-fx-background-color: #f0f0f0;");
            
            TextField nameField = new TextField(weapon.getName());
            nameField.setPromptText("Weapon Name");
            
            ComboBox<String> typeComboBox = new ComboBox<>();
            typeComboBox.getItems().addAll("Pistol", "Rifle", "Shotgun", "SMG", "Other");
            typeComboBox.setValue(weapon.getType());

            content.getChildren().addAll(
                new Label("Serial Number:"), serialNumberField,
                new Label("Name:"), nameField,
                new Label("Type:"), typeComboBox
            );

            dialog.getDialogPane().setContent(content);

            // Convert the result
            dialog.setResultConverter(dialogButton -> {
                if (dialogButton == saveButtonType) {
                    if (nameField.getText().trim().isEmpty() ||
                        typeComboBox.getValue() == null) {
                        showError("Validation Error", "All fields are required");
                        return null;
                    }
                    
                    Gun updatedWeapon = new Gun(
                        weapon.getSerialNumber(),
                        typeComboBox.getValue(),
                        nameField.getText().trim()
                    );
                    return updatedWeapon;
                }
                return null;
            });

            Optional<Gun> result = dialog.showAndWait();
            result.ifPresent(updatedWeapon -> {
                try {
                    // Use the original serial number for the update
                    gunService.updateGun(weapon.getSerialNumber(), updatedWeapon);
                    showSuccess("Weapon updated successfully");
                    
                    // Update the weapon in the observable list
                    int index = allWeapons.indexOf(weapon);
                    if (index != -1) {
                        allWeapons.set(index, updatedWeapon);
                    }
                    
                    // Refresh the table
                    weaponsTable.refresh();
                    
                } catch (Exception e) {
                    LOGGER.log(Level.SEVERE, "Error updating weapon", e);
                    showError("Error", "Failed to update weapon", e.getMessage());
                }
            });

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error opening edit weapon dialog", e);
            showError("Error", "Failed to open edit weapon dialog", e.getMessage());
        }
    }

    private void handleDeleteWeapon(Gun weapon) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Weapon");
        alert.setHeaderText("Delete Weapon");
        alert.setContentText("Are you sure you want to delete weapon " + weapon.getSerialNumber() + "?\nThis action cannot be undone.");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                // Check if weapon is assigned to any staff
                List<Staff> assignedStaff = gunAssignmentService.getStaffByGunId(weapon.getSerialNumber());
                if (!assignedStaff.isEmpty()) {
                    showError("Cannot Delete", 
                             "Weapon is currently assigned", 
                             "Cannot delete weapon " + weapon.getSerialNumber() + 
                             " because it is assigned to " + assignedStaff.get(0).getName() + 
                             ". Please remove the assignment first.");
                    return;
                }
                
                gunService.deleteGun(weapon.getSerialNumber());
                showSuccess("Weapon deleted successfully");
                
                // Remove the weapon from the observable list
                allWeapons.remove(weapon);
                
                // Refresh the table
                weaponsTable.refresh();
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Error deleting weapon", e);
                showError("Error", "Failed to delete weapon", e.getMessage());
            }
        }
    }

    @FXML
    private void handleSearch() {
        filterWeapons();
    }

    @FXML
    private void handleClear() {
        searchField.clear();
        filterTypeComboBox.setValue("All");
        filterWeapons();
    }

    private void showError(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void showError(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void showSuccess(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
} 