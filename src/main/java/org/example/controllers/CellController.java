package org.example.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import org.example.models.Cell;
import org.example.models.Block;
import org.example.services.CellService;
import org.example.services.BlockService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Optional;

@Component
@RestController
@RequestMapping("/api/cells")
@CrossOrigin(origins = "*")
public class CellController {

    private final CellService cellService;
    private final BlockService blockService;

    @FXML private Label cellIdLabel;
    @FXML private Label cellTypeLabel;
    @FXML private Label capacityLabel;
    @FXML private Label occupiedLabel;
    @FXML private Label availabilityLabel;

    @FXML private TableView<Cell> cellsTable;
    @FXML private TableColumn<Cell, Integer> cellIdCol;
    @FXML private TableColumn<Cell, String> cellTypeCol;
    @FXML private TableColumn<Cell, Integer> capacityCol;
    @FXML private TableColumn<Cell, Integer> occupiedCol;
    @FXML private TableColumn<Cell, Boolean> availabilityCol;

    private Block currentBlock;

    @Autowired
    public CellController(CellService cellService, BlockService blockService) {
        this.cellService = cellService;
        this.blockService = blockService;
    }

    @FXML
    public void initialize() {
        // Load block data by ID (adjust ID as needed)
        currentBlock = blockService.getBlockById(1).orElse(null);

        if (currentBlock != null) {
            setupTable();
            loadCells();
        }
    }

    private void setupTable() {
        cellIdCol.setCellValueFactory(new PropertyValueFactory<>("cellId"));
        cellTypeCol.setCellValueFactory(new PropertyValueFactory<>("type"));
        capacityCol.setCellValueFactory(new PropertyValueFactory<>("capacity"));
        occupiedCol.setCellValueFactory(new PropertyValueFactory<>("nPrisoners"));
        availabilityCol.setCellValueFactory(new PropertyValueFactory<>("availability"));
    }

    private void loadCells() {
        if (currentBlock != null) {
            ObservableList<Cell> cells = FXCollections.observableArrayList(
                    cellService.getCellsByBlockId(currentBlock.getBlockId())
            );
            cellsTable.setItems(cells);
        }
    }

    @FXML
    private void showAddPrisonerForm() {
        System.out.println("Show Add Prisoner Form");
        // Implement UI logic for adding a prisoner
    }

    @FXML
    private void goBack() {
        System.out.println("Go Back to Block");
        // Implement UI logic for going back
    }

    // REST Endpoints
    @GetMapping
    public ResponseEntity<List<Cell>> getAllCells() {
        return ResponseEntity.ok(cellService.getAllCells());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Cell> getCellById(@PathVariable Integer id) {
        Optional<Cell> cell = cellService.getCellById(id);
        return cell.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/block/{blockId}")
    public ResponseEntity<List<Cell>> getCellsByBlockId(@PathVariable Integer blockId) {
        return ResponseEntity.ok(cellService.getCellsByBlockId(blockId));
    }

    @GetMapping("/type/{type}")
    public ResponseEntity<List<Cell>> getCellsByType(@PathVariable String type) {
        return ResponseEntity.ok(cellService.getCellsByType(type));
    }

    @GetMapping("/available")
    public ResponseEntity<List<Cell>> getAvailableCells() {
        return ResponseEntity.ok(cellService.getAvailableCells());
    }

    @PostMapping
    public ResponseEntity<Cell> createCell(@RequestBody CellRequest request) {
        Cell cell = new Cell(
            request.getType(),
            request.getCapacity(),
            request.getBlock()
        );
        return ResponseEntity.ok(cellService.createCell(cell));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Cell> updateCell(@PathVariable Integer id, @RequestBody CellRequest request) {
        Cell cell = new Cell(
            request.getType(),
            request.getCapacity(),
            request.getBlock()
        );
        return ResponseEntity.ok(cellService.updateCell(id, cell));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCell(@PathVariable Integer id) {
        cellService.deleteCell(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}/stats")
    public ResponseEntity<CellStats> getCellStats(@PathVariable Integer id) {
        Optional<Cell> cell = cellService.getCellById(id);
        if (cell.isPresent()) {
            Cell c = cell.get();
            CellStats stats = new CellStats(
                c.getCapacity(),
                c.getNPrisoners(),
                !c.isAtCapacity()
            );
            return ResponseEntity.ok(stats);
        }
        return ResponseEntity.notFound().build();
    }

    // DTO classes
    private static class CellRequest {
        private String type;
        private int capacity;
        private Block block;

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public int getCapacity() {
            return capacity;
        }

        public void setCapacity(int capacity) {
            this.capacity = capacity;
        }

        public Block getBlock() {
            return block;
        }

        public void setBlock(Block block) {
            this.block = block;
        }
    }

    private static class CellStats {
        @JsonProperty("capacity")
        private final int capacity;
        @JsonProperty("occupied")
        private final int occupied;
        @JsonProperty("available")
        private final boolean available;

        public CellStats(int capacity, int occupied, boolean available) {
            this.capacity = capacity;
            this.occupied = occupied;
            this.available = available;
        }

        public int getCapacity() {
            return capacity;
        }

        public int getOccupied() {
            return occupied;
        }

        public boolean isAvailable() {
            return available;
        }
    }
}
