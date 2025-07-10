package org.example.repositories.mysql;

import org.example.models.Gun;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface GunRepository extends JpaRepository<Gun, String> {
    
    // Basic queries
    Optional<Gun> findBySerialNumber(String serialNumber);
    List<Gun> findByType(String type);
    
    // Staff-related queries
    @Query("SELECT g FROM Gun g JOIN g.assignedStaff s WHERE s.id = :staffId")
    List<Gun> findByAssignedStaffId(@Param("staffId") Integer staffId);
    
    @Query("SELECT g FROM Gun g JOIN g.assignedStaff s WHERE s.id = :staffId AND g.type = :type")
    List<Gun> findByAssignedStaffIdAndType(@Param("staffId") Integer staffId, @Param("type") String type);
    
    // Exists queries
    boolean existsBySerialNumber(String serialNumber);
    
    // Count queries
    long countByType(String type);
    
    @Query("SELECT COUNT(g) FROM Gun g JOIN g.assignedStaff s WHERE s.id = :staffId")
    long countByAssignedStaffId(@Param("staffId") Integer staffId);
}
