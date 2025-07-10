package org.example.controllers;

import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.TextField;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import org.example.models.Staff;
import org.example.models.Department;
import org.example.services.StaffService;
import org.example.services.DepartmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.context.annotation.Scope;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Component
@Scope("prototype")
public class EditStaffController {
    @FXML
    private TextField nameField;
    @FXML
    private DatePicker dobField;
    @FXML
    private ComboBox<String> genderComboBox;
    @FXML
    private TextField roleField;
    @FXML
    private ComboBox<Department> departmentComboBox;
    @FXML
    private TextField salaryField;
    @FXML
    private TextField phoneField;
    @FXML
    private ComboBox<Staff> supervisorComboBox;

    private Staff staff;
    private Parent root;
    
    @Autowired
    private StaffService staffService;
    
    @Autowired
    private DepartmentService departmentService;

    public Parent getRoot() {
        return root;
    }

    public void setRoot(Parent root) {
        this.root = root;
    }

    @FXML
    public void initialize() {
        // Initialize gender combo box
        genderComboBox.getItems().addAll("MALE", "FEMALE", "OTHER");
        
        // Load departments
        loadDepartments();
        
        // Load potential supervisors
        loadSupervisors();
    }

    private void loadDepartments() {
        try {
            List<Department> departments = departmentService.getAllDepartments();
            departmentComboBox.setItems(javafx.collections.FXCollections.observableArrayList(departments));
            departmentComboBox.setConverter(new StringConverter<Department>() {
                @Override
                public String toString(Department department) {
                    return department == null ? "" : department.getType();
                }

                @Override
                public Department fromString(String string) {
                    return null; // Not needed for ComboBox
                }
            });
            
            // Add listener to refresh supervisors when department changes
            departmentComboBox.setOnAction(event -> {
                loadSupervisors();
                // Reset supervisor selection when department changes
                supervisorComboBox.setValue(null);
            });
        } catch (Exception e) {
            System.err.println("Error loading departments: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadSupervisors() {
        try {
            List<Staff> allStaff = staffService.getAllStaff();
            List<Staff> potentialSupervisors = new java.util.ArrayList<>();
            
            // Add warden(s) from any department
            for (Staff staffMember : allStaff) {
                if ("Warden".equalsIgnoreCase(staffMember.getRole()) || "Deputy Warden".equalsIgnoreCase(staffMember.getRole())) {
                    potentialSupervisors.add(staffMember);
                }
            }
            
            // Add staff from the selected department (either from staff object or combo box)
            Department selectedDepartment = null;
            if (staff != null && staff.getId() != null && staff.getDepartment() != null) {
                // Existing staff - use their department
                selectedDepartment = staff.getDepartment();
            } else if (departmentComboBox.getValue() != null) {
                // New staff - use selected department from combo box
                selectedDepartment = departmentComboBox.getValue();
            }
            
            if (selectedDepartment != null) {
                for (Staff departmentStaff : allStaff) {
                    if (departmentStaff.getDepartment() != null && 
                        departmentStaff.getDepartment().getId().equals(selectedDepartment.getId()) &&
                        !potentialSupervisors.contains(departmentStaff)) {
                        potentialSupervisors.add(departmentStaff);
                    }
                }
            }
            
            // Add "None" option at the beginning
            potentialSupervisors.add(0, null);
            
            supervisorComboBox.setItems(javafx.collections.FXCollections.observableArrayList(potentialSupervisors));
            supervisorComboBox.setConverter(new StringConverter<Staff>() {
                @Override
                public String toString(Staff staff) {
                    return staff == null ? "None" : staff.getName() + " (" + staff.getRole() + ")";
                }

                @Override
                public Staff fromString(String string) {
                    return null; // Not needed for ComboBox
                }
            });
        } catch (Exception e) {
            System.err.println("Error loading supervisors: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void setStaff(Staff staff) {
        this.staff = staff;
        populateFields();
    }

    private void populateFields() {
        if (staff != null && staff.getId() != null) {
            // Existing staff - populate with current values
            nameField.setText(staff.getName());
            dobField.setValue(staff.getDob());
            genderComboBox.setValue(staff.getGender());
            roleField.setText(staff.getRole());
            departmentComboBox.setValue(staff.getDepartment());
            salaryField.setText(staff.getSalary().toString());
            phoneField.setText(staff.getPhone());
            
            // Set current supervisor
            Staff currentSupervisor = staff.getSupervisor();
            supervisorComboBox.setValue(currentSupervisor);
        } else {
            // New staff - clear fields or set defaults
            nameField.setText("");
            dobField.setValue(null);
            genderComboBox.setValue(null);
            roleField.setText("");
            departmentComboBox.setValue(staff != null ? staff.getDepartment() : null);
            salaryField.setText("");
            phoneField.setText("");
            supervisorComboBox.setValue(null);
        }
    }

    @FXML
    private void handleSave() {
        try {
            // Validate input
            if (nameField.getText().trim().isEmpty()) {
                showError("Validation Error", "Name is required");
                return;
            }

            if (dobField.getValue() == null) {
                showError("Validation Error", "Date of Birth is required");
                return;
            }

            if (genderComboBox.getValue() == null) {
                showError("Validation Error", "Gender is required");
                return;
            }

            if (roleField.getText().trim().isEmpty()) {
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

            if (phoneField.getText().trim().isEmpty()) {
                showError("Validation Error", "Phone is required");
                return;
            }

            // Parse salary
            BigDecimal salary;
            try {
                salary = new BigDecimal(salaryField.getText().trim());
            } catch (NumberFormatException e) {
                showError("Validation Error", "Invalid salary format");
                return;
            }

            // Check if this is a new staff or existing staff
            boolean isNewStaff = (staff == null || staff.getId() == null);
            
            if (isNewStaff) {
                // Create new staff
                staff = new Staff();
            }

            // Update staff fields
            staff.setName(nameField.getText().trim());
            staff.setDob(dobField.getValue());
            staff.setGender(genderComboBox.getValue());
            staff.setRole(roleField.getText().trim());
            staff.setDepartment(departmentComboBox.getValue());
            staff.setSalary(salary);
            staff.setPhone(phoneField.getText().trim());
            
            // Save to database
            if (isNewStaff) {
                staffService.createStaff(staff);
                System.out.println("Staff created successfully. Closing window...");
            } else {
                staffService.updateStaff(staff.getId(), staff);
                System.out.println("Staff updated successfully. Closing window...");
            }
            
            // Handle supervisor assignment
            Staff selectedSupervisor = supervisorComboBox.getValue();
            if (staff.getId() != null) {
                if (selectedSupervisor != null) {
                    // Assign supervisor
                    staffService.assignSupervisor(staff.getId(), selectedSupervisor.getId());
                } else {
                    // Remove supervisor
                    staffService.removeSupervisor(staff.getId());
                }
            }
            
            // Show success message
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Success");
            alert.setHeaderText(isNewStaff ? "Staff Created" : "Staff Updated");
            alert.setContentText("Staff details have been saved successfully.");
            alert.showAndWait();
            
            // Close the window
            Stage stage = (Stage) nameField.getScene().getWindow();
            if (stage != null) {
                stage.close();
            } else {
                System.err.println("Stage is null, cannot close window");
            }
        } catch (Exception e) {
            System.err.println("Error saving staff: " + e.getMessage());
            e.printStackTrace();
            showError("Save Error", "Failed to save staff details", e.getMessage());
        }
    }

    @FXML
    private void handleCancel() {
        // Close the window without saving
        Stage stage = (Stage) nameField.getScene().getWindow();
        stage.close();
    }

    private void showError(String title, String header) {
        showError(title, header, null);
    }

    private void showError(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(header);
        if (content != null) {
            alert.setContentText(content);
        }
        alert.showAndWait();
    }
} 