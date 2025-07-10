package org.example.repositories.mysql;

import org.example.models.Gun;
import org.example.models.GunAssignment;
import org.example.models.GunAssignmentId;
import org.example.models.Staff;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface GunAssignmentRepository extends JpaRepository<GunAssignment, GunAssignmentId> {
    
    // Basic queries
    List<GunAssignment> findByGun(Gun gun);
    List<GunAssignment> findByStaff(Staff staff);
    Optional<GunAssignment> findByGunAndStaff(Gun gun, Staff staff);
    
    // Return status queries
    List<GunAssignment> findByReturned(boolean returned);
    List<GunAssignment> findByGunAndReturned(Gun gun, boolean returned);
    List<GunAssignment> findByStaffAndReturned(Staff staff, boolean returned);
    
    // Active assignments (not returned)
    List<GunAssignment> findByGunAndReturnedFalse(Gun gun);
    List<GunAssignment> findByStaffAndReturnedFalse(Staff staff);
    
    // Latest assignment queries using custom JPQL
    @Query("SELECT ga FROM GunAssignment ga WHERE ga.gun = :gun ORDER BY ga.staff.id DESC")
    Optional<GunAssignment> findTopByGunOrderByStaffIdDesc(@Param("gun") Gun gun);
    
    @Query("SELECT ga FROM GunAssignment ga WHERE ga.staff = :staff ORDER BY ga.gun.serialNumber DESC")
    Optional<GunAssignment> findTopByStaffOrderByGunDesc(@Param("staff") Staff staff);
    
    // Complex queries
    List<GunAssignment> findByGunAndStaffAndReturnedFalse(Gun gun, Staff staff);

    List<GunAssignment> findByStaffId(Integer staffId);
    
    List<GunAssignment> findByGunSerialNumber(String serialNumber);
    
    @Query("SELECT ga FROM GunAssignment ga WHERE ga.staff.id = :staffId AND ga.gun.type = :type")
    List<GunAssignment> findByStaffIdAndGunType(@Param("staffId") Integer staffId, @Param("type") String type);
    
    boolean existsByStaffIdAndGunSerialNumber(Integer staffId, String serialNumber);
    
    @Query("SELECT COUNT(ga) FROM GunAssignment ga WHERE ga.staff.id = :staffId")
    long countByStaffId(@Param("staffId") Integer staffId);
    
    @Query("SELECT COUNT(ga) FROM GunAssignment ga WHERE ga.gun.serialNumber = :serialNumber")
    long countByGunSerialNumber(@Param("serialNumber") String serialNumber);
} 