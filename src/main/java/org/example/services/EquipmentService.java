package org.example.services;

import org.example.models.Equipment;
import org.example.repositories.mysql.EquipmentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import java.util.List;

@Service
@Transactional
public class EquipmentService {

    private final EquipmentRepository equipmentRepository;

    public EquipmentService(EquipmentRepository equipmentRepository) {
        this.equipmentRepository = equipmentRepository;
    }

    @Transactional(readOnly = true)
    public List<Equipment> getAllEquipment() {
        return equipmentRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Equipment getEquipmentById(Integer id) {
        Assert.notNull(id, "Equipment ID cannot be null");
        return equipmentRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Equipment not found with ID: " + id));
    }

    @Transactional(readOnly = true)
    public Equipment getEquipmentByName(String name) {
        Assert.hasText(name, "Equipment name cannot be null or empty");
        return equipmentRepository.findByName(name);
    }

    @Transactional(readOnly = true)
    public List<Equipment> getEquipmentByRoomId(Integer roomId) {
        Assert.notNull(roomId, "Room ID cannot be null");
        return equipmentRepository.findByRoom_Id(roomId);
    }

    @Transactional
    public Equipment createEquipment(Equipment equipment) {
        Assert.notNull(equipment, "Equipment cannot be null");
        validateEquipment(equipment);
        return equipmentRepository.save(equipment);
    }

    @Transactional
    public Equipment updateEquipment(Integer id, Equipment updatedEquipment) {
        Assert.notNull(id, "Equipment ID cannot be null");
        Assert.notNull(updatedEquipment, "Updated equipment cannot be null");
        validateEquipment(updatedEquipment);
        
        return equipmentRepository.findById(id)
            .map(equipment -> {
                equipment.setName(updatedEquipment.getName());
                equipment.setAmount(updatedEquipment.getAmount());
                equipment.setDescription(updatedEquipment.getDescription());
                equipment.setRoom(updatedEquipment.getRoom());
                return equipmentRepository.save(equipment);
            })
            .orElseThrow(() -> new RuntimeException("Equipment not found with ID: " + id));
    }

    @Transactional
    public void deleteEquipment(Integer id) {
        Assert.notNull(id, "Equipment ID cannot be null");
        equipmentRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Equipment not found with ID: " + id));
        equipmentRepository.deleteById(id);
    }

    private void validateEquipment(Equipment equipment) {
        Assert.hasText(equipment.getName(), "Equipment name cannot be null or empty");
        Assert.isTrue(equipment.getAmount() >= 0, "Amount cannot be negative");
        Assert.hasText(equipment.getDescription(), "Description cannot be null or empty");
        Assert.notNull(equipment.getRoom(), "Room cannot be null");
        // Department can be null for equipment without department assignment
    }

}
