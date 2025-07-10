package org.example.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import org.example.models.Room;
import org.example.models.Block;
import org.example.models.Equipment;
import org.example.models.Department;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;
import org.example.services.RoomService;
import org.example.services.BlockService;
import org.example.services.DepartmentService;

import java.util.List;

@Component
@RestController
@RequestMapping("/api/rooms")
@CrossOrigin(origins = "*")
public class RoomController {

    private final RoomService roomService;
    private final BlockService blockService;
    private final DepartmentService departmentService;

    @FXML private Label blockNameLabel;
    @FXML private TableView<Room> roomsTable;
    @FXML private TableColumn<Room, Integer> roomIdCol;
    @FXML private TableColumn<Room, String> roomTypeCol;
    @FXML private TableColumn<Room, String> roomDescriptionCol;
    @FXML private TableColumn<Room, Void> roomActionsCol;

    @Autowired
    public RoomController(RoomService roomService, BlockService blockService, DepartmentService departmentService) {
        this.roomService = roomService;
        this.blockService = blockService;
        this.departmentService = departmentService;
    }

    @FXML
    public void initialize() {
        blockNameLabel.setText("Block A");
        setupTableColumns();
        loadRooms();
    }

    private void setupTableColumns() {
        roomIdCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        roomTypeCol.setCellValueFactory(new PropertyValueFactory<>("type"));
        roomDescriptionCol.setCellValueFactory(new PropertyValueFactory<>("description"));
    }

    private void loadRooms() {
        List<Room> rooms = roomService.getAllRooms();
        ObservableList<Room> observableRooms = FXCollections.observableArrayList(rooms);
        roomsTable.setItems(observableRooms);
    }

    @FXML
    private void showAddRoomForm() {
        System.out.println("Show Add Room Form");
    }

    @FXML
    private void viewEquipment() {
        System.out.println("Viewing Equipment");
    }

    @FXML
    private void goBack() {
        System.out.println("Navigating back to Block Page");
    }

    // REST Endpoints
    @GetMapping
    public ResponseEntity<List<Room>> getAllRooms() {
        return ResponseEntity.ok(roomService.getAllRooms());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Room> getRoomById(@PathVariable Integer id) {
        return ResponseEntity.ok(roomService.getRoomById(id));
    }

    @GetMapping("/block/{blockId}")
    public ResponseEntity<List<Room>> getRoomsByBlock(@PathVariable Integer blockId) {
        return ResponseEntity.ok(roomService.getRoomsByBlockId(blockId));
    }

    @PostMapping
    public ResponseEntity<Room> createRoom(@RequestBody RoomRequest request) {
        Room room = new Room(request.getType(), request.getDescription());
        room = roomService.createRoom(room);
        
        if (request.getBlockId() != null && request.getDepartmentId() != null) {
            Block block = blockService.getBlockById(request.getBlockId())
                .orElseThrow(() -> new RuntimeException("Block not found with ID: " + request.getBlockId()));
            Department department = departmentService.getDepartmentById(request.getDepartmentId());
            room = roomService.assignToBlock(room.getId(), block, department);
        }
        
        return ResponseEntity.ok(room);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Room> updateRoom(@PathVariable Integer id, @RequestBody RoomRequest request) {
        Room room = new Room(request.getType(), request.getDescription());
        room = roomService.updateRoom(id, room);
        
        if (request.getBlockId() != null && request.getDepartmentId() != null) {
            Block block = blockService.getBlockById(request.getBlockId())
                .orElseThrow(() -> new RuntimeException("Block not found with ID: " + request.getBlockId()));
            Department department = departmentService.getDepartmentById(request.getDepartmentId());
            room = roomService.assignToBlock(room.getId(), block, department);
        }
        
        return ResponseEntity.ok(room);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRoom(@PathVariable Integer id) {
        roomService.deleteRoom(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/equipment")
    public ResponseEntity<Room> addEquipment(@PathVariable Integer id, @RequestBody Equipment equipment) {
        return ResponseEntity.ok(roomService.addEquipmentToRoom(id, equipment));
    }

    @DeleteMapping("/{id}/equipment/{equipmentId}")
    public ResponseEntity<Room> removeEquipment(@PathVariable Integer id, @PathVariable Integer equipmentId) {
        Equipment equipment = new Equipment("Equipment " + equipmentId, 1, "Description", null);
        return ResponseEntity.ok(roomService.removeEquipmentFromRoom(id, equipment));
    }

    @GetMapping("/{id}/stats")
    public ResponseEntity<RoomStats> getRoomStats(@PathVariable Integer id) {
        Room room = roomService.getRoomById(id);
        String blockName = room.getBlocks().isEmpty() ? "No Block" : 
            room.getBlocks().iterator().next().getType();
        return ResponseEntity.ok(new RoomStats(
            room.getEquipmentCount(),
            room.hasEquipment(),
            blockName
        ));
    }

    // DTO classes
    private static class RoomRequest {
        private String type;
        private String description;
        private Integer blockId;
        private Integer departmentId;

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public Integer getBlockId() {
            return blockId;
        }

        public void setBlockId(Integer blockId) {
            this.blockId = blockId;
        }

        public Integer getDepartmentId() {
            return departmentId;
        }

        public void setDepartmentId(Integer departmentId) {
            this.departmentId = departmentId;
        }
    }

    private static class RoomStats {
        private final int equipmentCount;
        private final boolean hasEquipment;
        private final String blockName;

        public RoomStats(int equipmentCount, boolean hasEquipment, String blockName) {
            this.equipmentCount = equipmentCount;
            this.hasEquipment = hasEquipment;
            this.blockName = blockName;
        }

        public int getEquipmentCount() {
            return equipmentCount;
        }

        public boolean isHasEquipment() {
            return hasEquipment;
        }

        public String getBlockName() {
            return blockName;
        }
    }
}
