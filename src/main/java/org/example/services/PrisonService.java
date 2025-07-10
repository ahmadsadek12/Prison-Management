package org.example.services;

import org.example.models.Prison;
import org.example.repositories.mysql.PrisonRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.util.List;

@Service
@Transactional
public class PrisonService {

    private final PrisonRepository prisonRepository;
    private final StaffService staffService;

    @Autowired
    public PrisonService(PrisonRepository prisonRepository, StaffService staffService) {
        this.prisonRepository = prisonRepository;
        this.staffService = staffService;
    }

    @Transactional(readOnly = true)
    public List<Prison> getAllPrisons() {
        return prisonRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Prison getPrisonById(Integer id) {
        Assert.notNull(id, "Prison ID cannot be null");
        return prisonRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Prison not found with ID: " + id));
    }

    @Transactional(readOnly = true)
    public Prison getPrisonByName(String name) {
        Assert.hasText(name, "Prison name cannot be null or empty");
        return prisonRepository.findByNameWithBlocks(name);
    }

    @Transactional(readOnly = true)
    public Prison findByNameWithBlocksAndCells(String name) {
        Assert.hasText(name, "Prison name cannot be null or empty");
        Prison prison = prisonRepository.findByNameWithBlocks(name);
        if (prison != null && prison.getBlocks() != null) {
            // Initialize cells for each block
            prison.getBlocks().forEach(block -> {
                if (block.getCells() != null) {
                    block.getCells().size(); // Force initialization
                }
            });
        }
        return prison;
    }

    @Transactional
    public Prison createPrison(Prison prison) {
        Assert.notNull(prison, "Prison cannot be null");
        validatePrison(prison);
        return prisonRepository.save(prison);
    }

    @Transactional
    public Prison updatePrison(Integer id, Prison updatedPrison) {
        Assert.notNull(id, "Prison ID cannot be null");
        Assert.notNull(updatedPrison, "Updated prison cannot be null");
        validatePrison(updatedPrison);

        Prison existingPrison = prisonRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Prison not found with ID: " + id));
        updatePrisonFields(existingPrison, updatedPrison);
        return prisonRepository.save(existingPrison);
    }

    @Transactional
    public void deletePrison(Integer id) {
        Assert.notNull(id, "Prison ID cannot be null");
        getPrisonById(id); // Verify prison exists
        prisonRepository.deleteById(id);
    }

    private void validatePrison(Prison prison) {
        Assert.hasText(prison.getName(), "Prison name cannot be null or empty");
        Assert.hasText(prison.getState(), "State cannot be null or empty");
        Assert.hasText(prison.getCity(), "City cannot be null or empty");
        Assert.hasText(prison.getStreet(), "Street cannot be null or empty");
        Assert.hasText(prison.getCountry(), "Country cannot be null or empty");
    }

    private void updatePrisonFields(Prison existingPrison, Prison updatedPrison) {
        existingPrison.setName(updatedPrison.getName());
        existingPrison.setState(updatedPrison.getState());
        existingPrison.setCity(updatedPrison.getCity());
        existingPrison.setStreet(updatedPrison.getStreet());
        existingPrison.setCountry(updatedPrison.getCountry());
    }

    public String getWardenName(Integer prisonId) {
        return staffService.findWardenByPrisonId(prisonId)
            .map(warden -> warden.getFirstName() + " " + warden.getLastName())
            .orElse(null);
    }
}
