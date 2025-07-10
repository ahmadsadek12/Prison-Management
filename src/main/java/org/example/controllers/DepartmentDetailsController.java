package org.example.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import javafx.stage.Popup;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Pos;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Tooltip;
import org.example.models.Department;
import org.example.models.Staff;
import org.example.models.Block;
import org.example.models.Schedule;
import org.example.services.DepartmentService;
import org.example.services.StaffService;
import org.example.services.BlockService;
import org.example.services.ScheduleService;
import org.example.config.SpringFXMLLoader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.util.List;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.Map;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.Set;
import java.util.Optional;

@Component
public class DepartmentDetailsController {
    private static final Logger LOGGER = Logger.getLogger(DepartmentDetailsController.class.getName());
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    
    // Global popup management to prevent multiple popups
    private static Popup currentSchedulePopup = null;
    private static javafx.animation.Timeline currentHideTimer = null;

    @FXML private Label departmentNameLabel;
    @FXML private Label departmentHeadLabel;
    @FXML private Label departmentDescriptionLabel;
    @FXML private Label departmentBlockLabel;

    @FXML private TableView<Staff> staffTable;
    @FXML private TableColumn<Staff, Integer> staffIdColumn;
    @FXML private TableColumn<Staff, String> staffNameColumn;
    @FXML private TableColumn<Staff, String> staffRoleColumn;
    @FXML private TableColumn<Staff, String> staffSupervisorColumn;
    @FXML private TableColumn<Staff, Void> staffActionsColumn;

    private final DepartmentService departmentService;
    private final StaffService staffService;
    private final BlockService blockService;
    private final ScheduleService scheduleService;
    private Department department;
    private Block block;
    private Parent root;

    @Autowired
    private SpringFXMLLoader springFXMLLoader;

    @Autowired
    public DepartmentDetailsController(DepartmentService departmentService, StaffService staffService, BlockService blockService, ScheduleService scheduleService) {
        this.departmentService = departmentService;
        this.staffService = staffService;
        this.blockService = blockService;
        this.scheduleService = scheduleService;
    }

    public Parent getRoot() {
        return root;
    }

    public void setRoot(Parent root) {
        this.root = root;
    }

    @FXML
    public void initialize() {
        setupStaffTable();
    }

    private void setupStaffTable() {
        // Initialize staff table columns
        staffIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        staffNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        staffRoleColumn.setCellValueFactory(new PropertyValueFactory<>("role"));
        staffSupervisorColumn.setCellValueFactory(cellData -> {
            Staff staff = cellData.getValue();
            Staff supervisor = staff.getSupervisor();
            return new SimpleStringProperty(supervisor != null ? supervisor.getName() : "None");
        });
        
        // Set up actions column with Edit, Delete, and Show Schedule buttons
        staffActionsColumn.setCellFactory(col -> new TableCell<Staff, Void>() {
            private final Button editButton = new Button("Edit");
            private final Button deleteButton = new Button("Delete");
            private final Button scheduleButton = new Button("📅");
            private final HBox buttonBox = new HBox(6, editButton, deleteButton, scheduleButton);
            {
                buttonBox.setAlignment(Pos.CENTER);
                
                // Improved button styling with better spacing and visibility
                editButton.setStyle("-fx-padding: 6 12 6 12; -fx-background-radius: 6; -fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-size: 12px; -fx-font-weight: bold; -fx-min-width: 60px; -fx-cursor: hand; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 2, 0, 0, 1);");
                deleteButton.setStyle("-fx-padding: 6 12 6 12; -fx-background-radius: 6; -fx-background-color: #F44336; -fx-text-fill: white; -fx-font-size: 12px; -fx-font-weight: bold; -fx-min-width: 60px; -fx-cursor: hand; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 2, 0, 0, 1);");
                scheduleButton.setStyle("-fx-padding: 6 8 6 8; -fx-background-radius: 6; -fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-size: 14px; -fx-min-width: 35px; -fx-cursor: hand; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 2, 0, 0, 1);");
                
                // Add hover effects
                editButton.setOnMouseEntered(e -> editButton.setStyle("-fx-padding: 6 12 6 12; -fx-background-radius: 6; -fx-background-color: #1976D2; -fx-text-fill: white; -fx-font-size: 12px; -fx-font-weight: bold; -fx-min-width: 60px; -fx-cursor: hand; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 3, 0, 0, 2);"));
                editButton.setOnMouseExited(e -> editButton.setStyle("-fx-padding: 6 12 6 12; -fx-background-radius: 6; -fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-size: 12px; -fx-font-weight: bold; -fx-min-width: 60px; -fx-cursor: hand; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 2, 0, 0, 1);"));
                
                deleteButton.setOnMouseEntered(e -> deleteButton.setStyle("-fx-padding: 6 12 6 12; -fx-background-radius: 6; -fx-background-color: #D32F2F; -fx-text-fill: white; -fx-font-size: 12px; -fx-font-weight: bold; -fx-min-width: 60px; -fx-cursor: hand; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 3, 0, 0, 2);"));
                deleteButton.setOnMouseExited(e -> deleteButton.setStyle("-fx-padding: 6 12 6 12; -fx-background-radius: 6; -fx-background-color: #F44336; -fx-text-fill: white; -fx-font-size: 12px; -fx-font-weight: bold; -fx-min-width: 60px; -fx-cursor: hand; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 2, 0, 0, 1);"));
                
                scheduleButton.setOnMouseEntered(e -> scheduleButton.setStyle("-fx-padding: 6 8 6 8; -fx-background-radius: 6; -fx-background-color: #388E3C; -fx-text-fill: white; -fx-font-size: 14px; -fx-min-width: 35px; -fx-cursor: hand; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 3, 0, 0, 2);"));
                scheduleButton.setOnMouseExited(e -> scheduleButton.setStyle("-fx-padding: 6 8 6 8; -fx-background-radius: 6; -fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-size: 14px; -fx-min-width: 35px; -fx-cursor: hand; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 2, 0, 0, 1);"));
                
                // Set tooltip for schedule button
                scheduleButton.setTooltip(new Tooltip("Show Schedule"));
                
                editButton.setOnAction(event -> {
                    Staff staff = getTableView().getItems().get(getIndex());
                    try {
                        EditStaffController controller = springFXMLLoader.loadAndGetController("/fxml/edit-staff.fxml", EditStaffController.class);
                        controller.setStaff(staff);
                        
                        Stage dialogStage = new Stage();
                        dialogStage.setTitle("Edit Staff - " + staff.getName());
                        dialogStage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
                        dialogStage.initOwner(departmentNameLabel.getScene().getWindow());
                        dialogStage.setScene(new javafx.scene.Scene(controller.getRoot()));
                        dialogStage.showAndWait();
                        
                        // Refresh entire UI to update department head and other info
                        updateUI();
                    } catch (Exception e) {
                        LOGGER.log(Level.SEVERE, "Error opening edit staff dialog", e);
                        showError("Error", "Failed to open edit staff dialog: " + e.getMessage());
                    }
                });
                deleteButton.setOnAction(event -> {
                    Staff staff = getTableView().getItems().get(getIndex());
                    handleDeleteStaff(staff);
                });
                scheduleButton.setOnMouseEntered(event -> {
                    Staff staff = getTableView().getItems().get(getIndex());
                    // Cancel any existing hide timer
                    if (currentHideTimer != null) {
                        currentHideTimer.stop();
                    }
                    // Close any existing popup before showing a new one
                    if (currentSchedulePopup != null) {
                        currentSchedulePopup.hide();
                        currentSchedulePopup = null;
                    }
                    currentSchedulePopup = showStaffSchedulePopup(staff, scheduleButton, currentHideTimer);
                });
                scheduleButton.setOnMouseExited(event -> {
                    // Start a timer to hide the popup after a delay
                    if (currentHideTimer != null) {
                        currentHideTimer.stop();
                    }
                    currentHideTimer = new javafx.animation.Timeline(new javafx.animation.KeyFrame(
                        javafx.util.Duration.millis(1000), // 1000ms delay (1 second)
                        e -> {
                            if (currentSchedulePopup != null) {
                                currentSchedulePopup.hide();
                                currentSchedulePopup = null;
                            }
                        }
                    ));
                    currentHideTimer.play();
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
                setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
            }
        });
        
        // Add double-click functionality to open staff details
        staffTable.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                Staff selectedStaff = staffTable.getSelectionModel().getSelectedItem();
                if (selectedStaff != null) {
                    openStaffDetails(selectedStaff);
                }
            }
        });
    }

    public void initData(Department department) {
        this.department = department;
        this.block = department.getContains2Relations().stream()
            .map(contains2 -> contains2.getBlock())
            .findFirst()
            .orElse(null);
        updateUI();
    }

    private void updateUI() {
        if (department != null) {
            departmentNameLabel.setText(department.getType());
            departmentDescriptionLabel.setText("Status: " + department.getStatus());
            
            // Get department head (first staff member with role "Head" or "Manager")
            String departmentHead = staffService.getStaffByDepartment(department.getId()).stream()
                .filter(staff -> staff.getRole().toLowerCase().contains("head") || 
                               staff.getRole().toLowerCase().contains("manager"))
                .map(Staff::getName)
                .findFirst()
                .orElse("Not assigned");
            departmentHeadLabel.setText(departmentHead);
            
            // Update block information and maintain the block reference
            Block currentBlock = department.getContains2Relations().stream()
                .map(contains2 -> {
                    try {
                        return contains2.getBlock();
                    } catch (Exception e) {
                        LOGGER.warning("Could not get block: " + e.getMessage());
                        return null;
                    }
                })
                .findFirst()
                .orElse(null);
            
            // Update the block reference
            this.block = currentBlock;
            
            // Get assigned block name for display
            String assignedBlock = currentBlock != null ? currentBlock.getType() : "Not assigned";
            departmentBlockLabel.setText(assignedBlock);
            
            // Load staff
            loadStaff();
        }
    }

    private void loadStaff() {
        try {
            // Force a fresh database query to get updated staff data
            List<Staff> staffList = staffService.getStaffByDepartment(department.getId());
            ObservableList<Staff> observableStaffList = FXCollections.observableArrayList(staffList);
            staffTable.setItems(observableStaffList);
            
            // Force table refresh
            staffTable.refresh();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error loading staff", e);
            showError("Error", "Failed to load staff: " + e.getMessage());
        }
    }

    @FXML
    private void handleDeleteStaff(Staff staff) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Staff");
        alert.setHeaderText("Delete Staff");
        alert.setContentText("Are you sure you want to delete this staff member? This action cannot be undone.");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    staffService.deleteStaff(staff.getId());
                    loadStaff(); // Refresh the table
                } catch (Exception e) {
                    showError("Error", "Failed to delete staff: " + e.getMessage());
                }
            }
        });
    }

    @FXML
    private void handleClose() {
        try {
            // Clean up any open popup
            if (currentSchedulePopup != null) {
                currentSchedulePopup.hide();
                currentSchedulePopup = null;
            }
            if (currentHideTimer != null) {
                currentHideTimer.stop();
                currentHideTimer = null;
            }
            
            // Close current window
            Stage currentStage = (Stage) departmentNameLabel.getScene().getWindow();
            currentStage.close();

            // Return to the block details page
            if (block != null) {
                // Reload block with all relationships to avoid lazy loading issues
                Block reloadedBlock = blockService.getBlockByIdWithRelations(block.getId());
                BlockDetailsController controller = springFXMLLoader.loadAndGetController("/fxml/block-details.fxml", BlockDetailsController.class);
                controller.initData(reloadedBlock);

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
            LOGGER.log(Level.SEVERE, "Error returning to block details", e);
            showError("Error", "Failed to return to block details: " + e.getMessage());
        }
    }

    @FXML
    private void handleEdit() {
        try {
            // Load the edit-department dialog
            EditDepartmentController controller = springFXMLLoader.loadAndGetController("/fxml/edit-department.fxml", EditDepartmentController.class);
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Edit Department");
            dialogStage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            dialogStage.initOwner(departmentNameLabel.getScene().getWindow());
            dialogStage.setScene(new javafx.scene.Scene(controller.getRoot()));
            controller.setDialogStage(dialogStage);
            controller.initData(department, () -> {
                // Refresh department details after editing
                Department refreshed = departmentService.getDepartmentByIdWithRelations(department.getId());
                initData(refreshed);
            });
            dialogStage.showAndWait();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error opening edit department dialog", e);
            showError("Error", "Failed to open edit department dialog: " + e.getMessage());
        }
    }

    @FXML
    private void handleAddStaff() {
        try {
            // Create a new staff object for the department
            Staff newStaff = new Staff();
            newStaff.setDepartment(department);
            
            EditStaffController controller = springFXMLLoader.loadAndGetController("/fxml/edit-staff.fxml", EditStaffController.class);
            controller.setStaff(newStaff);
            
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Add Staff");
            dialogStage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            dialogStage.initOwner(departmentNameLabel.getScene().getWindow());
            dialogStage.setScene(new javafx.scene.Scene(controller.getRoot()));
            dialogStage.showAndWait();
            
            // Refresh entire UI to update department head and other info
            updateUI();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error opening add staff dialog", e);
            showError("Error", "Failed to open add staff dialog: " + e.getMessage());
        }
    }

    @FXML
    private void handleViewExpenses() {
        try {
            // Load the department expenses page
            DepartmentExpensesController controller = springFXMLLoader.loadAndGetController("/fxml/department-expenses.fxml", DepartmentExpensesController.class);
            Stage stage = new Stage();
            stage.setTitle("Department Expenses");
            stage.setScene(new Scene(controller.getRoot()));
            controller.initData(department);
            stage.show();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error opening expenses page", e);
            showError("Error", "Failed to open expenses page: " + e.getMessage());
        }
    }

    @FXML
    private void handleExport() {
        showError("Not implemented", "Export data functionality is not implemented yet.");
    }

    private void showError(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void openStaffDetails(Staff staff) {
        try {
            // Load the staff details FXML and get controller using SpringFXMLLoader
            StaffDetailsController controller = springFXMLLoader.loadAndGetController("/fxml/staff-details.fxml", StaffDetailsController.class);
            Parent root = controller.getRoot();
            
            // Get fresh staff data with all relationships
            Staff freshStaff = staffService.getStaffByIdWithRelations(staff.getId());
            
            // Set the staff data before showing the window
            controller.setStaff(freshStaff);
            controller.setPreviousPage("department_details");
            
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

    private Popup showStaffSchedulePopup(Staff staff, Button sourceButton, javafx.animation.Timeline hideTimer) {
        try {
            // Get staff schedules
            List<Schedule> schedules = scheduleService.getSchedulesByStaffId(staff.getId());
            
            // Create schedule popup content
            VBox content = new VBox(8);
            content.setStyle("-fx-padding: 15px; -fx-background-color: white; -fx-border-color: #ccc; -fx-border-radius: 5px;");
            
            // Header
            Label headerLabel = new Label(staff.getName() + "'s Schedule");
            headerLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #333;");
            content.getChildren().add(headerLabel);
            
            // Create and show popup
            final Popup popup = new Popup();
            popup.getContent().add(content);
            popup.setAutoHide(false); // Don't auto-hide so we can control when it closes
            
            // Add mouse events to the popup content to keep it open when hovering over it
            content.setOnMouseEntered(e -> {
                // Cancel any hide timer when hovering over popup content
                if (currentHideTimer != null) {
                    currentHideTimer.stop();
                }
            });
            
            content.setOnMouseExited(e -> {
                // Start a timer to hide the popup when mouse leaves content
                if (currentHideTimer != null) {
                    currentHideTimer.stop();
                }
                currentHideTimer = new javafx.animation.Timeline(new javafx.animation.KeyFrame(
                    javafx.util.Duration.millis(300), // 300ms delay
                    ev -> {
                        if (currentSchedulePopup != null) {
                            currentSchedulePopup.hide();
                            currentSchedulePopup = null;
                        }
                    }
                ));
                currentHideTimer.play();
            });
            
            if (schedules.isEmpty()) {
                // No schedule - show message and add button
                Label noScheduleLabel = new Label("No schedule assigned");
                noScheduleLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #666; -fx-padding: 10px 0;");
                content.getChildren().add(noScheduleLabel);
                
                Button addScheduleButton = new Button("Add Schedule");
                addScheduleButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-padding: 8px 16px; -fx-cursor: hand;");
                addScheduleButton.setOnAction(e -> {
                    // Close current popup
                    if (currentSchedulePopup != null) {
                        currentSchedulePopup.hide();
                        currentSchedulePopup = null;
                    }
                    // Open add schedule dialog
                    openAddScheduleDialog(staff);
                });
                content.getChildren().add(addScheduleButton);
            } else {
                // Schedule details - handle lazy loading issue
                for (Schedule schedule : schedules) {
                    VBox scheduleBox = new VBox(5);
                    scheduleBox.setStyle("-fx-padding: 8px; -fx-background-color: #f5f5f5; -fx-border-radius: 3px;");
                    
                    Label timeLabel = new Label("Time: " + schedule.getStart().format(DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)) + 
                                            " - " + schedule.getEnd().format(DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)));
                    timeLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 12px;");
                    scheduleBox.getChildren().add(timeLabel);
                    
                    // Add schedule type
                    String scheduleType = schedule.isFullTime() ? "Full Time" : "Part Time";
                    Label typeLabel = new Label("Type: " + scheduleType);
                    typeLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #666;");
                    scheduleBox.getChildren().add(typeLabel);
                    
                    // Sort days from Monday to Sunday
                    List<String> sortedDays = new ArrayList<>(schedule.getDays());
                    sortedDays.sort((day1, day2) -> {
                        Map<String, Integer> dayOrder = Map.of(
                            "Monday", 1, "Tuesday", 2, "Wednesday", 3, "Thursday", 4,
                            "Friday", 5, "Saturday", 6, "Sunday", 7
                        );
                        return dayOrder.getOrDefault(day1, 8).compareTo(dayOrder.getOrDefault(day2, 8));
                    });
                    
                    // Display sorted days
                    for (String day : sortedDays) {
                        Label dayLabel = new Label("• " + day);
                        dayLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #333;");
                        scheduleBox.getChildren().add(dayLabel);
                    }
                    
                    // Add edit button
                    Button editButton = new Button("Edit Schedule");
                    editButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-padding: 5px 10px; -fx-cursor: hand; -fx-font-size: 10px;");
                    editButton.setOnAction(e -> {
                        // Close current popup
                        if (currentSchedulePopup != null) {
                            currentSchedulePopup.hide();
                            currentSchedulePopup = null;
                        }
                        // Open edit schedule dialog
                        openEditScheduleDialog(staff, schedule);
                    });
                    scheduleBox.getChildren().add(editButton);
                    
                    content.getChildren().add(scheduleBox);
                }
            }
            
            // Show popup positioned near the source button
            if (sourceButton != null && sourceButton.getScene() != null) {
                // Get button position relative to screen
                javafx.geometry.Point2D buttonPos = sourceButton.localToScreen(0, 0);
                double buttonWidth = sourceButton.getWidth();
                
                // Position popup to the right of the button, slightly below
                double popupX = buttonPos.getX() + buttonWidth + 5; // 5px gap
                double popupY = buttonPos.getY() - 20; // Slightly above the button
                
                popup.show(sourceButton, popupX, popupY);
            } else {
                // Fallback positioning
                popup.show(staffTable, staffTable.getScene().getWindow().getX() + staffTable.getScene().getWindow().getWidth() / 2, 
                          staffTable.getScene().getWindow().getY() + staffTable.getScene().getWindow().getHeight() / 2);
            }
            
            return popup;
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error showing staff schedule", e);
            showError("Error", "Failed to load schedule: " + e.getMessage());
            return null;
        }
    }

    private Popup showStaffSchedulePopup(Staff staff) {
        return showStaffSchedulePopup(staff, null, null);
    }

    private void showStaffSchedule(Staff staff) {
        showStaffSchedulePopup(staff);
    }

    private void openAddScheduleDialog(Staff staff) {
        try {
            // Create a simple dialog for adding schedule
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Add Schedule for " + staff.getName());
            dialogStage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            dialogStage.initOwner(staffTable.getScene().getWindow());
            
            VBox dialogContent = new VBox(15);
            dialogContent.setStyle("-fx-padding: 20px; -fx-background-color: white;");
            
            // Header
            Label headerLabel = new Label("Add New Schedule");
            headerLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #333;");
            
            // Time inputs
            HBox timeBox = new HBox(10);
            timeBox.setAlignment(Pos.CENTER_LEFT);
            
            Label startTimeLabel = new Label("Start Time:");
            Spinner<Integer> startHourSpinner = new Spinner<>(0, 23, 9);
            Spinner<Integer> startMinuteSpinner = new Spinner<>(0, 59, 0);
            Label startColon = new Label(":");
            
            Label endTimeLabel = new Label("End Time:");
            Spinner<Integer> endHourSpinner = new Spinner<>(0, 23, 17);
            Spinner<Integer> endMinuteSpinner = new Spinner<>(0, 59, 0);
            Label endColon = new Label(":");
            
            timeBox.getChildren().addAll(startTimeLabel, startHourSpinner, startColon, startMinuteSpinner, 
                                       new Label("  "), endTimeLabel, endHourSpinner, endColon, endMinuteSpinner);
            
            // Days selection
            VBox daysBox = new VBox(5);
            Label daysLabel = new Label("Working Days:");
            daysLabel.setStyle("-fx-font-weight: bold;");
            
            CheckBox mondayCheck = new CheckBox("Monday");
            CheckBox tuesdayCheck = new CheckBox("Tuesday");
            CheckBox wednesdayCheck = new CheckBox("Wednesday");
            CheckBox thursdayCheck = new CheckBox("Thursday");
            CheckBox fridayCheck = new CheckBox("Friday");
            CheckBox saturdayCheck = new CheckBox("Saturday");
            CheckBox sundayCheck = new CheckBox("Sunday");
            
            daysBox.getChildren().addAll(daysLabel, mondayCheck, tuesdayCheck, wednesdayCheck, 
                                       thursdayCheck, fridayCheck, saturdayCheck, sundayCheck);
            
            // Buttons
            HBox buttonBox = new HBox(10);
            buttonBox.setAlignment(Pos.CENTER_RIGHT);
            
            Button saveButton = new Button("Save");
            saveButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-padding: 8px 16px;");
            
            Button cancelButton = new Button("Cancel");
            cancelButton.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-padding: 8px 16px;");
            
            buttonBox.getChildren().addAll(cancelButton, saveButton);
            
            // Add all components
            dialogContent.getChildren().addAll(headerLabel, timeBox, daysBox, buttonBox);
            
            // Save button action
            saveButton.setOnAction(e -> {
                try {
                    // Validate that at least one day is selected
                    List<String> selectedDays = new ArrayList<>();
                    if (mondayCheck.isSelected()) selectedDays.add("Monday");
                    if (tuesdayCheck.isSelected()) selectedDays.add("Tuesday");
                    if (wednesdayCheck.isSelected()) selectedDays.add("Wednesday");
                    if (thursdayCheck.isSelected()) selectedDays.add("Thursday");
                    if (fridayCheck.isSelected()) selectedDays.add("Friday");
                    if (saturdayCheck.isSelected()) selectedDays.add("Saturday");
                    if (sundayCheck.isSelected()) selectedDays.add("Sunday");
                    
                    if (selectedDays.isEmpty()) {
                        showError("Validation Error", "Please select at least one working day.");
                        return;
                    }
                    
                    // Create schedule
                    Schedule schedule = new Schedule();
                    schedule.setStaff(staff);
                    schedule.setStart(LocalTime.of(startHourSpinner.getValue(), startMinuteSpinner.getValue()));
                    schedule.setEnd(LocalTime.of(endHourSpinner.getValue(), endMinuteSpinner.getValue()));
                    schedule.setDays(new HashSet<>(selectedDays));
                    
                    // Save schedule using the service method
                    scheduleService.createSchedule(staff, new HashSet<>(selectedDays), 
                                                 LocalTime.of(startHourSpinner.getValue(), startMinuteSpinner.getValue()),
                                                 LocalTime.of(endHourSpinner.getValue(), endMinuteSpinner.getValue()));
                    
                    dialogStage.close();
                    
                    // Refresh the schedule popup
                    showStaffSchedule(staff);
                    
                } catch (Exception ex) {
                    LOGGER.log(Level.SEVERE, "Error saving schedule", ex);
                    showError("Error", "Failed to save schedule: " + ex.getMessage());
                }
            });
            
            // Cancel button action
            cancelButton.setOnAction(e -> dialogStage.close());
            
            // Show dialog
            Scene scene = new Scene(dialogContent);
            dialogStage.setScene(scene);
            dialogStage.showAndWait();
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error opening add schedule dialog", e);
            showError("Error", "Failed to open schedule dialog: " + e.getMessage());
        }
    }

    private void openEditScheduleDialog(Staff staff, Schedule schedule) {
        try {
            // Create a dialog for editing schedule
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Edit Schedule for " + staff.getName());
            dialogStage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            dialogStage.initOwner(staffTable.getScene().getWindow());
            
            VBox dialogContent = new VBox(15);
            dialogContent.setStyle("-fx-padding: 20px; -fx-background-color: white;");
            
            // Header
            Label headerLabel = new Label("Edit Schedule");
            headerLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #333;");
            
            // Time inputs - pre-populated with current values
            HBox timeBox = new HBox(10);
            timeBox.setAlignment(Pos.CENTER_LEFT);
            
            Label startTimeLabel = new Label("Start Time:");
            Spinner<Integer> startHourSpinner = new Spinner<>(0, 23, schedule.getStart().getHour());
            Spinner<Integer> startMinuteSpinner = new Spinner<>(0, 59, schedule.getStart().getMinute());
            Label startColon = new Label(":");
            
            Label endTimeLabel = new Label("End Time:");
            Spinner<Integer> endHourSpinner = new Spinner<>(0, 23, schedule.getEnd().getHour());
            Spinner<Integer> endMinuteSpinner = new Spinner<>(0, 59, schedule.getEnd().getMinute());
            Label endColon = new Label(":");
            
            timeBox.getChildren().addAll(startTimeLabel, startHourSpinner, startColon, startMinuteSpinner, 
                                       new Label("  "), endTimeLabel, endHourSpinner, endColon, endMinuteSpinner);
            
            // Days selection - pre-populated with current values
            VBox daysBox = new VBox(5);
            Label daysLabel = new Label("Working Days:");
            daysLabel.setStyle("-fx-font-weight: bold;");
            
            CheckBox mondayCheck = new CheckBox("Monday");
            CheckBox tuesdayCheck = new CheckBox("Tuesday");
            CheckBox wednesdayCheck = new CheckBox("Wednesday");
            CheckBox thursdayCheck = new CheckBox("Thursday");
            CheckBox fridayCheck = new CheckBox("Friday");
            CheckBox saturdayCheck = new CheckBox("Saturday");
            CheckBox sundayCheck = new CheckBox("Sunday");
            
            // Set current selected days
            Set<String> currentDays = schedule.getDays();
            mondayCheck.setSelected(currentDays.contains("MONDAY"));
            tuesdayCheck.setSelected(currentDays.contains("TUESDAY"));
            wednesdayCheck.setSelected(currentDays.contains("WEDNESDAY"));
            thursdayCheck.setSelected(currentDays.contains("THURSDAY"));
            fridayCheck.setSelected(currentDays.contains("FRIDAY"));
            saturdayCheck.setSelected(currentDays.contains("SATURDAY"));
            sundayCheck.setSelected(currentDays.contains("SUNDAY"));
            
            daysBox.getChildren().addAll(daysLabel, mondayCheck, tuesdayCheck, wednesdayCheck, 
                                       thursdayCheck, fridayCheck, saturdayCheck, sundayCheck);
            
            // Buttons
            HBox buttonBox = new HBox(10);
            buttonBox.setAlignment(Pos.CENTER_RIGHT);
            
            Button saveButton = new Button("Save");
            saveButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-padding: 8px 16px;");
            
            Button deleteButton = new Button("Delete");
            deleteButton.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-padding: 8px 16px;");
            
            Button cancelButton = new Button("Cancel");
            cancelButton.setStyle("-fx-background-color: #9E9E9E; -fx-text-fill: white; -fx-padding: 8px 16px;");
            
            buttonBox.getChildren().addAll(deleteButton, cancelButton, saveButton);
            
            // Add all components
            dialogContent.getChildren().addAll(headerLabel, timeBox, daysBox, buttonBox);
            
            // Save button action
            saveButton.setOnAction(e -> {
                try {
                    // Validate that at least one day is selected
                    List<String> selectedDays = new ArrayList<>();
                    if (mondayCheck.isSelected()) selectedDays.add("Monday");
                    if (tuesdayCheck.isSelected()) selectedDays.add("Tuesday");
                    if (wednesdayCheck.isSelected()) selectedDays.add("Wednesday");
                    if (thursdayCheck.isSelected()) selectedDays.add("Thursday");
                    if (fridayCheck.isSelected()) selectedDays.add("Friday");
                    if (saturdayCheck.isSelected()) selectedDays.add("Saturday");
                    if (sundayCheck.isSelected()) selectedDays.add("Sunday");
                    
                    if (selectedDays.isEmpty()) {
                        showError("Validation Error", "Please select at least one working day.");
                        return;
                    }
                    
                    // Update schedule
                    schedule.setStart(LocalTime.of(startHourSpinner.getValue(), startMinuteSpinner.getValue()));
                    schedule.setEnd(LocalTime.of(endHourSpinner.getValue(), endMinuteSpinner.getValue()));
                    schedule.setDays(new HashSet<>(selectedDays));
                    
                    // Save updated schedule
                    scheduleService.updateSchedule(schedule.getId(), schedule);
                    
                    dialogStage.close();
                    
                    // Refresh the schedule popup
                    showStaffSchedule(staff);
                    
                } catch (Exception ex) {
                    LOGGER.log(Level.SEVERE, "Error updating schedule", ex);
                    showError("Error", "Failed to update schedule: " + ex.getMessage());
                }
            });
            
            // Delete button action
            deleteButton.setOnAction(e -> {
                try {
                    // Confirm deletion
                    Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
                    confirmDialog.setTitle("Confirm Deletion");
                    confirmDialog.setHeaderText("Delete Schedule");
                    confirmDialog.setContentText("Are you sure you want to delete this schedule?");
                    
                    Optional<ButtonType> result = confirmDialog.showAndWait();
                    if (result.isPresent() && result.get() == ButtonType.OK) {
                        scheduleService.deleteSchedule(schedule.getId());
                        dialogStage.close();
                        // Refresh the schedule popup
                        showStaffSchedule(staff);
                    }
                } catch (Exception ex) {
                    LOGGER.log(Level.SEVERE, "Error deleting schedule", ex);
                    showError("Error", "Failed to delete schedule: " + ex.getMessage());
                }
            });
            
            // Cancel button action
            cancelButton.setOnAction(e -> dialogStage.close());
            
            // Show dialog
            Scene scene = new Scene(dialogContent);
            dialogStage.setScene(scene);
            dialogStage.showAndWait();
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error opening edit schedule dialog", e);
            showError("Error", "Failed to open edit schedule dialog: " + e.getMessage());
        }
    }
} 