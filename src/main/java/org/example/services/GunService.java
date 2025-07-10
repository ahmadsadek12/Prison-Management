package org.example.services;

import org.example.models.Gun;
import org.example.repositories.mysql.GunRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import java.util.List;

@Service
@Transactional
public class GunService {

    private final GunRepository gunRepository;

    public GunService(GunRepository gunRepository) {
        this.gunRepository = gunRepository;
    }

    @Transactional(readOnly = true)
    public List<Gun> getAllGuns() {
        return gunRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Gun getGunBySerialNumber(String serialNumber) {
        Assert.hasText(serialNumber, "Serial number cannot be null or empty");
        return gunRepository.findBySerialNumber(serialNumber)
            .orElseThrow(() -> new RuntimeException("Gun not found with serial number: " + serialNumber));
    }

    @Transactional(readOnly = true)
    public List<Gun> getGunsByType(String type) {
        Assert.hasText(type, "Gun type cannot be null or empty");
        return gunRepository.findByType(type);
    }

    @Transactional(readOnly = true)
    public List<Gun> getGunsByAssignedStaffId(Integer staffId) {
        Assert.notNull(staffId, "Staff ID cannot be null");
        return gunRepository.findByAssignedStaffId(staffId);
    }

    @Transactional(readOnly = true)
    public List<Gun> getGunsByAssignedStaffIdAndType(Integer staffId, String type) {
        Assert.notNull(staffId, "Staff ID cannot be null");
        Assert.hasText(type, "Gun type cannot be null or empty");
        return gunRepository.findByAssignedStaffIdAndType(staffId, type);
    }

    @Transactional
    public Gun createGun(Gun gun) {
        Assert.notNull(gun, "Gun cannot be null");
        validateGun(gun);
        return gunRepository.save(gun);
    }

    @Transactional
    public Gun updateGun(String serialNumber, Gun updatedGun) {
        Assert.hasText(serialNumber, "Serial number cannot be null or empty");
        Assert.notNull(updatedGun, "Updated gun cannot be null");
        validateGun(updatedGun);
        
        return gunRepository.findBySerialNumber(serialNumber)
            .map(gun -> {
                gun.setType(updatedGun.getType());
                gun.setName(updatedGun.getName());
                return gunRepository.save(gun);
            })
            .orElseThrow(() -> new RuntimeException("Gun not found with serial number: " + serialNumber));
    }

    @Transactional
    public void deleteGun(String serialNumber) {
        Assert.hasText(serialNumber, "Serial number cannot be null or empty");
        gunRepository.findBySerialNumber(serialNumber)
            .orElseThrow(() -> new RuntimeException("Gun not found with serial number: " + serialNumber));
        gunRepository.deleteById(serialNumber);
    }

    private void validateGun(Gun gun) {
        Assert.hasText(gun.getSerialNumber(), "Serial number cannot be null or empty");
        Assert.hasText(gun.getType(), "Type cannot be null or empty");
        Assert.hasText(gun.getName(), "Name cannot be null or empty");
    }

    @Transactional(readOnly = true)
    public boolean existsBySerialNumber(String serialNumber) {
        Assert.hasText(serialNumber, "Serial number cannot be null or empty");
        return gunRepository.existsBySerialNumber(serialNumber);
    }

    @Transactional(readOnly = true)
    public long getGunCountByType(String type) {
        Assert.hasText(type, "Gun type cannot be null or empty");
        return gunRepository.countByType(type);
    }

    @Transactional(readOnly = true)
    public long getGunCountByAssignedStaffId(Integer staffId) {
        Assert.notNull(staffId, "Staff ID cannot be null");
        return gunRepository.countByAssignedStaffId(staffId);
    }
}
