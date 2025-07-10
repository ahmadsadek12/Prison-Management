package org.example.services;

import org.example.models.Staff;
import org.example.models.StaffSupervision;
import org.example.repositories.mysql.StaffSupervisionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class StaffSupervisionService {

    private final StaffSupervisionRepository supervisionRepository;

    @Autowired
    public StaffSupervisionService(StaffSupervisionRepository supervisionRepository) {
        this.supervisionRepository = supervisionRepository;
    }

    @Transactional(readOnly = true)
    public List<StaffSupervision> getAllSupervisions() {
        return supervisionRepository.findAll();
    }

    @Transactional(readOnly = true)
    public StaffSupervision getSupervisionById(Integer id) {
        Assert.notNull(id, "Supervision ID cannot be null");
        return supervisionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Staff supervision not found with ID: " + id));
    }

    @Transactional
    public StaffSupervision createSupervision(Staff supervisor, Staff subordinate) {
        Assert.notNull(supervisor, "Supervisor cannot be null");
        Assert.notNull(subordinate, "Subordinate cannot be null");
        
        validateSupervisionAssignment(supervisor, subordinate);
        
        StaffSupervision supervision = new StaffSupervision(supervisor, subordinate);
        return supervisionRepository.save(supervision);
    }

    @Transactional
    public void deleteSupervision(Integer id) {
        Assert.notNull(id, "Supervision ID cannot be null");
        getSupervisionById(id); // Verify exists
        supervisionRepository.deleteById(id);
    }

    @Transactional
    public void removeSupervision(Staff supervisor, Staff subordinate) {
        Assert.notNull(supervisor, "Supervisor cannot be null");
        Assert.notNull(subordinate, "Subordinate cannot be null");
        
        supervisionRepository.findBySupervisorAndSubordinate(supervisor, subordinate)
                .ifPresent(supervision -> supervisionRepository.deleteById(supervision.getId()));
    }

    @Transactional(readOnly = true)
    public List<StaffSupervision> getSupervisionsBySupervisor(Staff supervisor) {
        Assert.notNull(supervisor, "Supervisor cannot be null");
        return supervisionRepository.findBySupervisor(supervisor);
    }

    @Transactional(readOnly = true)
    public List<StaffSupervision> getSupervisionsBySubordinate(Staff subordinate) {
        Assert.notNull(subordinate, "Subordinate cannot be null");
        return supervisionRepository.findBySubordinate(subordinate);
    }

    @Transactional(readOnly = true)
    public Optional<StaffSupervision> getSupervision(Staff supervisor, Staff subordinate) {
        Assert.notNull(supervisor, "Supervisor cannot be null");
        Assert.notNull(subordinate, "Subordinate cannot be null");
        return supervisionRepository.findBySupervisorAndSubordinate(supervisor, subordinate);
    }

    @Transactional(readOnly = true)
    public List<StaffSupervision> getSupervisionsBySupervisorRole(String role) {
        Assert.hasText(role, "Role cannot be null or empty");
        return supervisionRepository.findBySupervisor_Role(role);
    }

    @Transactional(readOnly = true)
    public List<StaffSupervision> getSupervisionsBySubordinateRole(String role) {
        Assert.hasText(role, "Role cannot be null or empty");
        return supervisionRepository.findBySubordinate_Role(role);
    }

    @Transactional(readOnly = true)
    public List<StaffSupervision> getSupervisionsBySupervisorDepartment(Integer departmentId) {
        Assert.notNull(departmentId, "Department ID cannot be null");
        return supervisionRepository.findBySupervisor_Department_Id(departmentId);
    }

    @Transactional(readOnly = true)
    public List<StaffSupervision> getSupervisionsBySubordinateDepartment(Integer departmentId) {
        Assert.notNull(departmentId, "Department ID cannot be null");
        return supervisionRepository.findBySubordinate_Department_Id(departmentId);
    }

    @Transactional(readOnly = true)
    public List<StaffSupervision> getSupervisionsByDepartments(Integer supervisorDeptId, Integer subordinateDeptId) {
        Assert.notNull(supervisorDeptId, "Supervisor department ID cannot be null");
        Assert.notNull(subordinateDeptId, "Subordinate department ID cannot be null");
        return supervisionRepository.findBySupervisor_Department_IdAndSubordinate_Department_Id(
            supervisorDeptId, subordinateDeptId);
    }

    @Transactional(readOnly = true)
    public List<StaffSupervision> getSupervisionsByRoles(String supervisorRole, String subordinateRole) {
        Assert.hasText(supervisorRole, "Supervisor role cannot be null or empty");
        Assert.hasText(subordinateRole, "Subordinate role cannot be null or empty");
        return supervisionRepository.findBySupervisor_RoleAndSubordinate_Role(supervisorRole, subordinateRole);
    }

    @Transactional(readOnly = true)
    public long getSubordinateCount(Staff supervisor) {
        Assert.notNull(supervisor, "Supervisor cannot be null");
        return supervisionRepository.countBySupervisor(supervisor);
    }

    @Transactional(readOnly = true)
    public long getSupervisorCount(Staff subordinate) {
        Assert.notNull(subordinate, "Subordinate cannot be null");
        return supervisionRepository.countBySubordinate(subordinate);
    }

    @Transactional(readOnly = true)
    public boolean hasSupervisionRelationship(Staff supervisor, Staff subordinate) {
        Assert.notNull(supervisor, "Supervisor cannot be null");
        Assert.notNull(subordinate, "Subordinate cannot be null");
        return supervisionRepository.existsBySupervisorAndSubordinate(supervisor, subordinate);
    }

    @Transactional(readOnly = true)
    public boolean hasCrossDepartmentSupervision(Integer supervisorDeptId, Integer subordinateDeptId) {
        Assert.notNull(supervisorDeptId, "Supervisor department ID cannot be null");
        Assert.notNull(subordinateDeptId, "Subordinate department ID cannot be null");
        return supervisionRepository.existsBySupervisor_Department_IdAndSubordinate_Department_Id(
            supervisorDeptId, subordinateDeptId);
    }

    private void validateSupervisionAssignment(Staff supervisor, Staff subordinate) {
        if (supervisor.equals(subordinate)) {
            throw new IllegalArgumentException("Staff member cannot supervise themselves");
        }

        if (hasSupervisionRelationship(subordinate, supervisor)) {
            throw new IllegalArgumentException("Circular supervision relationship detected");
        }

        if (hasSupervisionRelationship(supervisor, subordinate)) {
            throw new IllegalArgumentException("Supervision relationship already exists");
        }
    }
}
