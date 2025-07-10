package org.example.services;

import org.example.models.MedicalRecord;
import org.example.repositories.mongodb.MedicalRecordRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class MedicalRecordService {

    @Autowired
    private MedicalRecordRepository medicalRecordRepository;

    @Transactional(readOnly = true)
    public List<MedicalRecord> getAllMedicalRecords() {
        return medicalRecordRepository.findAll();
    }

    @Transactional(readOnly = true)
    public MedicalRecord getMedicalRecordById(String id) {
        Assert.hasText(id, "Medical record ID cannot be null or empty");
        return medicalRecordRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Medical record not found with ID: " + id));
    }

    @Transactional(readOnly = true)
    public List<MedicalRecord> getMedicalRecordsByPrisonerId(String prisonerId) {
        Assert.hasText(prisonerId, "Prisoner ID cannot be null or empty");
        List<MedicalRecord> records = medicalRecordRepository.findByPrisonerIdWithPrisoner(Integer.parseInt(prisonerId));
        System.out.println("Loaded " + records.size() + " medical records for prisoner " + prisonerId);
        for (MedicalRecord record : records) {
            System.out.println("Record ID: " + record.getId() + ", Prisoner: " + 
                (record.getPrisoner() != null ? record.getPrisoner().getName() : "NULL"));
        }
        return records;
    }

    @Transactional(readOnly = true)
    public List<MedicalRecord> getMedicalRecordsByDiagnosis(String diagnosis) {
        Assert.hasText(diagnosis, "Diagnosis cannot be null or empty");
        return medicalRecordRepository.findByDiagnosis(diagnosis);
    }

    @Transactional(readOnly = true)
    public List<MedicalRecord> getMedicalRecordsByDateRange(LocalDateTime start, LocalDateTime end) {
        Assert.notNull(start, "Start date cannot be null");
        Assert.notNull(end, "End date cannot be null");
        Assert.isTrue(!end.isBefore(start), "End date must not be before start date");
        return medicalRecordRepository.findByRecordDateBetween(start, end);
    }

    @Transactional(readOnly = true)
    public List<MedicalRecord> getActiveMedicalRecords() {
        return medicalRecordRepository.findActiveTreatmentRecords();
    }

    @Transactional(readOnly = true)
    public List<MedicalRecord> getMedicalRecordsByTreatment(String treatment) {
        Assert.hasText(treatment, "Treatment cannot be null or empty");
        return medicalRecordRepository.findByTreatment(treatment);
    }

    @Transactional(readOnly = true)
    public List<MedicalRecord> getMedicalRecordsByPrisonerAndDiagnosis(String prisonerId, String diagnosis) {
        Assert.hasText(prisonerId, "Prisoner ID cannot be null or empty");
        Assert.hasText(diagnosis, "Diagnosis cannot be null or empty");
        return medicalRecordRepository.findByPrisoner_IdAndDiagnosis(Integer.parseInt(prisonerId), diagnosis);
    }

    @Transactional(readOnly = true)
    public List<MedicalRecord> getMedicalRecordsByStatus(String status) {
        Assert.hasText(status, "Status cannot be null or empty");
        return medicalRecordRepository.findByStatus(status);
    }

    @Transactional(readOnly = true)
    public List<MedicalRecord> getMedicalRecordsNeedingFollowUp() {
        return medicalRecordRepository.findRecordsNeedingFollowUp();
    }

    @Transactional(readOnly = true)
    public List<MedicalRecord> getPrisonerMedicalRecordsNeedingFollowUp(String prisonerId) {
        Assert.hasText(prisonerId, "Prisoner ID cannot be null or empty");
        return medicalRecordRepository.findPrisonerRecordsNeedingFollowUp(Integer.parseInt(prisonerId));
    }

    @Transactional
    public MedicalRecord createMedicalRecord(MedicalRecord record) {
        Assert.notNull(record, "Medical record cannot be null");
        validateMedicalRecord(record);
        record.setRecordDate(LocalDateTime.now());
        return medicalRecordRepository.save(record);
    }

    @Transactional
    public MedicalRecord updateMedicalRecord(String id, MedicalRecord updatedRecord) {
        Assert.hasText(id, "Medical record ID cannot be null or empty");
        Assert.notNull(updatedRecord, "Updated medical record cannot be null");
        
        // If the updatedRecord already has an ID and it matches, save it directly
        // This preserves the @DBRef references
        if (updatedRecord.getId() != null && updatedRecord.getId().equals(id)) {
            System.out.println("Saving existing record directly to preserve @DBRef: " + id);
            System.out.println("Prisoner reference: " + (updatedRecord.getPrisoner() != null ? updatedRecord.getPrisoner().getName() : "null"));
            return medicalRecordRepository.save(updatedRecord);
        }
        
        // Otherwise, update the existing record from database
        MedicalRecord existingRecord = getMedicalRecordById(id);
        
        // Update fields
        existingRecord.setDiagnosis(updatedRecord.getDiagnosis());
        existingRecord.setTreatment(updatedRecord.getTreatment());
        existingRecord.setDoctorNotes(updatedRecord.getDoctorNotes());
        existingRecord.setStatus(updatedRecord.getStatus());
        
        // Keep the existing prisoner reference (don't change it during updates)
        // The prisoner reference should remain the same for existing records
        
        System.out.println("Updating medical record: " + id);
        System.out.println("New diagnosis: " + updatedRecord.getDiagnosis());
        System.out.println("New status: " + updatedRecord.getStatus());
        
        return medicalRecordRepository.save(existingRecord);
    }

    @Transactional
    public void deleteMedicalRecord(String id) {
        Assert.hasText(id, "Medical record ID cannot be null or empty");
        MedicalRecord record = getMedicalRecordById(id);
        record.removePrisoner();
        medicalRecordRepository.deleteById(id);
    }

    @Transactional
    public void archiveMedicalRecord(String id) {
        Assert.hasText(id, "Medical record ID cannot be null or empty");
        MedicalRecord record = getMedicalRecordById(id);
        record.archive();
        medicalRecordRepository.save(record);
    }

    @Transactional
    public void activateMedicalRecord(String id) {
        Assert.hasText(id, "Medical record ID cannot be null or empty");
        MedicalRecord record = getMedicalRecordById(id);
        record.activate();
        medicalRecordRepository.save(record);
    }

    private void validateMedicalRecord(MedicalRecord record) {
        Assert.hasText(record.getDiagnosis(), "Diagnosis cannot be null or empty");
        Assert.hasText(record.getTreatment(), "Treatment cannot be null or empty");
        Assert.hasText(record.getDoctorNotes(), "Doctor notes cannot be null or empty");
        Assert.notNull(record.getPrisoner(), "Prisoner cannot be null");
        Assert.hasText(record.getStatus(), "Status cannot be null or empty");
    }

    public List<MedicalRecord> getMedicalRecordsByPrisoner(String prisonerId) {
        return medicalRecordRepository.findByPrisoner_Id(Integer.parseInt(prisonerId));
    }

    public MedicalRecord saveMedicalRecord(MedicalRecord medicalRecord) {
        return medicalRecordRepository.save(medicalRecord);
    }
}
