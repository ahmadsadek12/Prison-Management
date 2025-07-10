package org.example.services;

import org.example.models.Block;
import org.example.models.Cell;
import org.example.models.Room;
import org.example.repositories.mysql.BlockRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class BlockService {

    private final BlockRepository blockRepository;

    public BlockService(BlockRepository blockRepository) {
        this.blockRepository = blockRepository;
    }

    @Transactional(readOnly = true)
    public List<Block> getAllBlocks() {
        return blockRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Block> getBlockById(Integer id) {
        Assert.notNull(id, "Block ID cannot be null");
        return blockRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<Block> getBlocksByType(String type) {
        Assert.hasText(type, "Block type cannot be null or empty");
        return blockRepository.findByType(type);
    }

    @Transactional(readOnly = true)
    public List<Block> getBlocksByPrisonId(Integer prisonId) {
        List<Block> blocksWithCells = blockRepository.findByPrisonIdWithCells(prisonId);
        List<Block> blocksWithRelations = blockRepository.findByPrisonIdWithRelations(prisonId);
        
        // Merge the results
        for (Block block : blocksWithCells) {
            Block blockWithRelations = blocksWithRelations.stream()
                .filter(b -> b.getId().equals(block.getId()))
                .findFirst()
                .orElse(null);
            if (blockWithRelations != null) {
                block.setContains2(blockWithRelations.getContains2());
            }
        }
        
        return blocksWithCells;
    }

    @Transactional(readOnly = true)
    public List<Block> getBlocksByPrisonIdAndType(Integer prisonId, String type) {
        Assert.notNull(prisonId, "Prison ID cannot be null");
        Assert.hasText(type, "Block type cannot be null or empty");
        return blockRepository.findByPrisonIdAndType(prisonId, type);
    }

    @Transactional(readOnly = true)
    public List<Block> getBlocksByDepartmentId(Integer departmentId) {
        Assert.notNull(departmentId, "Department ID cannot be null");
        return blockRepository.findByDepartmentId(departmentId);
    }

    @Transactional(readOnly = true)
    public List<Block> getBlocksByPrisonIdAndDepartmentId(Integer prisonId, Integer departmentId) {
        Assert.notNull(prisonId, "Prison ID cannot be null");
        Assert.notNull(departmentId, "Department ID cannot be null");
        return blockRepository.findByPrisonIdAndDepartmentId(prisonId, departmentId);
    }

    @Transactional
    public Block createBlock(Block block) {
        Assert.notNull(block, "Block cannot be null");
        validateBlock(block);
        return blockRepository.save(block);
    }

    @Transactional
    public Block updateBlock(Integer id, Block updatedBlock) {
        Assert.notNull(id, "Block ID cannot be null");
        Assert.notNull(updatedBlock, "Updated block cannot be null");
        validateBlock(updatedBlock);
        
        return blockRepository.findById(id)
            .map(block -> {
                block.setType(updatedBlock.getType());
                block.setPrison(updatedBlock.getPrison());
                return blockRepository.save(block);
            })
            .orElseThrow(() -> new RuntimeException("Block not found with ID: " + id));
    }

    @Transactional
    public void deleteBlock(Integer id) {
        Assert.notNull(id, "Block ID cannot be null");
        Block block = blockRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Block not found with ID: " + id));
        blockRepository.delete(block);
    }

    private void validateBlock(Block block) {
        Assert.hasText(block.getType(), "Block type cannot be null or empty");
        Assert.notNull(block.getPrison(), "Block must be associated with a prison");
    }

    @Transactional(readOnly = true)
    public int getCellCount(Integer blockId) {
        Assert.notNull(blockId, "Block ID cannot be null");
        return blockRepository.findById(blockId)
            .map(block -> block.getCells().size())
            .orElse(0);
    }

    @Transactional(readOnly = true)
    public int getRoomCount(Integer blockId) {
        Assert.notNull(blockId, "Block ID cannot be null");
        return blockRepository.findById(blockId)
            .map(block -> block.getRooms().size())
            .orElse(0);
    }

    @Transactional(readOnly = true)
    public int getAvailableCellsCount(Integer prisonId) {
        Assert.notNull(prisonId, "Prison ID cannot be null");
        return blockRepository.countAvailableCellsByPrisonId(prisonId);
    }

    @Transactional(readOnly = true)
    public List<String> getBlockTypesByPrisonId(Integer prisonId) {
        Assert.notNull(prisonId, "Prison ID cannot be null");
        return blockRepository.findDistinctTypesByPrisonId(prisonId);
    }

    @Transactional(readOnly = true)
    public List<Block> getAllBlocksWithCells() {
        return blockRepository.findAllWithCells();
    }

    @Transactional(readOnly = true)
    public List<Block> getBlocksByPrisonIdWithCells(Integer prisonId) {
        Assert.notNull(prisonId, "Prison ID cannot be null");
        return blockRepository.findByPrisonIdWithCells(prisonId);
    }

    public int getAvailableCells(Integer prisonId) {
        List<Block> blocks = blockRepository.findByPrisonId(prisonId);
        int totalCells = blocks.stream()
            .flatMap(block -> block.getCells().stream())
            .mapToInt(cell -> 1)
            .sum();
        int occupiedCells = blocks.stream()
            .flatMap(block -> block.getCells().stream())
            .filter(cell -> !cell.getPrisoners().isEmpty())
            .mapToInt(cell -> 1)
            .sum();
        return totalCells - occupiedCells;
    }

    @Transactional(readOnly = true)
    public Block getBlockByIdWithRelations(Integer blockId) {
        Assert.notNull(blockId, "Block ID cannot be null");
        Block block = blockRepository.findByIdWithRelations(blockId);
        if (block == null) {
            throw new RuntimeException("Block not found with ID: " + blockId);
        }
        
        // Fetch cells with prisoners
        List<Cell> cells = blockRepository.findCellsWithPrisonersByBlockId(blockId);
        block.setCells(new HashSet<>(cells));
        
        // Fetch rooms with departments
        List<Room> rooms = blockRepository.findRoomsWithDepartmentsByBlockId(blockId);
        block.setRooms(new HashSet<>(rooms));
        
        return block;
    }
}

