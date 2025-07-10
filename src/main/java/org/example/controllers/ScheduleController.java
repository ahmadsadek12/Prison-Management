package org.example.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import org.example.models.Schedule;
import org.example.models.Staff;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;
import org.example.services.ScheduleService;
import org.example.services.StaffService;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
@RestController
@RequestMapping("/api/schedules")
@CrossOrigin(origins = "*")
public class ScheduleController {

    private final ScheduleService scheduleService;
    private final StaffService staffService;

    @FXML private TableView<Schedule> scheduleTable;
    @FXML private TableColumn<Schedule, Integer> idColumn;
    @FXML private TableColumn<Schedule, Staff> staffColumn;
    @FXML private TableColumn<Schedule, Set<String>> daysColumn;
    @FXML private TableColumn<Schedule, LocalTime> startTimeColumn;
    @FXML private TableColumn<Schedule, LocalTime> endTimeColumn;
    
    @FXML private ComboBox<Staff> staffComboBox;
    @FXML private TextField startTimeField;
    @FXML private TextField endTimeField;
    @FXML private CheckBox mondayCheck;
    @FXML private CheckBox tuesdayCheck;
    @FXML private CheckBox wednesdayCheck;
    @FXML private CheckBox thursdayCheck;
    @FXML private CheckBox fridayCheck;
    @FXML private CheckBox saturdayCheck;
    @FXML private CheckBox sundayCheck;

    @Autowired
    public ScheduleController(ScheduleService scheduleService, StaffService staffService) {
        this.scheduleService = scheduleService;
        this.staffService = staffService;
    }

    @FXML
    public void initialize() {
        setupTableColumns();
        loadSchedules();
        loadStaffComboBox();
    }

    private void setupTableColumns() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        staffColumn.setCellValueFactory(new PropertyValueFactory<>("staff"));
        daysColumn.setCellValueFactory(new PropertyValueFactory<>("days"));
        startTimeColumn.setCellValueFactory(new PropertyValueFactory<>("start"));
        endTimeColumn.setCellValueFactory(new PropertyValueFactory<>("end"));

        // Custom cell factory for staff column to show staff name
        staffColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Staff staff, boolean empty) {
                super.updateItem(staff, empty);
                if (empty || staff == null) {
                    setText(null);
                } else {
                    setText(staff.getName());
                }
            }
        });

        // Custom cell factory for days column to show days as comma-separated list
        daysColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Set<String> days, boolean empty) {
                super.updateItem(days, empty);
                if (empty || days == null) {
                    setText(null);
                } else {
                    setText(String.join(", ", days));
                }
            }
        });

        // Custom cell factory for time columns to format time
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
        startTimeColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(LocalTime time, boolean empty) {
                super.updateItem(time, empty);
                if (empty || time == null) {
                    setText(null);
                } else {
                    setText(time.format(timeFormatter));
                }
            }
        });

        endTimeColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(LocalTime time, boolean empty) {
                super.updateItem(time, empty);
                if (empty || time == null) {
                    setText(null);
                } else {
                    setText(time.format(timeFormatter));
                }
            }
        });
    }

    private void loadSchedules() {
        List<Schedule> schedules = scheduleService.getAllSchedules();
        ObservableList<Schedule> observableSchedules = FXCollections.observableArrayList(schedules);
        scheduleTable.setItems(observableSchedules);
    }

    private void loadStaffComboBox() {
        List<Staff> staffList = staffService.getAllStaff();
        ObservableList<Staff> observableStaff = FXCollections.observableArrayList(staffList);
        staffComboBox.setItems(observableStaff);
        
        // Custom cell factory to show staff name in combo box
        staffComboBox.setCellFactory(comboBox -> new ListCell<>() {
            @Override
            protected void updateItem(Staff staff, boolean empty) {
                super.updateItem(staff, empty);
                if (empty || staff == null) {
                    setText(null);
                } else {
                    setText(staff.getName());
                }
            }
        });
    }

    @FXML
    private void handleAddSchedule() {
        try {
            Staff selectedStaff = staffComboBox.getValue();
            if (selectedStaff == null) {
                showError("Please select a staff member");
                return;
            }

            Set<String> selectedDays = getSelectedDays();
            if (selectedDays.isEmpty()) {
                showError("Please select at least one day");
                return;
            }

            LocalTime startTime = parseTime(startTimeField.getText());
            LocalTime endTime = parseTime(endTimeField.getText());
            
            if (startTime == null || endTime == null) {
                showError("Please enter valid times in HH:mm format");
                return;
            }

            if (endTime.isBefore(startTime)) {
                showError("End time cannot be before start time");
                return;
            }

            scheduleService.createSchedule(selectedStaff, selectedDays, startTime, endTime);
            loadSchedules();
            clearForm();
            showSuccess("Schedule created successfully");
        } catch (Exception e) {
            showError("Error creating schedule: " + e.getMessage());
        }
    }

    private Set<String> getSelectedDays() {
        Set<String> days = new HashSet<>();
        if (mondayCheck.isSelected()) days.add("MONDAY");
        if (tuesdayCheck.isSelected()) days.add("TUESDAY");
        if (wednesdayCheck.isSelected()) days.add("WEDNESDAY");
        if (thursdayCheck.isSelected()) days.add("THURSDAY");
        if (fridayCheck.isSelected()) days.add("FRIDAY");
        if (saturdayCheck.isSelected()) days.add("SATURDAY");
        if (sundayCheck.isSelected()) days.add("SUNDAY");
        return days;
    }

    private LocalTime parseTime(String timeStr) {
        try {
            return LocalTime.parse(timeStr, DateTimeFormatter.ofPattern("HH:mm"));
        } catch (Exception e) {
            return null;
        }
    }

    private void clearForm() {
        staffComboBox.setValue(null);
        startTimeField.clear();
        endTimeField.clear();
        mondayCheck.setSelected(false);
        tuesdayCheck.setSelected(false);
        wednesdayCheck.setSelected(false);
        thursdayCheck.setSelected(false);
        fridayCheck.setSelected(false);
        saturdayCheck.setSelected(false);
        sundayCheck.setSelected(false);
    }

    @FXML
    private void handleDeleteSchedule() {
        Schedule selectedSchedule = scheduleTable.getSelectionModel().getSelectedItem();
        if (selectedSchedule == null) {
            showError("Please select a schedule to delete");
            return;
        }

        try {
            scheduleService.deleteSchedule(selectedSchedule.getId());
            loadSchedules();
            showSuccess("Schedule deleted successfully");
        } catch (Exception e) {
            showError("Error deleting schedule: " + e.getMessage());
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showSuccess(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void goBack() {
        System.out.println("Navigating back");
    }

    // REST Endpoints
    @GetMapping
    public ResponseEntity<List<Schedule>> getAllSchedules() {
        return ResponseEntity.ok(scheduleService.getAllSchedules());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Schedule> getScheduleById(@PathVariable Integer id) {
        return ResponseEntity.ok(scheduleService.getScheduleById(id));
    }

    @GetMapping("/staff/{staffId}")
    public ResponseEntity<List<Schedule>> getSchedulesByStaff(@PathVariable Integer staffId) {
        return ResponseEntity.ok(scheduleService.getSchedulesByStaffId(staffId));
    }

    @GetMapping("/current")
    public ResponseEntity<List<Schedule>> getCurrentSchedules(@RequestParam LocalTime currentTime) {
        return ResponseEntity.ok(scheduleService.getCurrentSchedules(currentTime));
    }

    @GetMapping("/staff/{staffId}/current")
    public ResponseEntity<List<Schedule>> getCurrentSchedulesByStaff(
            @PathVariable Integer staffId,
            @RequestParam LocalTime currentTime) {
        return ResponseEntity.ok(scheduleService.getCurrentSchedulesByStaff(staffId, currentTime));
    }

    @GetMapping("/time-range")
    public ResponseEntity<List<Schedule>> getSchedulesByTimeRange(
            @RequestParam LocalTime startTime,
            @RequestParam LocalTime endTime) {
        return ResponseEntity.ok(scheduleService.getSchedulesByTimeRange(startTime, endTime));
    }

    @GetMapping("/staff/{staffId}/time-range")
    public ResponseEntity<List<Schedule>> getSchedulesByStaffAndTimeRange(
            @PathVariable Integer staffId,
            @RequestParam LocalTime startTime,
            @RequestParam LocalTime endTime) {
        return ResponseEntity.ok(scheduleService.getSchedulesByStaffAndTimeRange(staffId, startTime, endTime));
    }

    @PostMapping
    public ResponseEntity<Schedule> createSchedule(@RequestBody ScheduleRequest request) {
        Staff staff = staffService.getStaffById(request.getStaffId());
        return ResponseEntity.ok(scheduleService.createSchedule(
            staff,
            request.getDays(),
            request.getStartTime(),
            request.getEndTime()
        ));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Schedule> updateSchedule(@PathVariable Integer id, @RequestBody ScheduleRequest request) {
        Staff staff = staffService.getStaffById(request.getStaffId());
        Schedule schedule = new Schedule(staff, request.getDays(), request.getStartTime(), request.getEndTime());
        return ResponseEntity.ok(scheduleService.updateSchedule(id, schedule));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSchedule(@PathVariable Integer id) {
        scheduleService.deleteSchedule(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}/working-hours")
    public ResponseEntity<Boolean> isWorkingHours(@PathVariable Integer id, @RequestParam LocalTime time) {
        return ResponseEntity.ok(scheduleService.isWorkingHours(id, time));
    }

    @GetMapping("/{id}/working-day")
    public ResponseEntity<Boolean> isWorkingDay(@PathVariable Integer id, @RequestParam String day) {
        return ResponseEntity.ok(scheduleService.isWorkingDay(id, day));
    }

    @GetMapping("/{id}/stats")
    public ResponseEntity<ScheduleStats> getScheduleStats(@PathVariable Integer id) {
        Schedule schedule = scheduleService.getScheduleById(id);
        return ResponseEntity.ok(new ScheduleStats(
            schedule.isFullTime(),
            schedule.isPartTime(),
            schedule.isWeekend(),
            schedule.isWeekday(),
            schedule.getDays().size()
        ));
    }

    // DTO classes
    private static class ScheduleRequest {
        private Integer staffId;
        private Set<String> days;
        private LocalTime startTime;
        private LocalTime endTime;

        public Integer getStaffId() {
            return staffId;
        }

        public void setStaffId(Integer staffId) {
            this.staffId = staffId;
        }

        public Set<String> getDays() {
            return days;
        }

        public void setDays(Set<String> days) {
            this.days = days;
        }

        public LocalTime getStartTime() {
            return startTime;
        }

        public void setStartTime(LocalTime startTime) {
            this.startTime = startTime;
        }

        public LocalTime getEndTime() {
            return endTime;
        }

        public void setEndTime(LocalTime endTime) {
            this.endTime = endTime;
        }
    }

    private static class ScheduleStats {
        private final boolean fullTime;
        private final boolean partTime;
        private final boolean weekend;
        private final boolean weekday;
        private final int totalDays;

        public ScheduleStats(boolean fullTime, boolean partTime, boolean weekend, boolean weekday, int totalDays) {
            this.fullTime = fullTime;
            this.partTime = partTime;
            this.weekend = weekend;
            this.weekday = weekday;
            this.totalDays = totalDays;
        }

        public boolean isFullTime() {
            return fullTime;
        }

        public boolean isPartTime() {
            return partTime;
        }

        public boolean isWeekend() {
            return weekend;
        }

        public boolean isWeekday() {
            return weekday;
        }

        public int getTotalDays() {
            return totalDays;
        }
    }
}