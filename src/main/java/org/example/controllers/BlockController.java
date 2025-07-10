package org.example.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import org.example.models.Block;
import org.example.models.Cell;
import org.example.models.Room;
import org.example.models.Prison;
import org.example.services.BlockService;
import org.example.services.CellService;
import org.example.services.RoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Optional;

@Component
@RestController
@RequestMapping("/api/blocks")
@CrossOrigin(origins = "*")
public class BlockController {

    private final BlockService blockService;
    private final CellService cellService;
    private final RoomService roomService;

    // FXML UI Elements
    @FXML private Label blockNameLabel;
    @FXML private Label totalRoomsLabel;
    @FXML private Label totalCellsLabel;
    @FXML private Label totalPrisonersLabel;

    @FXML private TableView<Cell> cellsTable;
    @FXML private TableColumn<Cell, String> typeCol;
    @FXML private TableColumn<Cell, Integer> capacityCol;
    @FXML private TableColumn<Cell, Integer> nPrisonersCol;

    @FXML private TableView<Room> roomsTable;
    @FXML private TableColumn<Room, String> roomDescriptionCol;

    private Block currentBlock;

    @Autowired
    public BlockController(BlockService blockService, CellService cellService, RoomService roomService) {
        this.blockService = blockService;
        this.cellService = cellService;
        this.roomService = roomService;
    }

    // JavaFX UI Methods
    @FXML
    public void initialize() {
        currentBlock = blockService.getBlockById(1).orElse(null);
        if (currentBlock != null) {
            blockNameLabel.setText(currentBlock.getType());
            totalRoomsLabel.setText(String.valueOf(currentBlock.getRooms().size()));
            totalCellsLabel.setText(String.valueOf(currentBlock.getNumberOfCells()));
            totalPrisonersLabel.setText(String.valueOf(currentBlock.getNumberOfPrisoners()));

            typeCol.setCellValueFactory(new PropertyValueFactory<>("type"));
            capacityCol.setCellValueFactory(new PropertyValueFactory<>("capacity"));
            nPrisonersCol.setCellValueFactory(new PropertyValueFactory<>("nPrisoners"));

            roomDescriptionCol.setCellValueFactory(new PropertyValueFactory<>("description"));

            loadCells();
            loadRooms();
        }
    }

    private void loadCells() {
        if (currentBlock != null) {
            ObservableList<Cell> cells = FXCollections.observableArrayList(
                    cellService.getCellsByBlockId(currentBlock.getBlockId())
            );
            cellsTable.setItems(cells);
        }
    }

    private void loadRooms() {
        if (currentBlock != null) {
            ObservableList<Room> rooms = FXCollections.observableArrayList(
                    roomService.getRoomsByBlockId(currentBlock.getBlockId())
            );
            roomsTable.setItems(rooms);
        }
    }

    @FXML
    private void showAddRoomForm() {
        // TODO: Implement add room dialog
    }

    @FXML
    private void showAddCellForm() {
        // TODO: Implement add cell dialog
    }

    @FXML
    private void goBack() {
        // TODO: Implement navigation back to blocks page
    }

    // REST Endpoints
    @GetMapping
    public ResponseEntity<List<Block>> getAllBlocks() {
        return ResponseEntity.ok(blockService.getAllBlocks());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Block> getBlockById(@PathVariable Integer id) {
        Optional<Block> block = blockService.getBlockById(id);
        return block.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/prison/{prisonId}")
    public ResponseEntity<List<Block>> getBlocksByPrisonId(@PathVariable Integer prisonId) {
        return ResponseEntity.ok(blockService.getBlocksByPrisonId(prisonId));
    }

    @GetMapping("/type/{type}")
    public ResponseEntity<List<Block>> getBlocksByType(@PathVariable String type) {
        return ResponseEntity.ok(blockService.getBlocksByType(type));
    }

    @PostMapping
    public ResponseEntity<Block> createBlock(@RequestBody BlockRequest request) {
        Block block = new Block(request.getType(), request.getPrison());
        return ResponseEntity.ok(blockService.createBlock(block));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Block> updateBlock(@PathVariable Integer id, @RequestBody BlockRequest request) {
        Block block = new Block(request.getType(), request.getPrison());
        return ResponseEntity.ok(blockService.updateBlock(id, block));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBlock(@PathVariable Integer id) {
        blockService.deleteBlock(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}/cells")
    public ResponseEntity<List<Cell>> getBlockCells(@PathVariable Integer id) {
        return ResponseEntity.ok(cellService.getCellsByBlockId(id));
    }

    @GetMapping("/{id}/rooms")
    public ResponseEntity<List<Room>> getBlockRooms(@PathVariable Integer id) {
        return ResponseEntity.ok(roomService.getRoomsByBlockId(id));
    }

    @GetMapping("/{id}/stats")
    public ResponseEntity<BlockStats> getBlockStats(@PathVariable Integer id) {
        Optional<Block> block = blockService.getBlockById(id);
        if (block.isPresent()) {
            Block b = block.get();
            BlockStats stats = new BlockStats(
                b.getRooms().size(),
                b.getNumberOfCells(),
                b.getNumberOfPrisoners()
            );
            return ResponseEntity.ok(stats);
        }
        return ResponseEntity.notFound().build();
    }

    // DTO classes
    private static class BlockRequest {
        private String type;
        private Prison prison;

        public String getType() {
            return type;
        }

        public Prison getPrison() {
            return prison;
        }
    }

    private static class BlockStats {
        @JsonProperty("totalRooms")
        private final int totalRooms;
        @JsonProperty("totalCells")
        private final int totalCells;
        @JsonProperty("totalPrisoners")
        private final int totalPrisoners;

        public BlockStats(int totalRooms, int totalCells, int totalPrisoners) {
            this.totalRooms = totalRooms;
            this.totalCells = totalCells;
            this.totalPrisoners = totalPrisoners;
        }
    }
}