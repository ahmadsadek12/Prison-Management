package org.example.services;

import org.example.models.EquipmentMaintenanceLog;
import org.example.repositories.mongodb.EquipmentMaintenanceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class EquipmentMaintenanceService {

    private final EquipmentMaintenanceRepository maintenanceRepository;

    public EquipmentMaintenanceService(EquipmentMaintenanceRepository maintenanceRepository) {
        this.maintenanceRepository = maintenanceRepository;
    }

    @Transactional(readOnly = true)
    public List<EquipmentMaintenanceLog> getAllMaintenanceRecords() {
        return maintenanceRepository.findAll();
    }

    @Transactional(readOnly = true)
    public EquipmentMaintenanceLog getMaintenanceRecordById(String id) {
        Assert.hasText(id, "Maintenance record ID cannot be null or empty");
        return maintenanceRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Maintenance record not found with ID: " + id));
    }

    @Transactional(readOnly = true)
    public List<EquipmentMaintenanceLog> getMaintenanceRecordsByEquipmentId(Integer equipmentId) {
        Assert.notNull(equipmentId, "Equipment ID cannot be null");
        return maintenanceRepository.findByEquipmentId(equipmentId);
    }

    @Transactional(readOnly = true)
    public List<EquipmentMaintenanceLog> getMaintenanceRecordsByType(String type) {
        Assert.hasText(type, "Type cannot be null or empty");
        return maintenanceRepository.findByType(type);
    }

    @Transactional(readOnly = true)
    public List<EquipmentMaintenanceLog> getMaintenanceRecordsByStatus(String status) {
        Assert.hasText(status, "Status cannot be null or empty");
        return maintenanceRepository.findByStatus(status);
    }

    @Transactional(readOnly = true)
    public List<EquipmentMaintenanceLog> getMaintenanceRecordsByTechnician(String technician) {
        Assert.hasText(technician, "Technician name cannot be null or empty");
        return maintenanceRepository.findByTechnician(technician);
    }

    @Transactional(readOnly = true)
    public List<EquipmentMaintenanceLog> getMaintenanceRecordsByDateRange(LocalDateTime start, LocalDateTime end) {
        Assert.notNull(start, "Start date cannot be null");
        Assert.notNull(end, "End date cannot be null");
        Assert.isTrue(!end.isBefore(start), "End date must not be before start date");
        return maintenanceRepository.findByMaintenanceDateBetween(start, end);
    }

    @Transactional(readOnly = true)
    public List<EquipmentMaintenanceLog> getActiveMaintenanceRecords() {
        return maintenanceRepository.findActiveMaintenanceRecords();
    }

    @Transactional
    public EquipmentMaintenanceLog createMaintenanceRecord(EquipmentMaintenanceLog record) {
        Assert.notNull(record, "Maintenance record cannot be null");
        validateMaintenanceRecord(record);
        return maintenanceRepository.save(record);
    }

    @Transactional
    public EquipmentMaintenanceLog updateMaintenanceRecord(String id, EquipmentMaintenanceLog updatedRecord) {
        Assert.hasText(id, "Maintenance record ID cannot be null or empty");
        Assert.notNull(updatedRecord, "Updated maintenance record cannot be null");
        validateMaintenanceRecord(updatedRecord);
        
        return maintenanceRepository.findById(id)
            .map(record -> {
                record.setEquipmentId(updatedRecord.getEquipmentId());
                record.setMaintenanceDate(updatedRecord.getMaintenanceDate());
                record.setType(updatedRecord.getType());
                record.setDescription(updatedRecord.getDescription());
                record.setTechnician(updatedRecord.getTechnician());
                record.setStatus(updatedRecord.getStatus());
                return maintenanceRepository.save(record);
            })
            .orElseThrow(() -> new RuntimeException("Maintenance record not found with ID: " + id));
    }

    @Transactional
    public void deleteMaintenanceRecord(String id) {
        Assert.hasText(id, "Maintenance record ID cannot be null or empty");
        maintenanceRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Maintenance record not found with ID: " + id));
        maintenanceRepository.deleteById(id);
    }

    private void validateMaintenanceRecord(EquipmentMaintenanceLog record) {
        Assert.notNull(record.getEquipmentId(), "Equipment ID cannot be null");
        Assert.notNull(record.getMaintenanceDate(), "Maintenance date cannot be null");
        Assert.hasText(record.getType(), "Type cannot be null or empty");
        Assert.hasText(record.getDescription(), "Description cannot be null or empty");
        Assert.hasText(record.getTechnician(), "Technician name cannot be null or empty");
        Assert.hasText(record.getStatus(), "Status cannot be null or empty");
    }

    @Transactional
    public void updateMaintenanceStatus(String id, String newStatus) {
        Assert.hasText(id, "Maintenance record ID cannot be null or empty");
        Assert.hasText(newStatus, "New status cannot be null or empty");
        
        EquipmentMaintenanceLog record = getMaintenanceRecordById(id);
        record.setStatus(newStatus);
        maintenanceRepository.save(record);
    }

    @Transactional(readOnly = true)
    public List<EquipmentMaintenanceLog> getPendingMaintenanceRecords() {
        return maintenanceRepository.findByStatus("Pending");
    }

    @Transactional(readOnly = true)
    public List<EquipmentMaintenanceLog> getInProgressMaintenanceRecords() {
        return maintenanceRepository.findByStatus("In Progress");
    }

    @Transactional(readOnly = true)
    public List<EquipmentMaintenanceLog> getCompletedMaintenanceRecords() {
        return maintenanceRepository.findByStatus("Completed");
    }
}
