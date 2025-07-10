package org.example.services;

import org.example.models.Department;
import org.example.repositories.mysql.DepartmentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.util.List;

@Service
@Transactional
public class DepartmentService {

    private final DepartmentRepository departmentRepository;

    public DepartmentService(DepartmentRepository departmentRepository) {
        this.departmentRepository = departmentRepository;
    }

    @Transactional(readOnly = true)
    public List<Department> getAllDepartments() {
        return departmentRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Department getDepartmentById(Integer id) {
        Assert.notNull(id, "Department ID cannot be null");
        return departmentRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Department not found with ID: " + id));
    }
    
    @Transactional(readOnly = true)
    public Department getDepartmentByIdWithRelations(Integer id) {
        Assert.notNull(id, "Department ID cannot be null");
        Department department = departmentRepository.findByIdWithRelations(id);
        if (department == null) {
            throw new RuntimeException("Department not found with ID: " + id);
        }
        return department;
    }

    @Transactional(readOnly = true)
    public Department getDepartmentByType(String type) {
        Assert.hasText(type, "Department type cannot be null or empty");
        return departmentRepository.findByType(type);
    }

    @Transactional(readOnly = true)
    public List<Department> getDepartmentsByStatus(String status) {
        Assert.hasText(status, "Status cannot be null or empty");
        return departmentRepository.findByStatus(status);
    }

    @Transactional(readOnly = true)
    public List<Department> getActiveDepartments() {
        return departmentRepository.findByStatus("ACTIVE");
    }

    @Transactional(readOnly = true)
    public List<Department> getInactiveDepartments() {
        return departmentRepository.findByStatus("INACTIVE");
    }

    @Transactional
    public Department createDepartment(Department department) {
        Assert.notNull(department, "Department cannot be null");
        validateDepartment(department);
        return departmentRepository.save(department);
    }

    @Transactional
    public Department updateDepartment(Integer id, Department updatedDepartment) {
        Assert.notNull(id, "Department ID cannot be null");
        Assert.notNull(updatedDepartment, "Updated department cannot be null");
        validateDepartment(updatedDepartment);
        
        return departmentRepository.findById(id)
            .map(department -> {
                department.setName(updatedDepartment.getType());
                department.setStatus(updatedDepartment.getStatus());
                return departmentRepository.save(department);
            })
            .orElseThrow(() -> new RuntimeException("Department not found with ID: " + id));
    }

    @Transactional
    public void deleteDepartment(Integer id) {
        Assert.notNull(id, "Department ID cannot be null");
        departmentRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Department not found with ID: " + id));
        departmentRepository.deleteById(id);
    }

    private void validateDepartment(Department department) {
        Assert.hasText(department.getType(), "Department name cannot be null or empty");
        Assert.hasText(department.getStatus(), "Department status cannot be null or empty");
        try {
            department.setStatus(department.getStatus()); // This will validate the status
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid status. Must be one of: ACTIVE, INACTIVE");
        }
    }

    @Transactional
    public void activateDepartment(Integer id) {
        Assert.notNull(id, "Department ID cannot be null");
        Department department = getDepartmentById(id);
        department.activate();
        departmentRepository.save(department);
    }

    @Transactional
    public void deactivateDepartment(Integer id) {
        Assert.notNull(id, "Department ID cannot be null");
        Department department = getDepartmentById(id);
        department.deactivate();
        departmentRepository.save(department);
    }

    @Transactional(readOnly = true)
    public int getContains2RelationsCount(Integer id) {
        Assert.notNull(id, "Department ID cannot be null");
        return getDepartmentById(id).getContains2RelationsCount();
    }

    @Transactional(readOnly = true)
    public int getExpensesCount(Integer id) {
        Assert.notNull(id, "Department ID cannot be null");
        return getDepartmentById(id).getExpensesCount();
    }

    @Transactional(readOnly = true)
    public List<Department> getDepartmentsByPrisonId(Integer prisonId) {
        Assert.notNull(prisonId, "Prison ID cannot be null");
        return departmentRepository.findByPrisonId(prisonId);
    }
}
