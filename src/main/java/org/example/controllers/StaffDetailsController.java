package org.example.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.example.models.Staff;
import org.example.models.Gun;
import org.example.services.StaffService;
import org.example.services.GunService;
import org.example.services.GunAssignmentService;
import org.example.config.SpringFXMLLoader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Optional;

@Component
public class StaffDetailsController implements Initializable {

    @FXML private Label staffIdLabel;
    @FXML private Label staffNameLabel;
    @FXML private Label staffAgeLabel;
    @FXML private Label staffGenderLabel;
    @FXML private Label staffRoleLabel;
    @FXML private Label staffDepartmentLabel;
    @FXML private Label staffSalaryLabel;
    @FXML private Label staffPhoneLabel;
    @FXML private Label staffDobLabel;
    
    // Gun-related FXML elements
    @FXML private TableView<Gun> gunsTable;
    @FXML private TableColumn<Gun, String> gunSerialNumberColumn;
    @FXML private TableColumn<Gun, String> gunNameColumn;
    @FXML private TableColumn<Gun, String> gunTypeColumn;
    @FXML private TableColumn<Gun, Void> gunActionsColumn;

    @Autowired
    private StaffService staffService;
    
    @Autowired
    private GunService gunService;
    
    @Autowired
    private GunAssignmentService gunAssignmentService;
    
    @Autowired
    private ApplicationContext applicationContext;
    
    @Autowired
    private SpringFXMLLoader springFXMLLoader;

    private Staff currentStaff;
    private String previousPage;
    private Parent root;

    public Parent getRoot() {
        return root;
    }

    public void setRoot(Parent root) {
        this.root = root;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            setupGunsTable();
            System.out.println("[DEBUG] StaffDetailsController initialized successfully.");
        } catch (Exception e) {
            System.err.println("[ERROR] Exception in StaffDetailsController.initialize(): " + e);
            e.printStackTrace();
            showError("Initialization Error", "Failed to initialize staff details", e.getMessage());
        }
    }

    private void setupGunsTable() {
        gunSerialNumberColumn.setCellValueFactory(new PropertyValueFactory<>("serialNumber"));
        gunNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        gunTypeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));

        // Add remove button to gunActionsColumn
        gunActionsColumn.setCellFactory(col -> {
            return new TableCell<Gun, Void>() {
                private final Button removeButton = new Button("Remove");
                {
                    removeButton.setStyle("-fx-font-size: 10px; -fx-padding: 2px 5px;");
                    
                    removeButton.setOnAction(event -> {
                        Gun gun = getTableView().getItems().get(getIndex());
                        StaffDetailsController.this.handleRemoveGun(gun);
                    });
                }
                @Override
                protected void updateItem(Void item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) {
                        setGraphic(null);
                    } else {
                        setGraphic(removeButton);
                    }
                }
            };
        });
    }

    public void setStaff(Staff staff) {
        System.out.println("[DEBUG] Setting staff in StaffDetailsController: " + (staff != null ? staff.getName() + " (ID: " + staff.getId() + ")" : "NULL"));
        this.currentStaff = staff;
        loadStaffDetails();
        loadGuns();
    }

    public void setPreviousPage(String previousPage) {
        this.previousPage = previousPage;
    }

    private void loadStaffDetails() {
        System.out.println("[DEBUG] Loading staff details for: " + (currentStaff != null ? currentStaff.getName() : "NULL"));
        if (currentStaff == null) return;

        try {
            staffIdLabel.setText(String.valueOf(currentStaff.getId()));
            staffNameLabel.setText(currentStaff.getName());
            staffAgeLabel.setText(String.valueOf(currentStaff.getAge()));
            staffGenderLabel.setText(currentStaff.getGender());
            staffRoleLabel.setText(currentStaff.getRole());
            staffDepartmentLabel.setText(currentStaff.getDepartment() != null ? currentStaff.getDepartment().getType() : "N/A");
            staffSalaryLabel.setText("$" + String.format("%,.2f", currentStaff.getSalary()));
            staffPhoneLabel.setText(currentStaff.getPhone());
            staffDobLabel.setText(currentStaff.getDob() != null ? currentStaff.getDob().toString() : "N/A");
            System.out.println("[DEBUG] Staff details loaded successfully");
        } catch (Exception e) {
            System.err.println("[ERROR] Failed to load staff details: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadGuns() {
        if (currentStaff == null) return;

        try {
            List<Gun> assignedGuns = gunService.getGunsByAssignedStaffId(currentStaff.getId());
            ObservableList<Gun> gunsList = FXCollections.observableArrayList(assignedGuns);
            gunsTable.setItems(gunsList);
            System.out.println("[DEBUG] Loaded " + assignedGuns.size() + " guns for staff ID: " + currentStaff.getId());
        } catch (Exception e) {
            System.err.println("[ERROR] Failed to load guns: " + e.getMessage());
            e.printStackTrace();
            showError("Data Loading Error", "Failed to load assigned guns", e.getMessage());
        }
    }

    @FXML
    private void handleAssignGun() {
        try {
            // Check if staff is in Security department
            if (currentStaff.getDepartment() == null || 
                !currentStaff.getDepartment().getType().toLowerCase().contains("security")) {
                showError("Gun Assignment Error", 
                         "Gun assignment is restricted", 
                         "Only staff members in the Security department can be assigned guns.\n" +
                         "Current department: " + (currentStaff.getDepartment() != null ? 
                                                  currentStaff.getDepartment().getType() : "None"));
                return;
            }
            
            // Create a dialog to select a gun to assign
            Dialog<Gun> dialog = new Dialog<>();
            dialog.setTitle("Assign Gun");
            dialog.setHeaderText("Select a gun to assign to " + currentStaff.getName());

            // Set the button types
            ButtonType assignButtonType = new ButtonType("Assign", ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(assignButtonType, ButtonType.CANCEL);

            // Create the custom content
            VBox content = new VBox(10);
            content.setPadding(new javafx.geometry.Insets(10));

            ComboBox<Gun> gunComboBox = new ComboBox<>();
            List<Gun> availableGuns = gunService.getAllGuns();
            // Filter out already assigned guns
            List<Gun> assignedGuns = gunService.getGunsByAssignedStaffId(currentStaff.getId());
            availableGuns.removeAll(assignedGuns);
            gunComboBox.setItems(FXCollections.observableArrayList(availableGuns));
            gunComboBox.setCellFactory(param -> new ListCell<Gun>() {
                @Override
                protected void updateItem(Gun item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText(item.getSerialNumber() + " - " + item.getName() + " (" + item.getType() + ")");
                    }
                }
            });
            gunComboBox.setButtonCell(gunComboBox.getCellFactory().call(null));

            content.getChildren().addAll(new Label("Select Gun:"), gunComboBox);
            dialog.getDialogPane().setContent(content);

            // Request focus on the gun combo box
            gunComboBox.requestFocus();

            // Convert the result to a gun when the assign button is clicked
            dialog.setResultConverter(dialogButton -> {
                if (dialogButton == assignButtonType) {
                    return gunComboBox.getValue();
                }
                return null;
            });

            Optional<Gun> result = dialog.showAndWait();
            result.ifPresent(gun -> {
                try {
                    gunAssignmentService.assignGunToStaff(gun.getSerialNumber(), currentStaff.getId());
                    showSuccess("Gun assigned successfully");
                    loadGuns(); // Refresh the table
                } catch (Exception e) {
                    System.err.println("[ERROR] Failed to assign gun: " + e.getMessage());
                    e.printStackTrace();
                    showError("Assignment Error", "Failed to assign gun", e.getMessage());
                }
            });

        } catch (Exception e) {
            System.err.println("[ERROR] Error in handleAssignGun: " + e.getMessage());
            e.printStackTrace();
            showError("Error", "Failed to open gun assignment dialog", e.getMessage());
        }
    }

    private void handleRemoveGun(Gun gun) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Remove Gun Assignment");
        alert.setHeaderText("Remove Gun Assignment");
        alert.setContentText("Are you sure you want to remove the assignment of gun " + gun.getSerialNumber() + " from " + currentStaff.getName() + "?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                gunAssignmentService.removeGunAssignment(gun.getSerialNumber(), currentStaff.getId());
                showSuccess("Gun assignment removed successfully");
                loadGuns(); // Refresh the table
            } catch (Exception e) {
                System.err.println("[ERROR] Failed to remove gun assignment: " + e.getMessage());
                e.printStackTrace();
                showError("Removal Error", "Failed to remove gun assignment", e.getMessage());
            }
        }
    }

    @FXML
    private void handleEdit() {
        try {
            // Get fresh staff data with all relationships
            Staff freshStaff = staffService.getStaffByIdWithRelations(currentStaff.getId());
            
            EditStaffController controller = springFXMLLoader.loadAndGetController("/fxml/edit-staff.fxml", EditStaffController.class);
            controller.setStaff(freshStaff);

            Stage stage = new Stage();
            stage.setTitle("Edit Staff - " + freshStaff.getName());
            stage.setScene(new Scene(controller.getRoot()));
            stage.showAndWait(); // Wait for the dialog to close
            
            // Get fresh staff data after editing and update the current staff
            Staff updatedStaff = staffService.getStaffByIdWithRelations(currentStaff.getId());
            this.currentStaff = updatedStaff;
            
            // Refresh the staff details after editing
            loadStaffDetails();
            loadGuns();
            
        } catch (Exception e) {
            System.err.println("[ERROR] Failed to open edit staff dialog: " + e.getMessage());
            e.printStackTrace();
            showError("Edit Error", "Failed to open edit dialog", e.getMessage());
        }
    }

    private void showAlert(String title, String content, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void showError(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(header);
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