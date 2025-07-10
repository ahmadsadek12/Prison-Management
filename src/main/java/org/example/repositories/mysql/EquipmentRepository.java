package org.example.repositories.mysql;

import org.example.models.Equipment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface EquipmentRepository extends JpaRepository<Equipment, Integer> {
    
    // Find equipment by name
    Equipment findByName(String name);
    
    // Find all equipment by room ID
    List<Equipment> findByRoom_Id(Integer roomId);   
}
