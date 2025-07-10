package org.example.repositories.mysql;

import org.example.models.Staff;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.math.BigDecimal;
import java.time.LocalDate;

@Repository
public interface StaffRepository extends JpaRepository<Staff, Integer> {
    
    // Basic queries
    Optional<Staff> findByName(String name);
    List<Staff> findByRole(String role);
    List<Staff> findByGender(String gender);
    
    @Query("SELECT s FROM Staff s JOIN FETCH s.department")
    List<Staff> findAllWithDepartment();
    
    @Query("SELECT s FROM Staff s JOIN FETCH s.department LEFT JOIN FETCH s.supervisors")
    List<Staff> findAllWithDepartmentAndSupervisors();
    
    @Query("SELECT s FROM Staff s JOIN FETCH s.department LEFT JOIN FETCH s.supervisors WHERE s.id = :id")
    Optional<Staff> findByIdWithDepartmentAndSupervisors(@Param("id") Integer id);
    
    // Department queries
    List<Staff> findByDepartment_Id(Integer departmentId);
    List<Staff> findByDepartment_IdAndRole(Integer departmentId, String role);
    List<Staff> findByDepartment_IdAndGender(Integer departmentId, String gender);
    
    @Query("SELECT s FROM Staff s JOIN FETCH s.department LEFT JOIN FETCH s.supervisors WHERE s.department.id = :departmentId")
    List<Staff> findByDepartment_IdWithSupervisors(@Param("departmentId") Integer departmentId);
    
    // Salary queries
    List<Staff> findBySalaryGreaterThan(BigDecimal salary);
    List<Staff> findBySalaryLessThan(BigDecimal salary);
    List<Staff> findBySalaryBetween(BigDecimal minSalary, BigDecimal maxSalary);
    List<Staff> findByDepartment_IdAndSalaryGreaterThan(Integer departmentId, BigDecimal salary);
    List<Staff> findByDepartment_IdAndSalaryLessThan(Integer departmentId, BigDecimal salary);
    List<Staff> findByDepartment_IdAndSalaryBetween(Integer departmentId, BigDecimal minSalary, BigDecimal maxSalary);
    
    // Date of birth queries
    List<Staff> findByDobBefore(LocalDate date);
    List<Staff> findByDobAfter(LocalDate date);
    List<Staff> findByDobBetween(LocalDate startDate, LocalDate endDate);
    List<Staff> findByDepartment_IdAndDobBefore(Integer departmentId, LocalDate date);
    List<Staff> findByDepartment_IdAndDobAfter(Integer departmentId, LocalDate date);
    List<Staff> findByDepartment_IdAndDobBetween(Integer departmentId, LocalDate startDate, LocalDate endDate);
    
    // Complex queries
    List<Staff> findByDepartment_IdAndRoleAndGender(Integer departmentId, String role, String gender);
    List<Staff> findByDepartment_IdAndRoleAndSalaryBetween(Integer departmentId, String role, BigDecimal minSalary, BigDecimal maxSalary);
    List<Staff> findByDepartment_IdAndGenderAndSalaryBetween(Integer departmentId, String gender, BigDecimal minSalary, BigDecimal maxSalary);
    
    // Count queries
    long countByDepartment_Id(Integer departmentId);
    long countByDepartment_IdAndRole(Integer departmentId, String role);
    long countByDepartment_IdAndGender(Integer departmentId, String gender);
    
    // Custom queries
    @Query("SELECT s FROM Staff s WHERE s.department.id = :departmentId AND s.salary > :minSalary AND s.salary < :maxSalary")
    List<Staff> findStaffByDepartmentAndSalaryRange(
        @Param("departmentId") Integer departmentId,
        @Param("minSalary") BigDecimal minSalary,
        @Param("maxSalary") BigDecimal maxSalary
    );
    
    @Query("SELECT s FROM Staff s WHERE s.department.id = :departmentId AND s.role = :role AND s.salary > :minSalary")
    List<Staff> findStaffByDepartmentRoleAndMinSalary(
        @Param("departmentId") Integer departmentId,
        @Param("role") String role,
        @Param("minSalary") BigDecimal minSalary
    );
    
    @Query("SELECT s FROM Staff s WHERE s.department.id = :departmentId AND s.dob BETWEEN :startDate AND :endDate")
    List<Staff> findStaffByDepartmentAndDobRange(
        @Param("departmentId") Integer departmentId,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );
    
    // Statistics queries
    @Query("SELECT AVG(s.salary) FROM Staff s WHERE s.department.id = :departmentId")
    Optional<BigDecimal> findAverageSalaryByDepartment(@Param("departmentId") Integer departmentId);
    
    @Query("SELECT COUNT(s) FROM Staff s WHERE s.department.id = :departmentId AND s.gender = :gender")
    long countStaffByDepartmentAndGender(
        @Param("departmentId") Integer departmentId,
        @Param("gender") String gender
    );
    
    @Query("SELECT s.role, COUNT(s) FROM Staff s WHERE s.department.id = :departmentId GROUP BY s.role")
    List<Object[]> countStaffByRoleInDepartment(@Param("departmentId") Integer departmentId);

    @Query("SELECT DISTINCT s FROM Staff s JOIN s.department d JOIN d.contains2Relations c2 JOIN c2.block b WHERE b.prison.id = :prisonId AND s.role = 'WARDEN' ORDER BY s.id DESC")
    List<Staff> findWardenByPrisonId(@Param("prisonId") Integer prisonId);

    @Query("SELECT COUNT(DISTINCT s) FROM Staff s JOIN s.department d JOIN d.contains2Relations c2 JOIN c2.block b WHERE b.prison.id = :prisonId")
    int countByPrisonId(@Param("prisonId") Integer prisonId);

    @Query("SELECT DISTINCT s FROM Staff s JOIN s.department d JOIN d.contains2Relations c2 JOIN c2.block b WHERE b.prison.id = :prisonId AND s.role = :role ORDER BY s.id DESC LIMIT 1")
    Optional<Staff> findByRoleAndDepartment_PrisonId(@Param("role") String role, @Param("prisonId") Integer prisonId);

    @Query("SELECT s FROM Staff s JOIN s.department d JOIN d.contains2Relations c2 JOIN c2.block b WHERE b.prison.id = :prisonId AND s.role = :role ORDER BY s.id DESC LIMIT 1")
    Optional<Staff> findByPrisonIdAndRole(@Param("prisonId") Integer prisonId, @Param("role") String role);
}
