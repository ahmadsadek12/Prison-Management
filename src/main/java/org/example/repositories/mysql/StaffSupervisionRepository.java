package org.example.repositories.mysql;

import org.example.models.Staff;
import org.example.models.StaffSupervision;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface StaffSupervisionRepository extends JpaRepository<StaffSupervision, Integer> {
    
    // Basic queries
    List<StaffSupervision> findBySupervisor(Staff supervisor);
    List<StaffSupervision> findBySubordinate(Staff subordinate);
    Optional<StaffSupervision> findBySupervisorAndSubordinate(Staff supervisor, Staff subordinate);
    
    // Supervisor queries
    List<StaffSupervision> findBySupervisor_Role(String role);
    List<StaffSupervision> findBySupervisor_Department_Id(Integer departmentId);
    List<StaffSupervision> findBySupervisor_RoleAndSupervisor_Department_Id(
        String role, Integer departmentId);
    
    // Subordinate queries
    List<StaffSupervision> findBySubordinate_Role(String role);
    List<StaffSupervision> findBySubordinate_Department_Id(Integer departmentId);
    List<StaffSupervision> findBySubordinate_RoleAndSubordinate_Department_Id(
        String role, Integer departmentId);
    
    // Department-based queries
    List<StaffSupervision> findBySupervisor_Department_IdAndSubordinate_Department_Id(
        Integer supervisorDepartmentId, Integer subordinateDepartmentId);
    
    // Role-based queries
    List<StaffSupervision> findBySupervisor_RoleAndSubordinate_Role(
        String supervisorRole, String subordinateRole);
    
    // Complex queries
    List<StaffSupervision> findBySupervisor_Department_IdAndSubordinate_Role(
        Integer departmentId, String role);
    List<StaffSupervision> findBySubordinate_Department_IdAndSupervisor_Role(
        Integer departmentId, String role);
    
    // Count queries
    long countBySupervisor(Staff supervisor);
    long countBySubordinate(Staff subordinate);
    long countBySupervisor_Department_Id(Integer departmentId);
    long countBySubordinate_Department_Id(Integer departmentId);
    
    // Existence queries
    boolean existsBySupervisorAndSubordinate(Staff supervisor, Staff subordinate);
    boolean existsBySupervisor_Department_IdAndSubordinate_Department_Id(
        Integer supervisorDepartmentId, Integer subordinateDepartmentId);
}
