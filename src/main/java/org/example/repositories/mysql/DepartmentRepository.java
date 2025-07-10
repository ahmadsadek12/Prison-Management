package org.example.repositories.mysql;

import org.example.models.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, Integer> {
    
    // Find department by type
    Department findByType(String type);
    
    // Find all departments by status
    List<Department> findByStatus(String status);

    // Find departments by prison ID
    List<Department> findByPrisonId(Integer prisonId);
    
    // Find department with all relationships eagerly loaded
    @Query("SELECT d FROM Department d " +
           "LEFT JOIN FETCH d.contains2Relations c2 " +
           "LEFT JOIN FETCH c2.block b " +
           "LEFT JOIN FETCH b.prison " +
           "LEFT JOIN FETCH d.expenses " +
           "WHERE d.id = :id")
    Department findByIdWithRelations(@Param("id") Integer id);
}
