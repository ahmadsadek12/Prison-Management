package org.example.services;

import org.example.models.Prisoner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.example.repositories.mysql.PrisonerRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class PrisonerService {

    private final PrisonerRepository prisonerRepository;

    @Autowired
    public PrisonerService(PrisonerRepository prisonerRepository) {
        this.prisonerRepository = prisonerRepository;
    }

    @Transactional(readOnly = true)
    public List<Prisoner> getAllPrisoners() {
        return prisonerRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Prisoner getPrisonerById(Integer id) {
        Assert.notNull(id, "Prisoner ID cannot be null");
        return prisonerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Prisoner not found with ID: " + id));
    }

    @Transactional
    public Prisoner createPrisoner(Prisoner prisoner) {
        Assert.notNull(prisoner, "Prisoner cannot be null");
        validatePrisoner(prisoner);
        return prisonerRepository.save(prisoner);
    }

    @Transactional
    public Prisoner updatePrisoner(Integer id, Prisoner updatedPrisoner) {
        Assert.notNull(id, "Prisoner ID cannot be null");
        Assert.notNull(updatedPrisoner, "Updated prisoner cannot be null");
        validatePrisoner(updatedPrisoner);

        Prisoner existingPrisoner = getPrisonerById(id);
        updatePrisonerFields(existingPrisoner, updatedPrisoner);
        return prisonerRepository.save(existingPrisoner);
    }

    @Transactional
    public void deletePrisoner(Integer id) {
        Assert.notNull(id, "Prisoner ID cannot be null");
        getPrisonerById(id); // Verify prisoner exists
        prisonerRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<Prisoner> getPrisonersByCellId(Integer cellId) {
        Assert.notNull(cellId, "Cell ID cannot be null");
        return prisonerRepository.findByCellId(cellId);
    }

    @Transactional(readOnly = true)
    public List<Prisoner> getPrisonersByBlockId(Integer blockId) {
        Assert.notNull(blockId, "Block ID cannot be null");
        return prisonerRepository.findByCellBlockId(blockId);
    }

    @Transactional(readOnly = true)
    public List<Prisoner> getPrisonersByAdmissionDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        Assert.notNull(startDate, "Start date cannot be null");
        Assert.notNull(endDate, "End date cannot be null");
        Assert.isTrue(!endDate.isBefore(startDate), "End date must not be before start date");
        return prisonerRepository.findBySentenceStartBetween(startDate, endDate);
    }

    @Transactional(readOnly = true)
    public List<Prisoner> getPrisonersByReleaseDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        Assert.notNull(startDate, "Start date cannot be null");
        Assert.notNull(endDate, "End date cannot be null");
        Assert.isTrue(!endDate.isBefore(startDate), "End date must not be before start date");
        return prisonerRepository.findBySentenceEndBetween(startDate, endDate);
    }

    @Transactional(readOnly = true)
    public List<Prisoner> getPrisonersByBlockAndCell(Integer blockId, Integer cellId) {
        Assert.notNull(blockId, "Block ID cannot be null");
        Assert.notNull(cellId, "Cell ID cannot be null");
        return prisonerRepository.findByCellBlockIdAndCellId(blockId, cellId);
    }

    @Transactional(readOnly = true)
    public List<Prisoner> getPrisonersByPrisonId(Integer prisonId) {
        Assert.notNull(prisonId, "Prison ID cannot be null");
        return prisonerRepository.findByCellBlockPrisonId(prisonId);
    }

    public int getTotalPrisoners(Integer prisonId) {
        return prisonerRepository.countByPrisonId(prisonId);
    }

    private void validatePrisoner(Prisoner prisoner) {
        Assert.hasText(prisoner.getName(), "Prisoner name cannot be null or empty");
        Assert.notNull(prisoner.getDateOfBirth(), "Date of birth cannot be null");
        Assert.notNull(prisoner.getSentenceStart(), "Sentence start date cannot be null");
        Assert.notNull(prisoner.getSentenceEnd(), "Sentence end date cannot be null");
        Assert.hasText(prisoner.getGender(), "Gender cannot be null or empty");
        Assert.notNull(prisoner.getCell(), "Cell cannot be null");
    }

    private void updatePrisonerFields(Prisoner existingPrisoner, Prisoner updatedPrisoner) {
        existingPrisoner.setName(updatedPrisoner.getName());
        existingPrisoner.setDateOfBirth(updatedPrisoner.getDateOfBirth());
        existingPrisoner.setSentenceStart(updatedPrisoner.getSentenceStart());
        existingPrisoner.setSentenceEnd(updatedPrisoner.getSentenceEnd());
        existingPrisoner.setGender(updatedPrisoner.getGender());
        existingPrisoner.setCell(updatedPrisoner.getCell());
    }
}
