package org.example.controllers;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import org.example.models.Gun;
import org.example.models.GunAssignment;
import org.example.models.Staff;
import org.example.services.GunService;
import org.example.services.GunAssignmentService;
import org.example.services.StaffService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.HashMap;

@Component
@RestController
@RequestMapping("/api/weapons")
@CrossOrigin(origins = "*")
public class WeaponsController {

    private final GunService gunService;
    private final GunAssignmentService gunAssignmentService;
    private final StaffService staffService;

    @FXML private TableView<Gun> weaponsTable;
    @FXML private TableColumn<Gun, String> serialNumberCol;
    @FXML private TableColumn<Gun, String> typeCol;
    @FXML private TableColumn<Gun, String> nameCol;
    @FXML private TableColumn<Gun, String> assignedToCol;
    @FXML private TableColumn<Gun, String> returnedCol;
    @FXML private TableColumn<Gun, Void> actionsCol;

    @Autowired
    public WeaponsController(GunService gunService, GunAssignmentService gunAssignmentService, StaffService staffService) {
        this.gunService = gunService;
        this.gunAssignmentService = gunAssignmentService;
        this.staffService = staffService;
    }

    @FXML
    public void initialize() {
        setupTableColumns();
        loadWeapons();
    }

    private void setupTableColumns() {
        serialNumberCol.setCellValueFactory(new PropertyValueFactory<>("serialNumber"));
        typeCol.setCellValueFactory(new PropertyValueFactory<>("type"));
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));

        assignedToCol.setCellValueFactory(cellData -> {
            String serialNumber = cellData.getValue().getSerialNumber();
            List<GunAssignment> assignments = gunAssignmentService.getGunAssignmentsByGunSerialNumber(serialNumber);
            if (!assignments.isEmpty()) {
                return new ReadOnlyStringWrapper(assignments.get(0).getStaff().getName());
            }
            return new ReadOnlyStringWrapper("Unassigned");
        });

        returnedCol.setCellValueFactory(cellData -> {
            String serialNumber = cellData.getValue().getSerialNumber();
            List<GunAssignment> assignments = gunAssignmentService.getGunAssignmentsByGunSerialNumber(serialNumber);
            if (!assignments.isEmpty()) {
                return new ReadOnlyStringWrapper(assignments.get(0).isReturned() ? "Yes" : "No");
            }
            return new ReadOnlyStringWrapper("Yes");
        });
    }

    private void loadWeapons() {
        List<Gun> guns = gunService.getAllGuns();
        ObservableList<Gun> weapons = FXCollections.observableArrayList(guns);
        weaponsTable.setItems(weapons);
    }

    @FXML
    private void showAddWeaponForm() {
        System.out.println("Show Add Weapon Form");
    }

    @FXML
    private void assignGun() {
        Gun selectedGun = weaponsTable.getSelectionModel().getSelectedItem();
        if (selectedGun == null) {
            showError("Please select a gun to assign");
            return;
        }

        List<Staff> availableStaff = staffService.getAllStaff();
        if (availableStaff.isEmpty()) {
            showError("No staff members available for assignment");
            return;
        }

        Dialog<Staff> dialog = new Dialog<>();
        dialog.setTitle("Assign Gun");
        dialog.setHeaderText("Select staff member to assign gun: " + selectedGun.getSerialNumber());

        ComboBox<Staff> staffComboBox = new ComboBox<>(FXCollections.observableArrayList(availableStaff));
        staffComboBox.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Staff staff, boolean empty) {
                super.updateItem(staff, empty);
                setText(empty ? "" : staff.getName());
            }
        });

        dialog.getDialogPane().setContent(staffComboBox);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(buttonType -> {
            if (buttonType == ButtonType.OK) {
                return staffComboBox.getValue();
            }
            return null;
        });

        Optional<Staff> result = dialog.showAndWait();
        result.ifPresent(staff -> {
            try {
                gunAssignmentService.assignGunToStaff(selectedGun.getSerialNumber(), staff.getId());
                loadWeapons();
                showSuccess("Gun assigned successfully");
            } catch (Exception e) {
                showError("Error assigning gun: " + e.getMessage());
            }
        });
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
        System.out.println("Navigating back to Dashboard");
    }

    // REST Endpoints
    @GetMapping
    public ResponseEntity<List<Gun>> getAllGuns() {
        return ResponseEntity.ok(gunService.getAllGuns());
    }

    @GetMapping("/{serialNumber}")
    public ResponseEntity<Gun> getGunBySerialNumber(@PathVariable String serialNumber) {
        return ResponseEntity.ok(gunService.getGunBySerialNumber(serialNumber));
    }

    @GetMapping("/type/{type}")
    public ResponseEntity<List<Gun>> getGunsByType(@PathVariable String type) {
        return ResponseEntity.ok(gunService.getGunsByType(type));
    }

    @GetMapping("/staff/{staffId}")
    public ResponseEntity<List<Gun>> getGunsByStaffId(@PathVariable Integer staffId) {
        return ResponseEntity.ok(gunService.getGunsByAssignedStaffId(staffId));
    }

    @GetMapping("/staff/{staffId}/type/{type}")
    public ResponseEntity<List<Gun>> getGunsByStaffIdAndType(
            @PathVariable Integer staffId,
            @PathVariable String type) {
        return ResponseEntity.ok(gunService.getGunsByAssignedStaffIdAndType(staffId, type));
    }

    @PostMapping
    public ResponseEntity<Gun> createGun(@RequestBody GunRequest request) {
        Gun gun = new Gun(request.getSerialNumber(), request.getType(), request.getName());
        return ResponseEntity.ok(gunService.createGun(gun));
    }

    @PutMapping("/{serialNumber}")
    public ResponseEntity<Gun> updateGun(@PathVariable String serialNumber, @RequestBody GunRequest request) {
        Gun gun = new Gun(request.getSerialNumber(), request.getType(), request.getName());
        return ResponseEntity.ok(gunService.updateGun(serialNumber, gun));
    }

    @DeleteMapping("/{serialNumber}")
    public ResponseEntity<Void> deleteGun(@PathVariable String serialNumber) {
        gunService.deleteGun(serialNumber);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{serialNumber}/assign/{staffId}")
    public ResponseEntity<GunAssignment> assignGunToStaff(@PathVariable String serialNumber, @PathVariable Integer staffId) {
        return ResponseEntity.ok(gunAssignmentService.assignGunToStaff(serialNumber, staffId));
    }

    @PostMapping("/{serialNumber}/remove/{staffId}")
    public ResponseEntity<Void> removeGunFromStaff(@PathVariable String serialNumber, @PathVariable Integer staffId) {
        gunAssignmentService.removeGunAssignment(serialNumber, staffId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/staff/{staffId}/count")
    public ResponseEntity<Long> getGunsAssignedToStaff(@PathVariable Integer staffId) {
        return ResponseEntity.ok(gunAssignmentService.getGunAssignmentCountByStaffId(staffId));
    }

    @GetMapping("/maintenance/needed")
    public ResponseEntity<List<Gun>> getGunsNeedingMaintenance() {
        return ResponseEntity.ok(gunService.getGunsByAssignedStaffId(null));
    }

    @GetMapping("/stats/type")
    public ResponseEntity<Map<String, Long>> getGunStatisticsByType() {
        Map<String, Long> stats = new HashMap<>();
        stats.put("total", gunService.getGunCountByType(""));
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/maintenance/count")
    public ResponseEntity<Long> getGunsNeedingMaintenanceCount() {
        return ResponseEntity.ok(gunService.getGunCountByAssignedStaffId(null));
    }

    // DTO classes
    private static class GunRequest {
        private String serialNumber;
        private String type;
        private String name;

        public String getSerialNumber() {
            return serialNumber;
        }

        public void setSerialNumber(String serialNumber) {
            this.serialNumber = serialNumber;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}
