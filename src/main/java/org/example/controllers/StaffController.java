package org.example.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import org.example.models.Staff;
import org.example.services.StaffService;
import org.example.config.SpringFXMLLoader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;
import java.util.logging.Level;

@Component
public class StaffController {
    private static final Logger LOGGER = Logger.getLogger(StaffController.class.getName());

    @FXML
    private TextField searchField;
    @FXML
    private TableView<Staff> staffTable;
    @FXML
    private TableColumn<Staff, Integer> idCol;
    @FXML
    private TableColumn<Staff, String> nameCol;
    @FXML
    private TableColumn<Staff, Integer> ageCol;
    @FXML
    private TableColumn<Staff, String> genderCol;
    @FXML
    private TableColumn<Staff, String> roleCol;
    @FXML
    private TableColumn<Staff, String> departmentCol;
    @FXML
    private TableColumn<Staff, String> supervisorCol;
    @FXML
    private TableColumn<Staff, String> salaryCol;
    @FXML
    private TableColumn<Staff, Void> actionsCol;

    private final StaffService staffService;
    private final ApplicationContext applicationContext;
    private final SpringFXMLLoader springFXMLLoader;
    private ObservableList<Staff> allStaff;
    private FilteredList<Staff> filteredStaff;

    @Autowired
    public StaffController(StaffService staffService, ApplicationContext applicationContext, SpringFXMLLoader springFXMLLoader) {
        this.staffService = staffService;
        this.applicationContext = applicationContext;
        this.springFXMLLoader = springFXMLLoader;
        LOGGER.info("StaffController initialized with dependencies");
    }

    @FXML
    public void initialize() {
        setupTableColumns();
        setupSearch();
        loadStaffData();
        setupTableClickHandlers();
    }

    private void setupTableColumns() {
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        ageCol.setCellValueFactory(cellData -> 
            new SimpleIntegerProperty(cellData.getValue().getAge()).asObject());
        genderCol.setCellValueFactory(new PropertyValueFactory<>("gender"));
        roleCol.setCellValueFactory(new PropertyValueFactory<>("role"));
        departmentCol.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getDepartment().getType()));
        supervisorCol.setCellValueFactory(cellData -> {
            Staff staff = cellData.getValue();
            Staff supervisor = staff.getSupervisor();
            return new SimpleStringProperty(supervisor != null ? supervisor.getName() : "None");
        });
        salaryCol.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getSalary().toString()));

        actionsCol.setCellFactory(col -> new TableCell<Staff, Void>() {
            private final Button deleteButton = new Button("Delete");
            {
                deleteButton.setOnAction(event -> {
                    Staff staff = getTableView().getItems().get(getIndex());
                    handleDeleteStaff(staff);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : deleteButton);
            }
        });
    }

    private void setupSearch() {
        // Initialize the filtered list
        allStaff = FXCollections.observableArrayList();
        filteredStaff = new FilteredList<>(allStaff, p -> true);

        // Bind the search field to the filter predicate
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredStaff.setPredicate(staff -> {
                // If search field is empty, display all staff
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }

                String lowerCaseFilter = newValue.toLowerCase();

                // Match against name
                if (staff.getName().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                }
                // Match against role
                if (staff.getRole().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                }
                // Match against department
                if (staff.getDepartment().getType().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                }
                // Match against gender
                if (staff.getGender().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                }
                // Match against supervisor
                Staff supervisor = staff.getSupervisor();
                if (supervisor != null && supervisor.getName().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                }
                return false;
            });
        });

        // Bind the filtered list to the table
        staffTable.setItems(filteredStaff);
    }

    private void loadStaffData() {
        try {
            List<Staff> staffList = staffService.getAllStaffWithSupervisors();
            allStaff.clear();
            allStaff.addAll(staffList);
        } catch (Exception e) {
            showError("Error loading staff", e.getMessage());
        }
    }

    private void setupTableClickHandlers() {
        // Double-click to open staff details
        staffTable.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                Staff selectedStaff = staffTable.getSelectionModel().getSelectedItem();
                if (selectedStaff != null) {
                    openStaffDetails(selectedStaff);
                }
            }
        });

        // Context menu for additional options
        ContextMenu contextMenu = new ContextMenu();
        MenuItem viewDetailsItem = new MenuItem("View Details");
        MenuItem editItem = new MenuItem("Edit Staff");
        MenuItem deleteItem = new MenuItem("Delete Staff");

        viewDetailsItem.setOnAction(event -> {
            Staff selectedStaff = staffTable.getSelectionModel().getSelectedItem();
            if (selectedStaff != null) {
                openStaffDetails(selectedStaff);
            }
        });

        editItem.setOnAction(event -> {
            Staff selectedStaff = staffTable.getSelectionModel().getSelectedItem();
            if (selectedStaff != null) {
                // TODO: Implement edit functionality
                showNotImplemented("Edit Staff");
            }
        });

        deleteItem.setOnAction(event -> {
            Staff selectedStaff = staffTable.getSelectionModel().getSelectedItem();
            if (selectedStaff != null) {
                handleDeleteStaff(selectedStaff);
            }
        });

        contextMenu.getItems().addAll(viewDetailsItem, editItem, deleteItem);
        staffTable.setContextMenu(contextMenu);
    }

    private void openStaffDetails(Staff staff) {
        try {
            Stage currentStage = (Stage) staffTable.getScene().getWindow();
            
            // Load the staff details FXML and get controller using SpringFXMLLoader
            StaffDetailsController controller = springFXMLLoader.loadAndGetController("/fxml/staff-details.fxml", StaffDetailsController.class);
            Parent root = controller.getRoot();
            
            // Get fresh staff data with all relationships
            Staff freshStaff = staffService.getStaffByIdWithRelations(staff.getId());
            
            // Set the staff data before showing the window
            controller.setStaff(freshStaff);
            controller.setPreviousPage("all_staff");
            
            // Create and show new scene
            Stage stage = new Stage();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("Staff Details - " + staff.getName());
            stage.show();
            
            LOGGER.info("Successfully opened staff details for: " + staff.getName());
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Unexpected error opening staff details", e);
            showError("Unexpected Error", "An unexpected error occurred: " + e.getMessage());
        }
    }

    @FXML
    private void handleBackToDashboard() {
        LOGGER.info("Handling back to dashboard action");
        try {
            // Close current window
            Stage currentStage = (Stage) staffTable.getScene().getWindow();
            
            // Load the dashboard FXML using SpringFXMLLoader
            Parent root = springFXMLLoader.load("/fxml/dashboard.fxml");
            
            // Create and show new scene
            Stage stage = new Stage();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("Prison Management System - Dashboard");
            stage.show();
            
            // Close the current window after showing the new one
            currentStage.close();
            
            LOGGER.info("Successfully navigated back to dashboard");
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to navigate back to dashboard", e);
            showError("Navigation Error", "Could not return to dashboard. Error: " + e.getMessage());
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Unexpected error during navigation", e);
            showError("Unexpected Error", "An unexpected error occurred: " + e.getMessage());
        }
    }

    private void handleDeleteStaff(Staff staff) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Staff");
        alert.setHeaderText("Delete Staff Member");
        alert.setContentText("Are you sure you want to delete " + staff.getName() + "?");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    staffService.deleteStaff(staff.getId());
                    loadStaffData();
                } catch (Exception e) {
                    showError("Error deleting staff", e.getMessage());
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

    @FXML
    private void handleAddStaff() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("fxml/add_staff.fxml"));
            loader.setControllerFactory(applicationContext::getBean);
            Parent root = loader.load();
            
            Stage stage = new Stage();
            stage.setTitle("Add New Staff");
            stage.setScene(new Scene(root));
            stage.showAndWait();
            
            // Refresh the table after adding
            loadStaffData();
        } catch (IOException e) {
            showError("Error", "Could not open add staff form: " + e.getMessage());
        }
    }

    @FXML
    private void handleSearch() {
        String searchText = searchField.getText().toLowerCase();
        filteredStaff.setPredicate(staff -> {
            if (searchText == null || searchText.isEmpty()) {
                return true;
            }
            return staff.getName().toLowerCase().contains(searchText) ||
                   staff.getRole().toLowerCase().contains(searchText) ||
                   staff.getDepartment().getName().toLowerCase().contains(searchText);
        });
    }

    @FXML
    private void handleApplyFilters() {
        // TODO: Implement filter logic based on selected ComboBox values
        showNotImplemented("Apply Filters");
    }

    @FXML
    private void handleResetFilters() {
        // Reset all filter ComboBoxes
        if (filterRoleComboBox != null) filterRoleComboBox.getSelectionModel().clearSelection();
        if (filterDepartmentComboBox != null) filterDepartmentComboBox.getSelectionModel().clearSelection();
        if (filterBlockComboBox != null) filterBlockComboBox.getSelectionModel().clearSelection();
        if (filterStatusComboBox != null) filterStatusComboBox.getSelectionModel().clearSelection();
        
        // Reset the search field
        if (searchField != null) searchField.clear();
        
        // Show all staff
        filteredStaff.setPredicate(staff -> true);
    }

    @FXML
    private void handleRefreshCharts() {
        // TODO: Implement chart refresh logic
        showNotImplemented("Refresh Charts");
    }

    @FXML
    private void handleScheduleLeave() {
        // TODO: Implement leave scheduling
        showNotImplemented("Schedule Leave");
    }

    @FXML
    private void handleAssignDepartment() {
        // TODO: Implement department assignment
        showNotImplemented("Assign Department");
    }

    @FXML
    private void handlePerformanceReview() {
        // TODO: Implement performance review
        showNotImplemented("Performance Review");
    }

    @FXML
    private void handleTrainingRecords() {
        // TODO: Implement training records
        showNotImplemented("Training Records");
    }

    @FXML
    private void handleGenerateReport() {
        // TODO: Implement report generation
        showNotImplemented("Generate Report");
    }

    @FXML
    private void handleExportData() {
        // TODO: Implement data export
        showNotImplemented("Export Data");
    }

    @FXML
    private void handlePrintList() {
        // TODO: Implement print functionality
        showNotImplemented("Print List");
    }

    @FXML
    private void handleBack() {
        handleBackToDashboard();
    }

    private void showNotImplemented(String feature) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Not Implemented");
        alert.setHeaderText(null);
        alert.setContentText(feature + " feature is not implemented yet.");
        alert.showAndWait();
    }

    // Add missing FXML fields
    @FXML
    private ComboBox<String> filterRoleComboBox;
    @FXML
    private ComboBox<String> filterDepartmentComboBox;
    @FXML
    private ComboBox<String> filterBlockComboBox;
    @FXML
    private ComboBox<String> filterStatusComboBox;
    @FXML
    private ComboBox<String> chartTypeComboBox;
    @FXML
    private ComboBox<String> roleChartTypeComboBox;

    // Add Staff form fields
    @FXML
    private TextField nameField;
    @FXML
    private ComboBox<String> genderComboBox;
    @FXML
    private DatePicker dobPicker;
    @FXML
    private ComboBox<String> roleComboBox;
    @FXML
    private ComboBox<String> departmentComboBox;
    @FXML
    private TextField salaryField;
    @FXML
    private ComboBox<String> blockComboBox;

    @FXML
    private void handleSaveStaff() {
        try {
            // Validate input
            if (nameField.getText().trim().isEmpty()) {
                showError("Validation Error", "Name is required");
                return;
            }

            if (genderComboBox.getValue() == null) {
                showError("Validation Error", "Gender is required");
                return;
            }

            if (dobPicker.getValue() == null) {
                showError("Validation Error", "Date of Birth is required");
                return;
            }

            if (roleComboBox.getValue() == null) {
                showError("Validation Error", "Role is required");
                return;
            }

            if (departmentComboBox.getValue() == null) {
                showError("Validation Error", "Department is required");
                return;
            }

            if (salaryField.getText().trim().isEmpty()) {
                showError("Validation Error", "Salary is required");
                return;
            }

            // Create new staff member
            Staff staff = new Staff();
            staff.setName(nameField.getText().trim());
            staff.setGender(genderComboBox.getValue());
            staff.setDob(dobPicker.getValue());
            staff.setRole(roleComboBox.getValue());
            // TODO: Set department and block based on selected values
            staff.setSalary(new java.math.BigDecimal(salaryField.getText().trim()));

            // Save staff member
            staffService.createStaff(staff);

            // Close the dialog
            Stage stage = (Stage) nameField.getScene().getWindow();
            stage.close();

            // Show success message
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Success");
            alert.setHeaderText(null);
            alert.setContentText("Staff member added successfully");
            alert.showAndWait();

        } catch (NumberFormatException e) {
            showError("Validation Error", "Invalid salary format");
        } catch (Exception e) {
            showError("Error", "Failed to save staff member: " + e.getMessage());
        }
    }

    @FXML
    private void handleCancelAddStaff() {
        Stage stage = (Stage) nameField.getScene().getWindow();
        stage.close();
    }

    private void initializeAddStaffForm() {
        // Initialize role choices
        roleComboBox.getItems().addAll(
            "Guard",
            "Medical Staff",
            "Administrative Staff",
            "Maintenance Staff",
            "Kitchen Staff"
        );

        // Initialize department choices
        // TODO: Load departments from database
        departmentComboBox.getItems().addAll(
            "Security",
            "Medical",
            "Administration",
            "Maintenance",
            "Kitchen"
        );

        // Initialize block choices
        // TODO: Load blocks from database
        blockComboBox.getItems().addAll(
            "Block A",
            "Block B",
            "Block C"
        );
    }
}
