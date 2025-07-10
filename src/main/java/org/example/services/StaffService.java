package org.example.services;

import org.example.models.Staff;
import org.example.models.StaffSupervision;
import org.example.models.GunAssignment;
import org.example.models.Schedule;
import org.example.repositories.mysql.StaffRepository;
import org.example.repositories.mysql.StaffSupervisionRepository;
import org.example.repositories.mysql.GunAssignmentRepository;
import org.example.repositories.mysql.ScheduleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.logging.Logger;

@Service
@Transactional
public class StaffService {

    private final StaffRepository staffRepository;
    private final StaffSupervisionRepository staffSupervisionRepository;
    private final GunAssignmentRepository gunAssignmentRepository;
    private final ScheduleRepository scheduleRepository;
    private static final Logger LOGGER = Logger.getLogger(StaffService.class.getName());

    @Autowired
    public StaffService(StaffRepository staffRepository, StaffSupervisionRepository staffSupervisionRepository,
                       GunAssignmentRepository gunAssignmentRepository, ScheduleRepository scheduleRepository) {
        this.staffRepository = staffRepository;
        this.staffSupervisionRepository = staffSupervisionRepository;
        this.gunAssignmentRepository = gunAssignmentRepository;
        this.scheduleRepository = scheduleRepository;
    }

    @Transactional(readOnly = true)
    public List<Staff> getAllStaff() {
        return staffRepository.findAllWithDepartment();
    }
    
    @Transactional(readOnly = true)
    public List<Staff> getAllStaffWithSupervisors() {
        return staffRepository.findAllWithDepartmentAndSupervisors();
    }

    @Transactional(readOnly = true)
    public Staff getStaffById(Integer id) {
        Assert.notNull(id, "Staff ID cannot be null");
        return staffRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Staff not found with ID: " + id));
    }

    @Transactional(readOnly = true)
    public Staff getStaffByIdWithRelations(Integer id) {
        Assert.notNull(id, "Staff ID cannot be null");
        return staffRepository.findByIdWithDepartmentAndSupervisors(id)
                .orElseThrow(() -> new RuntimeException("Staff not found with ID: " + id));
    }

    @Transactional(readOnly = true)
    public Optional<Staff> getStaffByName(String name) {
        Assert.hasText(name, "Name cannot be null or empty");
        return staffRepository.findByName(name);
    }

    @Transactional
    public Staff createStaff(Staff staff) {
        Assert.notNull(staff, "Staff cannot be null");
        validateStaff(staff);
        return staffRepository.save(staff);
    }

    @Transactional
    public Staff updateStaff(Integer id, Staff updatedStaff) {
        Assert.notNull(id, "Staff ID cannot be null");
        Assert.notNull(updatedStaff, "Updated staff cannot be null");
        validateStaff(updatedStaff);

        Staff existingStaff = getStaffById(id);
        updateStaffFields(existingStaff, updatedStaff);
        return staffRepository.save(existingStaff);
    }

    @Transactional
    public void deleteStaff(Integer id) {
        Assert.notNull(id, "Staff ID cannot be null");
        Staff staff = getStaffById(id); // Verify exists and get the staff object
        
        // First, remove all supervision relationships where this staff is involved
        // Remove relationships where this staff is a supervisor
        List<StaffSupervision> supervisedRelations = staffSupervisionRepository.findBySupervisor(staff);
        staffSupervisionRepository.deleteAll(supervisedRelations);
        
        // Remove relationships where this staff is a subordinate
        List<StaffSupervision> supervisorRelations = staffSupervisionRepository.findBySubordinate(staff);
        staffSupervisionRepository.deleteAll(supervisorRelations);
        
        // Remove all gun assignments for this staff
        List<GunAssignment> gunAssignments = gunAssignmentRepository.findByStaff(staff);
        gunAssignmentRepository.deleteAll(gunAssignments);
        
        // Remove all schedules for this staff
        List<Schedule> schedules = scheduleRepository.findByStaffId(staff.getId());
        scheduleRepository.deleteAll(schedules);
        
        // Now delete the staff member
        staffRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<Staff> getStaffByRole(String role) {
        Assert.hasText(role, "Role cannot be null or empty");
        return staffRepository.findByRole(role);
    }

    @Transactional(readOnly = true)
    public List<Staff> getStaffByGender(String gender) {
        Assert.hasText(gender, "Gender cannot be null or empty");
        return staffRepository.findByGender(gender);
    }

    @Transactional(readOnly = true)
    public List<Staff> getStaffByDepartment(Integer departmentId) {
        Assert.notNull(departmentId, "Department ID cannot be null");
        return staffRepository.findByDepartment_IdWithSupervisors(departmentId);
    }

    @Transactional(readOnly = true)
    public List<Staff> getStaffByDepartmentAndRole(Integer departmentId, String role) {
        Assert.notNull(departmentId, "Department ID cannot be null");
        Assert.hasText(role, "Role cannot be null or empty");
        return staffRepository.findByDepartment_IdAndRole(departmentId, role);
    }

    @Transactional(readOnly = true)
    public List<Staff> getStaffBySalaryRange(BigDecimal minSalary, BigDecimal maxSalary) {
        Assert.notNull(minSalary, "Minimum salary cannot be null");
        Assert.notNull(maxSalary, "Maximum salary cannot be null");
        Assert.isTrue(maxSalary.compareTo(minSalary) >= 0, "Maximum salary must be greater than or equal to minimum salary");
        return staffRepository.findBySalaryBetween(minSalary, maxSalary);
    }

    @Transactional(readOnly = true)
    public List<Staff> getStaffByDepartmentAndSalaryRange(Integer departmentId, BigDecimal minSalary, BigDecimal maxSalary) {
        Assert.notNull(departmentId, "Department ID cannot be null");
        Assert.notNull(minSalary, "Minimum salary cannot be null");
        Assert.notNull(maxSalary, "Maximum salary cannot be null");
        Assert.isTrue(maxSalary.compareTo(minSalary) >= 0, "Maximum salary must be greater than or equal to minimum salary");
        return staffRepository.findStaffByDepartmentAndSalaryRange(departmentId, minSalary, maxSalary);
    }

    @Transactional(readOnly = true)
    public List<Staff> getStaffByAgeRange(LocalDate startDate, LocalDate endDate) {
        Assert.notNull(startDate, "Start date cannot be null");
        Assert.notNull(endDate, "End date cannot be null");
        Assert.isTrue(!endDate.isBefore(startDate), "End date must not be before start date");
        return staffRepository.findByDobBetween(startDate, endDate);
    }

    @Transactional(readOnly = true)
    public BigDecimal getAverageSalaryByDepartment(Integer departmentId) {
        Assert.notNull(departmentId, "Department ID cannot be null");
        return staffRepository.findAverageSalaryByDepartment(departmentId)
                .orElse(BigDecimal.ZERO);
    }

    @Transactional(readOnly = true)
    public long getStaffCountByDepartmentAndGender(Integer departmentId, String gender) {
        Assert.notNull(departmentId, "Department ID cannot be null");
        Assert.hasText(gender, "Gender cannot be null or empty");
        return staffRepository.countStaffByDepartmentAndGender(departmentId, gender);
    }

    @Transactional(readOnly = true)
    public Map<String, Long> getStaffCountByRole(Integer departmentId) {
        Assert.notNull(departmentId, "Department ID cannot be null");
        List<Object[]> results = staffRepository.countStaffByRoleInDepartment(departmentId);
        return results.stream()
                .collect(Collectors.toMap(
                    row -> (String) row[0],
                    row -> (Long) row[1]
                ));
    }

    @Transactional(readOnly = true)
    public List<Staff> getRetirementEligibleStaff() {
        LocalDate retirementDate = LocalDate.now().minusYears(65);
        return staffRepository.findByDobBefore(retirementDate);
    }

    @Transactional(readOnly = true)
    public Optional<String> getWardenNameByPrisonId(Integer prisonId) {
        Assert.notNull(prisonId, "Prison ID cannot be null");
        List<Staff> wardens = staffRepository.findWardenByPrisonId(prisonId);
        return wardens.isEmpty() ? Optional.empty() : Optional.of(wardens.get(0).getName());
    }

    @Transactional(readOnly = true)
    public int getTotalStaffCount(Integer prisonId) {
        Assert.notNull(prisonId, "Prison ID cannot be null");
        return staffRepository.countByPrisonId(prisonId);
    }

    public Optional<Staff> findWardenByPrisonId(Integer prisonId) {
        try {
            return staffRepository.findByPrisonIdAndRole(prisonId, "Warden");
        } catch (Exception e) {
            LOGGER.warning("Multiple wardens found for prison " + prisonId + ". Using most recent warden.");
            return staffRepository.findByPrisonIdAndRole(prisonId, "Warden");
        }
    }

    public int getTotalStaff(Integer prisonId) {
        return staffRepository.countByPrisonId(prisonId);
    }

    @Transactional(readOnly = true)
    public Optional<Staff> getStaffByRoleAndPrisonId(String role, Integer prisonId) {
        Assert.hasText(role, "Role cannot be null or empty");
        Assert.notNull(prisonId, "Prison ID cannot be null");
        return staffRepository.findByRoleAndDepartment_PrisonId(role, prisonId);
    }

    @Transactional(readOnly = true)
    public Staff getSupervisor(Integer staffId) {
        Assert.notNull(staffId, "Staff ID cannot be null");
        Staff staff = getStaffById(staffId);
        return staff.getSupervisor();
    }

    @Transactional(readOnly = true)
    public List<Staff> getSupervisedStaff(Integer supervisorId) {
        Assert.notNull(supervisorId, "Supervisor ID cannot be null");
        Staff supervisor = getStaffById(supervisorId);
        return supervisor.getSupervisedStaff().stream().collect(Collectors.toList());
    }

    @Transactional
    public void assignSupervisor(Integer subordinateId, Integer supervisorId) {
        Assert.notNull(subordinateId, "Subordinate ID cannot be null");
        Assert.notNull(supervisorId, "Supervisor ID cannot be null");
        
        if (subordinateId.equals(supervisorId)) {
            throw new IllegalArgumentException("Staff cannot supervise themselves");
        }
        
        Staff subordinate = getStaffById(subordinateId);
        Staff supervisor = getStaffById(supervisorId);
        
        // Remove existing supervisor relationships for this subordinate
        List<StaffSupervision> existingSupervisions = staffSupervisionRepository.findBySubordinate(subordinate);
        staffSupervisionRepository.deleteAll(existingSupervisions);
        
        // Create new supervisor relationship
        StaffSupervision supervision = new StaffSupervision(supervisor, subordinate);
        staffSupervisionRepository.save(supervision);
    }

    @Transactional
    public void removeSupervisor(Integer subordinateId) {
        Assert.notNull(subordinateId, "Subordinate ID cannot be null");
        Staff subordinate = getStaffById(subordinateId);
        
        // Remove all supervisor relationships for this subordinate
        List<StaffSupervision> existingSupervisions = staffSupervisionRepository.findBySubordinate(subordinate);
        staffSupervisionRepository.deleteAll(existingSupervisions);
    }

    @Transactional(readOnly = true)
    public List<Staff> getStaffWithoutSupervisor() {
        return staffRepository.findAll().stream()
            .filter(staff -> !staff.hasSupervisor())
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<Staff> getPotentialSupervisors(Integer excludeStaffId) {
        Assert.notNull(excludeStaffId, "Staff ID to exclude cannot be null");
        return staffRepository.findAll().stream()
            .filter(staff -> !staff.getId().equals(excludeStaffId))
            .collect(Collectors.toList());
    }

    private void validateStaff(Staff staff) {
        Assert.hasText(staff.getName(), "Name cannot be null or empty");
        Assert.notNull(staff.getSalary(), "Salary cannot be null");
        Assert.isTrue(staff.getSalary().compareTo(BigDecimal.ZERO) > 0, "Salary must be greater than 0");
        Assert.hasText(staff.getPhone(), "Phone number cannot be null or empty");
        Assert.notNull(staff.getDob(), "Date of birth cannot be null");
        Assert.hasText(staff.getGender(), "Gender cannot be null or empty");
        Assert.hasText(staff.getRole(), "Role cannot be null or empty");
        Assert.notNull(staff.getDepartment(), "Department cannot be null");
    }

    private void updateStaffFields(Staff existingStaff, Staff updatedStaff) {
        existingStaff.setName(updatedStaff.getName());
        existingStaff.setSalary(updatedStaff.getSalary());
        existingStaff.setPhone(updatedStaff.getPhone());
        existingStaff.setDob(updatedStaff.getDob());
        existingStaff.setGender(updatedStaff.getGender());
        existingStaff.setRole(updatedStaff.getRole());
        
        if (updatedStaff.getDepartment() != null && !updatedStaff.getDepartment().equals(existingStaff.getDepartment())) {
            existingStaff.setDepartment(updatedStaff.getDepartment());
        }
    }
}
