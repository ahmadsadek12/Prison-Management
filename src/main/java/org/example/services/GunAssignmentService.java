package org.example.services;

import org.example.models.Gun;
import org.example.models.GunAssignment;
import org.example.models.GunAssignmentId;
import org.example.models.Staff;
import org.example.repositories.mysql.GunAssignmentRepository;
import org.example.repositories.mysql.GunRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class GunAssignmentService {

    private final GunAssignmentRepository gunAssignmentRepository;
    private final GunRepository gunRepository;
    private final StaffService staffService;

    @Autowired
    public GunAssignmentService(GunAssignmentRepository gunAssignmentRepository,
                               GunRepository gunRepository,
                               StaffService staffService) {
        this.gunAssignmentRepository = gunAssignmentRepository;
        this.gunRepository = gunRepository;
        this.staffService = staffService;
    }

    @Transactional(readOnly = true)
    public Optional<GunAssignment> getLatestAssignmentBySerialNumber(Gun gun) {
        return gunAssignmentRepository.findTopByGunOrderByStaffIdDesc(gun);
    }

    @Transactional(readOnly = true)
    public List<GunAssignment> getActiveAssignments() {
        return gunAssignmentRepository.findByReturned(false);
    }

    @Transactional(readOnly = true)
    public List<GunAssignment> getActiveAssignmentsByGun(Gun gun) {
        return gunAssignmentRepository.findByGunAndReturnedFalse(gun);
    }

    @Transactional(readOnly = true)
    public List<GunAssignment> getActiveAssignmentsByStaff(Staff staff) {
        return gunAssignmentRepository.findByStaffAndReturnedFalse(staff);
    }

    @Transactional
    public GunAssignment assignGunToStaff(String gunSerialNumber, Integer staffId) {
        Gun gun = gunRepository.findBySerialNumber(gunSerialNumber)
                .orElseThrow(() -> new RuntimeException("Gun not found"));
        
        Staff staff = staffService.getStaffById(staffId);
        if (staff == null) {
            throw new RuntimeException("Staff not found");
        }

        if (gunAssignmentRepository.existsByStaffIdAndGunSerialNumber(staffId, gunSerialNumber)) {
            throw new RuntimeException("Gun is already assigned to this staff member");
        }

        GunAssignment assignment = new GunAssignment();
        assignment.setGun(gun);
        assignment.setStaff(staff);
        return gunAssignmentRepository.save(assignment);
    }

    @Transactional
    public void removeGunAssignment(String gunSerialNumber, Integer staffId) {
        GunAssignmentId id = new GunAssignmentId(gunSerialNumber, staffId);
        gunAssignmentRepository.deleteById(id);
    }

    public List<GunAssignment> getGunAssignmentsByStaffId(Integer staffId) {
        return gunAssignmentRepository.findByStaffId(staffId);
    }

    public List<GunAssignment> getGunAssignmentsByGunSerialNumber(String serialNumber) {
        return gunAssignmentRepository.findByGunSerialNumber(serialNumber);
    }

    public List<GunAssignment> getGunAssignmentsByStaffIdAndType(Integer staffId, String type) {
        return gunAssignmentRepository.findByStaffIdAndGunType(staffId, type);
    }

    public long getGunAssignmentCountByStaffId(Integer staffId) {
        return gunAssignmentRepository.countByStaffId(staffId);
    }

    public long getGunAssignmentCountByGunSerialNumber(String serialNumber) {
        return gunAssignmentRepository.countByGunSerialNumber(serialNumber);
    }

    @Transactional
    public GunAssignment returnGun(Gun gun) {
        Optional<GunAssignment> activeAssignment = getLatestAssignmentBySerialNumber(gun);
        if (activeAssignment.isEmpty() || activeAssignment.get().isReturned()) {
            throw new IllegalStateException("No active assignment found for this gun");
        }

        GunAssignment assignment = activeAssignment.get();
        assignment.markAsReturned();
        return gunAssignmentRepository.save(assignment);
    }

    @Transactional(readOnly = true)
    public List<GunAssignment> getAssignmentHistory(Gun gun) {
        return gunAssignmentRepository.findByGun(gun);
    }

    @Transactional(readOnly = true)
    public List<GunAssignment> getStaffAssignmentHistory(Staff staff) {
        return gunAssignmentRepository.findByStaff(staff);
    }

    @Transactional(readOnly = true)
    public List<Staff> getStaffByGunId(String gunSerialNumber) {
        List<GunAssignment> assignments = gunAssignmentRepository.findByGunSerialNumber(gunSerialNumber);
        return assignments.stream()
                .filter(assignment -> !assignment.isReturned())
                .map(GunAssignment::getStaff)
                .toList();
    }
} 