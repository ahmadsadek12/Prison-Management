package org.example.services;

import org.example.models.Cell;
import org.example.models.Prisoner;
import org.example.models.Block;
import org.example.repositories.mysql.CellRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class CellService {

    private final CellRepository cellRepository;

    public CellService(CellRepository cellRepository) {
        this.cellRepository = cellRepository;
    }

    @Transactional(readOnly = true)
    public List<Cell> getAllCells() {
        return cellRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Cell> getCellById(Integer id) {
        Assert.notNull(id, "Cell ID cannot be null");
        return cellRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<Cell> getCellsByType(String type) {
        Assert.hasText(type, "Cell type cannot be null or empty");
        return cellRepository.findByType(type);
    }

    @Transactional(readOnly = true)
    public List<Cell> getCellsByBlockId(Integer blockId) {
        Assert.notNull(blockId, "Block ID cannot be null");
        return cellRepository.findByBlockId(blockId);
    }

    @Transactional(readOnly = true)
    public List<Cell> getCellsByBlockIdAndType(Integer blockId, String type) {
        Assert.notNull(blockId, "Block ID cannot be null");
        Assert.hasText(type, "Cell type cannot be null or empty");
        return cellRepository.findByBlockIdAndType(blockId, type);
    }

    @Transactional(readOnly = true)
    public List<Cell> getCellsByPrisonerId(Integer prisonerId) {
        Assert.notNull(prisonerId, "Prisoner ID cannot be null");
        return cellRepository.findByPrisonerId(prisonerId);
    }

    @Transactional
    public Cell createCell(Cell cell) {
        Assert.notNull(cell, "Cell cannot be null");
        validateCell(cell);
        return cellRepository.save(cell);
    }

    @Transactional
    public Cell updateCell(Integer id, Cell updatedCell) {
        Assert.notNull(id, "Cell ID cannot be null");
        Assert.notNull(updatedCell, "Updated cell cannot be null");
        validateCell(updatedCell);
        
        return cellRepository.findById(id)
            .map(cell -> {
                int oldCapacity = cell.getCapacity();
                int newCapacity = updatedCell.getCapacity();
                int currentPrisonerCount = cell.getPrisoners().size();
                
                // Update cell properties
                cell.setType(updatedCell.getType());
                cell.setCapacity(newCapacity);
                cell.setBlock(updatedCell.getBlock());
                
                // If capacity is reduced and there are more prisoners than new capacity
                if (newCapacity < oldCapacity && currentPrisonerCount > newCapacity) {
                    reallocateExcessPrisoners(cell, currentPrisonerCount - newCapacity);
                }
                
                return cellRepository.save(cell);
            })
            .orElseThrow(() -> new RuntimeException("Cell not found with ID: " + id));
    }

    /**
     * Reallocates excess prisoners to available cells of the same type
     */
    private void reallocateExcessPrisoners(Cell cell, int excessCount) {
        List<Prisoner> prisonersToMove = cell.getPrisoners().stream()
            .skip(cell.getCapacity()) // Get prisoners beyond the new capacity
            .limit(excessCount)
            .toList();
        
        // Find available cells of the same type in the same block
        List<Cell> availableCells = cellRepository.findByBlockIdAndType(cell.getBlock().getId(), cell.getType())
            .stream()
            .filter(c -> !c.getId().equals(cell.getId()) && !c.isAtCapacity())
            .toList();
        
        if (availableCells.isEmpty()) {
            throw new RuntimeException("No available cells of type '" + cell.getType() + "' found for reallocation");
        }
        
        // Reallocate prisoners
        int cellIndex = 0;
        for (Prisoner prisoner : prisonersToMove) {
            // Find next available cell
            while (cellIndex < availableCells.size() && availableCells.get(cellIndex).isAtCapacity()) {
                cellIndex++;
            }
            
            if (cellIndex >= availableCells.size()) {
                throw new RuntimeException("Not enough available cells for reallocation");
            }
            
            Cell targetCell = availableCells.get(cellIndex);
            
            // Move prisoner to new cell
            cell.removePrisoner(prisoner);
            targetCell.addPrisoner(prisoner);
            
            // Save both cells
            cellRepository.save(cell);
            cellRepository.save(targetCell);
            
            // If target cell is now full, move to next cell
            if (targetCell.isAtCapacity()) {
                cellIndex++;
            }
        }
    }

    @Transactional
    public void deleteCell(Integer id) {
        Assert.notNull(id, "Cell ID cannot be null");
        Cell cell = cellRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Cell not found with ID: " + id));
        
        // Remove the cell from its block's collection
        Block block = cell.getBlock();
        if (block != null) {
            block.removeCell(cell);
        }
        
        cellRepository.deleteById(id);
    }

    private void validateCell(Cell cell) {
        Assert.hasText(cell.getType(), "Cell type cannot be null or empty");
        Assert.notNull(cell.getCapacity(), "Cell capacity cannot be null");
        Assert.isTrue(cell.getCapacity() > 0, "Cell capacity must be greater than 0");
        Assert.notNull(cell.getBlock(), "Cell must be associated with a block");
    }

    @Transactional(readOnly = true)
    public int getPrisonerCount(Integer cellId) {
        Assert.notNull(cellId, "Cell ID cannot be null");
        return cellRepository.findById(cellId)
            .map(cell -> cell.getPrisoners().size())
            .orElse(0);
    }

    @Transactional(readOnly = true)
    public boolean isAtCapacity(Integer cellId) {
        Assert.notNull(cellId, "Cell ID cannot be null");
        return cellRepository.findById(cellId)
            .map(Cell::isAtCapacity)
            .orElse(false);
    }

    @Transactional(readOnly = true)
    public int getAvailableSpace(Integer cellId) {
        Assert.notNull(cellId, "Cell ID cannot be null");
        return cellRepository.findById(cellId)
            .map(Cell::getAvailableSpace)
            .orElse(0);
    }

    @Transactional
    public void addPrisoner(Integer cellId, Prisoner prisoner) {
        Assert.notNull(cellId, "Cell ID cannot be null");
        Assert.notNull(prisoner, "Prisoner cannot be null");
        
        Cell cell = cellRepository.findById(cellId)
            .orElseThrow(() -> new RuntimeException("Cell not found with ID: " + cellId));
        
        cell.addPrisoner(prisoner);
        cellRepository.save(cell);
    }

    @Transactional
    public void removePrisoner(Integer cellId, Prisoner prisoner) {
        Assert.notNull(cellId, "Cell ID cannot be null");
        Assert.notNull(prisoner, "Prisoner cannot be null");
        
        Cell cell = cellRepository.findById(cellId)
            .orElseThrow(() -> new RuntimeException("Cell not found with ID: " + cellId));
        
        cell.removePrisoner(prisoner);
        cellRepository.save(cell);
    }

    @Transactional(readOnly = true)
    public List<Cell> getAvailableCells() {
        return cellRepository.findAll().stream()
                .filter(cell -> !cell.isAtCapacity())
                .toList();
    }
}
