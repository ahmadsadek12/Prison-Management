package org.example.services;

import org.example.models.Room;
import org.example.models.Block;
import org.example.models.Equipment;
import org.example.models.Contains2;
import org.example.models.Department;
import org.example.repositories.mysql.RoomRepository;
import org.example.repositories.mysql.Contains2Repository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.util.List;
import java.util.Set;
import java.util.HashSet;

@Service
@Transactional
public class RoomService {

    private final RoomRepository roomRepository;
    private final Contains2Repository contains2Repository;

    @Autowired
    public RoomService(RoomRepository roomRepository, Contains2Repository contains2Repository) {
        this.roomRepository = roomRepository;
        this.contains2Repository = contains2Repository;
    }

    @Transactional(readOnly = true)
    public List<Room> getAllRooms() {
        return roomRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Room getRoomById(Integer id) {
        Assert.notNull(id, "Room ID cannot be null");
        return roomRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Room not found with ID: " + id));
    }

    @Transactional(readOnly = true)
    public Room getRoomByIdWithEquipment(Integer id) {
        Assert.notNull(id, "Room ID cannot be null");
        return roomRepository.findByIdWithEquipment(id);
    }

    @Transactional(readOnly = true)
    public Room getRoomByIdBasic(Integer id) {
        Assert.notNull(id, "Room ID cannot be null");
        return roomRepository.findByIdBasic(id);
    }
    
    @Transactional(readOnly = true)
    public Room getRoomByIdWithRelations(Integer id) {
        Assert.notNull(id, "Room ID cannot be null");
        return roomRepository.findByIdWithRelations(id);
    }
    
    @Transactional(readOnly = true)
    public Room getRoomByIdWithRelationsSafe(Integer id) {
        Assert.notNull(id, "Room ID cannot be null");
        return roomRepository.findByIdWithRelationsSafe(id);
    }
    
    @Transactional(readOnly = true)
    public List<Equipment> getValidEquipmentByRoomId(Integer roomId) {
        Assert.notNull(roomId, "Room ID cannot be null");
        return roomRepository.findValidEquipmentByRoomId(roomId);
    }

    @Transactional(readOnly = true)
    public List<Room> getRoomsByBlockId(Integer blockId) {
        Assert.notNull(blockId, "Block ID cannot be null");
        return roomRepository.findByBlocks_Id(blockId);
    }

    @Transactional
    public Room createRoom(Room room) {
        Assert.notNull(room, "Room cannot be null");
        validateRoom(room);
        return roomRepository.save(room);
    }

    @Transactional
    public Room createRoom(String type, String description) {
        Assert.hasText(type, "Room type cannot be null or empty");
        Room room = new Room(type, description);
        return createRoom(room);
    }

    @Transactional
    public Room updateRoom(Integer id, Room updatedRoom) {
        Assert.notNull(id, "Room ID cannot be null");
        Assert.notNull(updatedRoom, "Updated room cannot be null");
        validateRoom(updatedRoom);

        Room existingRoom = getRoomById(id);
        updateRoomFields(existingRoom, updatedRoom);
        return roomRepository.save(existingRoom);
    }

    @Transactional
    public void deleteRoom(Integer id) {
        Assert.notNull(id, "Room ID cannot be null");
        Room room = getRoomById(id);
        
        // Remove room from all blocks (clean up many-to-many relationship)
        Set<Block> blocksToRemove = new HashSet<>(room.getBlocks());
        for (Block block : blocksToRemove) {
            room.removeBlock(block);
        }
        
        // Delete all Contains2 relationships for this room
        List<Contains2> contains2Relations = contains2Repository.findByRoomId(id);
        contains2Repository.deleteAll(contains2Relations);
        
        // Finally delete the room (equipment will be deleted due to cascade)
        roomRepository.deleteById(id);
    }

    @Transactional
    public Room addEquipmentToRoom(Integer roomId, Equipment equipment) {
        Assert.notNull(roomId, "Room ID cannot be null");
        Assert.notNull(equipment, "Equipment cannot be null");

        Room room = getRoomById(roomId);
        room.addEquipment(equipment);
        return roomRepository.save(room);
    }

    @Transactional
    public Room removeEquipmentFromRoom(Integer roomId, Equipment equipment) {
        Assert.notNull(roomId, "Room ID cannot be null");
        Assert.notNull(equipment, "Equipment cannot be null");

        Room room = getRoomById(roomId);
        room.removeEquipment(equipment);
        return roomRepository.save(room);
    }

    @Transactional
    public Room assignToBlock(Integer roomId, Block block, Department department) {
        Assert.notNull(roomId, "Room ID cannot be null");
        Assert.notNull(block, "Block cannot be null");
        Assert.notNull(department, "Department cannot be null");

        Room room = getRoomById(roomId);
        
        // Create the Contains2 relationship
        Contains2 contains2 = new Contains2(block, department, room);
        contains2Repository.save(contains2);
        
        return room;
    }

    @Transactional
    public void removeFromBlock(Integer roomId, Block block, Department department) {
        Assert.notNull(roomId, "Room ID cannot be null");
        Assert.notNull(block, "Block cannot be null");
        Assert.notNull(department, "Department cannot be null");

        List<Contains2> contains2List = contains2Repository.findByBlockIdAndDepartmentIdAndRoomId(
            block.getId(), department.getId(), roomId);
            
        if (contains2List.isEmpty()) {
            throw new RuntimeException("Room is not assigned to this block and department");
        }
        
        contains2Repository.deleteAll(contains2List);
    }

    @Transactional(readOnly = true)
    public int getEquipmentCount(Integer roomId) {
        Assert.notNull(roomId, "Room ID cannot be null");
        Room room = getRoomById(roomId);
        return room.getEquipmentCount();
    }

    @Transactional(readOnly = true)
    public boolean hasEquipment(Integer roomId) {
        Assert.notNull(roomId, "Room ID cannot be null");
        Room room = getRoomById(roomId);
        return room.hasEquipment();
    }

    @Transactional(readOnly = true)
    public boolean isInBlock(Integer roomId, Block block) {
        Assert.notNull(roomId, "Room ID cannot be null");
        Assert.notNull(block, "Block cannot be null");
        Room room = getRoomById(roomId);
        return room.getBlocks().contains(block);
    }

    private void validateRoom(Room room) {
        Assert.hasText(room.getType(), "Room type cannot be null or empty");
        // Block is now optional since it's a many-to-many relationship
    }

    private void updateRoomFields(Room existingRoom, Room updatedRoom) {
        existingRoom.setType(updatedRoom.getType());
        existingRoom.setDescription(updatedRoom.getDescription());
        
        // Don't update collections to avoid lazy loading issues
        // The blocks and equipment relationships should remain unchanged
        // Only update the basic room properties
    }
}
