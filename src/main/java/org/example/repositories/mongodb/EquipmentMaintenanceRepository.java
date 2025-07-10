package org.example.repositories.mongodb;

import org.example.models.EquipmentMaintenanceLog;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface EquipmentMaintenanceRepository extends MongoRepository<EquipmentMaintenanceLog, String> {
    
    // Find all maintenance records for a specific equipment
    List<EquipmentMaintenanceLog> findByEquipmentId(Integer equipmentId);
    
    // Find all maintenance records by type
    List<EquipmentMaintenanceLog> findByType(String type);
    
    // Find all maintenance records with a specific status
    List<EquipmentMaintenanceLog> findByStatus(String status);
    
    // Find all maintenance records for a specific technician
    List<EquipmentMaintenanceLog> findByTechnician(String technician);
    
    // Find all maintenance records between two dates
    List<EquipmentMaintenanceLog> findByMaintenanceDateBetween(LocalDateTime start, LocalDateTime end);
    
    // Find all active maintenance records (status not completed)
    @Query("{ 'status': { $ne: 'Completed' } }")
    List<EquipmentMaintenanceLog> findActiveMaintenanceRecords();
}
