package org.example.services;

import org.example.models.Contains2;
import org.example.models.Block;
import org.example.models.Department;
import org.example.models.Room;
import org.example.repositories.mysql.Contains2Repository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.util.List;

@Service
@Transactional
public class Contains2Service {

    private final Contains2Repository contains2Repository;

    @Autowired
    public Contains2Service(Contains2Repository contains2Repository) {
        this.contains2Repository = contains2Repository;
    }

    @Transactional(readOnly = true)
    public List<Contains2> getAllContains2() {
        return contains2Repository.findAll();
    }

    @Transactional(readOnly = true)
    public Contains2 getContains2ById(Integer id) {
        Assert.notNull(id, "Contains2 ID cannot be null");
        return contains2Repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Contains2 relationship not found with ID: " + id));
    }

    @Transactional
    public Contains2 createContains2(Contains2 contains2) {
        Assert.notNull(contains2, "Contains2 relationship cannot be null");
        validateContains2(contains2);
        return contains2Repository.save(contains2);
    }

    @Transactional
    public Contains2 updateContains2(Integer id, Contains2 updatedContains2) {
        Assert.notNull(id, "Contains2 ID cannot be null");
        Assert.notNull(updatedContains2, "Updated Contains2 relationship cannot be null");
        validateContains2(updatedContains2);

        Contains2 existingContains2 = getContains2ById(id);
        updateContains2Fields(existingContains2, updatedContains2);
        return contains2Repository.save(existingContains2);
    }

    @Transactional
    public void deleteContains2(Integer id) {
        Assert.notNull(id, "Contains2 ID cannot be null");
        getContains2ById(id); // Verify exists
        contains2Repository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<Contains2> getContains2ByBlockId(Integer blockId) {
        Assert.notNull(blockId, "Block ID cannot be null");
        return contains2Repository.findByBlockId(blockId);
    }

    @Transactional(readOnly = true)
    public List<Contains2> getContains2ByDepartmentId(Integer departmentId) {
        Assert.notNull(departmentId, "Department ID cannot be null");
        return contains2Repository.findByDepartmentId(departmentId);
    }

    @Transactional(readOnly = true)
    public List<Contains2> getContains2ByRoomId(Integer roomId) {
        Assert.notNull(roomId, "Room ID cannot be null");
        return contains2Repository.findByRoomId(roomId);
    }

    @Transactional(readOnly = true)
    public List<Contains2> getContains2ByBlockAndDepartment(Integer blockId, Integer departmentId) {
        Assert.notNull(blockId, "Block ID cannot be null");
        Assert.notNull(departmentId, "Department ID cannot be null");
        return contains2Repository.findByBlockIdAndDepartmentId(blockId, departmentId);
    }

    @Transactional(readOnly = true)
    public List<Contains2> getContains2ByBlockAndRoom(Integer blockId, Integer roomId) {
        Assert.notNull(blockId, "Block ID cannot be null");
        Assert.notNull(roomId, "Room ID cannot be null");
        return contains2Repository.findByBlockIdAndRoomId(blockId, roomId);
    }

    @Transactional(readOnly = true)
    public List<Contains2> getContains2ByDepartmentAndRoom(Integer departmentId, Integer roomId) {
        Assert.notNull(departmentId, "Department ID cannot be null");
        Assert.notNull(roomId, "Room ID cannot be null");
        return contains2Repository.findByDepartmentIdAndRoomId(departmentId, roomId);
    }

    @Transactional(readOnly = true)
    public List<Contains2> getContains2ByBlockDepartmentAndRoom(Integer blockId, Integer departmentId, Integer roomId) {
        Assert.notNull(blockId, "Block ID cannot be null");
        Assert.notNull(departmentId, "Department ID cannot be null");
        Assert.notNull(roomId, "Room ID cannot be null");
        return contains2Repository.findByBlockIdAndDepartmentIdAndRoomId(blockId, departmentId, roomId);
    }

    @Transactional
    public Contains2 assignBlockToDepartmentAndRoom(Block block, Department department, Room room) {
        Assert.notNull(block, "Block cannot be null");
        Assert.notNull(department, "Department cannot be null");
        Assert.notNull(room, "Room cannot be null");

        Contains2 contains2 = new Contains2(block, department, room);
        return createContains2(contains2);
    }

    private void validateContains2(Contains2 contains2) {
        Assert.notNull(contains2.getBlock(), "Block cannot be null");
        Assert.notNull(contains2.getDepartment(), "Department cannot be null");
        Assert.notNull(contains2.getRoom(), "Room cannot be null");
    }

    private void updateContains2Fields(Contains2 existingContains2, Contains2 updatedContains2) {
        existingContains2.setBlock(updatedContains2.getBlock());
        existingContains2.setDepartment(updatedContains2.getDepartment());
        existingContains2.setRoom(updatedContains2.getRoom());
    }
} 