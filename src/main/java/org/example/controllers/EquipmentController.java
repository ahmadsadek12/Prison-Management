package org.example.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import org.example.models.Equipment;
import org.example.models.Room;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;
import org.example.services.EquipmentService;
import org.example.services.RoomService;

import java.util.List;

@Component
@RestController
@RequestMapping("/api/equipment")
@CrossOrigin(origins = "*")
public class EquipmentController {

    private final EquipmentService equipmentService;
    private final RoomService roomService;

    @FXML private Label roomNameLabel;
    @FXML private TableView<Equipment> equipmentTable;
    @FXML private TableColumn<Equipment, String> equipmentNameCol;
    @FXML private TableColumn<Equipment, Integer> quantityCol;
    @FXML private TableColumn<Equipment, String> descriptionCol;
    @FXML private TableColumn<Equipment, Void> equipmentActionsCol;

    private Room currentRoom;

    @Autowired
    public EquipmentController(EquipmentService equipmentService, RoomService roomService) {
        this.equipmentService = equipmentService;
        this.roomService = roomService;
    }

    @FXML
    public void initialize() {
        // Fetch the room by ID dynamically (change the ID as necessary)
        try {
            currentRoom = roomService.getRoomById(1);
            roomNameLabel.setText(currentRoom.getDescription());
            setupTableColumns();
            loadEquipment();
        } catch (RuntimeException e) {
            System.out.println("Room not found: " + e.getMessage());
        }
    }

    private void setupTableColumns() {
        equipmentNameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        quantityCol.setCellValueFactory(new PropertyValueFactory<>("amount"));
        descriptionCol.setCellValueFactory(new PropertyValueFactory<>("description"));
    }

    private void loadEquipment() {
        if (currentRoom != null) {
            ObservableList<Equipment> equipmentList = FXCollections.observableArrayList(
                    equipmentService.getEquipmentByRoomId(currentRoom.getId())
            );
            equipmentTable.setItems(equipmentList);
        }
    }

    @FXML
    private void showAddEquipmentForm() {
        System.out.println("Show Add Equipment Form");
    }

    @FXML
    private void viewMaintenanceLog() {
        System.out.println("Viewing Maintenance Log");
    }

    @FXML
    private void goBack() {
        System.out.println("Navigating back to Room Page");
    }

    // REST Endpoints
    @GetMapping
    public ResponseEntity<List<Equipment>> getAllEquipment() {
        return ResponseEntity.ok(equipmentService.getAllEquipment());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Equipment> getEquipmentById(@PathVariable Integer id) {
        return ResponseEntity.ok(equipmentService.getEquipmentById(id));
    }

    @GetMapping("/room/{roomId}")
    public ResponseEntity<List<Equipment>> getEquipmentByRoom(@PathVariable Integer roomId) {
        return ResponseEntity.ok(equipmentService.getEquipmentByRoomId(roomId));
    }


    @PostMapping
    public ResponseEntity<Equipment> createEquipment(@RequestBody EquipmentRequest request) {
        Room room = roomService.getRoomById(request.getRoomId());
        Equipment equipment = new Equipment(request.getName(), request.getAmount(), request.getDescription(), room);
        return ResponseEntity.ok(equipmentService.createEquipment(equipment));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Equipment> updateEquipment(@PathVariable Integer id, @RequestBody EquipmentRequest request) {
        Room room = roomService.getRoomById(request.getRoomId());
        Equipment equipment = new Equipment(request.getName(), request.getAmount(), request.getDescription(), room);
        return ResponseEntity.ok(equipmentService.updateEquipment(id, equipment));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEquipment(@PathVariable Integer id) {
        equipmentService.deleteEquipment(id);
        return ResponseEntity.ok().build();
    }

    // DTO classes
    private static class EquipmentRequest {
        private String name;
        private Integer amount;
        private String description;
        private Integer roomId;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Integer getAmount() {
            return amount;
        }

        public void setAmount(Integer amount) {
            this.amount = amount;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public Integer getRoomId() {
            return roomId;
        }

        public void setRoomId(Integer roomId) {
            this.roomId = roomId;
        }
    }
}
