package org.example.repositories.mongodb;

import org.example.models.VisitorLog;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface VisitorLogRepository extends MongoRepository<VisitorLog, String> {
    
    // Find all visits for a specific prisoner
    List<VisitorLog> findByPrisonerId(Integer prisonerId);
    
    // Find all visits by a specific visitor
    List<VisitorLog> findByVisitorId(Integer visitorId);
    
    // Find all visits between two dates
    List<VisitorLog> findByDateBetween(java.time.LocalDate start, java.time.LocalDate end);
    
    // Find all visits with a specific status
    List<VisitorLog> findByStatus(String status);
    
    // Find all visits for a specific prisoner on a specific date
    List<VisitorLog> findByPrisonerIdAndDate(Integer prisonerId, java.time.LocalDate date);
    
    // Find all visits by a specific visitor to a specific prisoner
    List<VisitorLog> findByVisitorIdAndPrisonerId(Integer visitorId, Integer prisonerId);
    
    // Find all approved visits
    @Query("{ 'status': 'APPROVED' }")
    List<VisitorLog> findApprovedVisits();
    
    // Find all pending visits
    @Query("{ 'status': 'PENDING' }")
    List<VisitorLog> findPendingVisits();
    
    // Find all visits that need follow-up
    @Query("{ 'needsFollowUp': true }")
    List<VisitorLog> findVisitsNeedingFollowUp();
    
    // Find all visits for a specific prisoner that need follow-up
    @Query("{ 'prisonerId': ?0, 'needsFollowUp': true }")
    List<VisitorLog> findPrisonerVisitsNeedingFollowUp(Integer prisonerId);
    
    // Find all visits by relationship type
    List<VisitorLog> findByRelationship(String relationship);
    
    // Find all visits for a specific prisoner by relationship type
    List<VisitorLog> findByPrisonerIdAndRelationship(Integer prisonerId, String relationship);
}
