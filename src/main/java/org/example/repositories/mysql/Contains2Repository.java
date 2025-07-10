package org.example.repositories.mysql;

import org.example.models.Contains2;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface Contains2Repository extends JpaRepository<Contains2, Integer> {
    
    // Find by block ID
    List<Contains2> findByBlockId(Integer blockId);
    
    // Find by department ID
    List<Contains2> findByDepartmentId(Integer departmentId);
    
    // Find by room ID
    List<Contains2> findByRoomId(Integer roomId);
    
    // Find by block ID and department ID
    List<Contains2> findByBlockIdAndDepartmentId(Integer blockId, Integer departmentId);
    
    // Find by block ID and room ID
    List<Contains2> findByBlockIdAndRoomId(Integer blockId, Integer roomId);
    
    // Find by department ID and room ID
    List<Contains2> findByDepartmentIdAndRoomId(Integer departmentId, Integer roomId);
    
    // Find by block ID, department ID, and room ID
    List<Contains2> findByBlockIdAndDepartmentIdAndRoomId(
        Integer blockId, Integer departmentId, Integer roomId);
} 