package org.example.repositories.mongodb;

import org.example.models.MedicalRecord;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MedicalRecordRepository extends MongoRepository<MedicalRecord, String> {
    
    // Find all medical records for a specific prisoner
    List<MedicalRecord> findByPrisoner_Id(Integer prisonerId);
    
    // Find all medical records for a specific prisoner with prisoner reference loaded
    @Query("{ 'prisoner._id': ?0 }")
    List<MedicalRecord> findByPrisonerIdWithPrisoner(Integer prisonerId);
    
    // Find all medical records with a specific diagnosis
    List<MedicalRecord> findByDiagnosis(String diagnosis);
    
    // Find all medical records between two dates
    List<MedicalRecord> findByRecordDateBetween(LocalDateTime start, LocalDateTime end);
    
    // Find all active treatment records (ongoing treatments)
    @Query("{ 'status': 'ACTIVE' }")
    List<MedicalRecord> findActiveTreatmentRecords();
    
    // Find all medical records by treatment
    List<MedicalRecord> findByTreatment(String treatment);
    
    // Find all medical records for a specific prisoner with a specific diagnosis
    List<MedicalRecord> findByPrisoner_IdAndDiagnosis(Integer prisonerId, String diagnosis);
    
    // Find all medical records with a specific status
    List<MedicalRecord> findByStatus(String status);
    
    // Find all medical records that need follow-up
    @Query("{ 'needsFollowUp': true }")
    List<MedicalRecord> findRecordsNeedingFollowUp();
    
    // Find all medical records for a specific prisoner that need follow-up
    @Query("{ 'prisoner._id': ?0, 'needsFollowUp': true }")
    List<MedicalRecord> findPrisonerRecordsNeedingFollowUp(Integer prisonerId);
}
