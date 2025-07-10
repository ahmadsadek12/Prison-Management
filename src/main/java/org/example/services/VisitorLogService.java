package org.example.services;

import org.example.models.Visitor;
import org.example.models.Prisoner;
import org.example.models.VisitorLog;
import org.example.repositories.mongodb.VisitorLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Service
public class VisitorLogService {

    private final VisitorLogRepository visitorLogRepository;

    @Autowired
    public VisitorLogService(VisitorLogRepository visitorLogRepository) {
        this.visitorLogRepository = visitorLogRepository;
    }

    public List<VisitorLog> getAllVisitorLogs() {
        return visitorLogRepository.findAll();
    }

    public Optional<VisitorLog> getVisitorLogById(String id) {
        Assert.hasText(id, "Visitor log ID cannot be null or empty");
        return visitorLogRepository.findById(id);
    }

    public VisitorLog createVisitorLog(Visitor visitor, Prisoner prisoner, LocalDate visitDate, 
                                     LocalTime visitTime, String comments) {
        Assert.notNull(visitor, "Visitor cannot be null");
        Assert.notNull(prisoner, "Prisoner cannot be null");
        Assert.notNull(visitDate, "Visit date cannot be null");
        Assert.notNull(visitTime, "Visit time cannot be null");

        // Default duration of 60 minutes if not specified
        Integer duration = 60;
        String relationship = visitor.getRelationship();
        String notes = comments;

        VisitorLog visitorLog = new VisitorLog(prisoner.getId(), visitor.getId(), visitDate, duration, relationship, notes);
        return visitorLogRepository.save(visitorLog);
    }

    public VisitorLog updateVisitorLog(String id, VisitorLog updatedLog) {
        Assert.hasText(id, "Visitor log ID cannot be null or empty");
        Assert.notNull(updatedLog, "Updated visitor log cannot be null");

        VisitorLog existingLog = getVisitorLogById(id)
            .orElseThrow(() -> new RuntimeException("Visitor log not found with ID: " + id));

        updateVisitorLogFields(existingLog, updatedLog);
        return visitorLogRepository.save(existingLog);
    }

    public void deleteVisitorLog(String id) {
        Assert.hasText(id, "Visitor log ID cannot be null or empty");
        getVisitorLogById(id); // Verify exists
        visitorLogRepository.deleteById(id);
    }

    public List<VisitorLog> getVisitorLogsByPrisoner(String prisonerId) {
        Assert.hasText(prisonerId, "Prisoner ID cannot be null or empty");
        return visitorLogRepository.findByPrisonerId(Integer.parseInt(prisonerId));
    }

    public List<VisitorLog> getVisitorLogsByVisitor(String visitorId) {
        Assert.hasText(visitorId, "Visitor ID cannot be null or empty");
        return visitorLogRepository.findByVisitorId(Integer.parseInt(visitorId));
    }

    public List<VisitorLog> getVisitorLogsByDateRange(LocalDateTime start, LocalDateTime end) {
        Assert.notNull(start, "Start date cannot be null");
        Assert.notNull(end, "End date cannot be null");
        Assert.isTrue(!end.isBefore(start), "End date must not be before start date");
        return visitorLogRepository.findByDateBetween(start.toLocalDate(), end.toLocalDate());
    }

    public List<VisitorLog> getVisitorLogsByStatus(String status) {
        Assert.hasText(status, "Status cannot be null or empty");
        return visitorLogRepository.findByStatus(status);
    }

    public List<VisitorLog> getPendingVisitorLogs() {
        return visitorLogRepository.findPendingVisits();
    }

    public List<VisitorLog> getApprovedVisitorLogs() {
        return visitorLogRepository.findApprovedVisits();
    }

    public List<VisitorLog> getVisitorLogsNeedingFollowUp() {
        return visitorLogRepository.findVisitsNeedingFollowUp();
    }

    public List<VisitorLog> getPrisonerVisitsNeedingFollowUp(String prisonerId) {
        Assert.hasText(prisonerId, "Prisoner ID cannot be null or empty");
        return visitorLogRepository.findPrisonerVisitsNeedingFollowUp(Integer.parseInt(prisonerId));
    }

    public List<VisitorLog> getVisitorLogsByRelationship(String relationship) {
        Assert.hasText(relationship, "Relationship cannot be null or empty");
        return visitorLogRepository.findByRelationship(relationship);
    }

    public List<VisitorLog> getVisitorLogsByPrisonerAndRelationship(String prisonerId, String relationship) {
        Assert.hasText(prisonerId, "Prisoner ID cannot be null or empty");
        Assert.hasText(relationship, "Relationship cannot be null or empty");
        return visitorLogRepository.findByPrisonerIdAndRelationship(Integer.parseInt(prisonerId), relationship);
    }

    public VisitorLog approveVisitorLog(String id) {
        VisitorLog visitorLog = getVisitorLogById(id)
            .orElseThrow(() -> new RuntimeException("Visitor log not found with ID: " + id));
        
        if (!visitorLog.isPending()) {
            throw new IllegalStateException("Can only approve pending visitor logs");
        }
        
        visitorLog.approve();
        return visitorLogRepository.save(visitorLog);
    }

    public VisitorLog rejectVisitorLog(String id) {
        VisitorLog visitorLog = getVisitorLogById(id)
            .orElseThrow(() -> new RuntimeException("Visitor log not found with ID: " + id));
        
        if (!visitorLog.isPending()) {
            throw new IllegalStateException("Can only reject pending visitor logs");
        }
        
        visitorLog.reject();
        return visitorLogRepository.save(visitorLog);
    }

    public VisitorLog completeVisitorLog(String id) {
        VisitorLog visitorLog = getVisitorLogById(id)
            .orElseThrow(() -> new RuntimeException("Visitor log not found with ID: " + id));
        
        if (!visitorLog.isApproved()) {
            throw new IllegalStateException("Can only complete approved visitor logs");
        }
        
        visitorLog.complete();
        return visitorLogRepository.save(visitorLog);
    }

    private void updateVisitorLogFields(VisitorLog existingLog, VisitorLog updatedLog) {
        if (updatedLog.getPrisonerId() != null) {
            existingLog.setPrisonerId(updatedLog.getPrisonerId());
        }
        if (updatedLog.getVisitorId() != null) {
            existingLog.setVisitorId(updatedLog.getVisitorId());
        }
        if (updatedLog.getDate() != null) {
            existingLog.setDate(updatedLog.getDate());
        }
        if (updatedLog.getDuration() != null) {
            existingLog.setDuration(updatedLog.getDuration());
        }
        if (updatedLog.getRelationship() != null) {
            existingLog.setRelationship(updatedLog.getRelationship());
        }
        if (updatedLog.getNotes() != null) {
            existingLog.setNotes(updatedLog.getNotes());
        }
        if (updatedLog.getStatus() != null) {
            existingLog.setStatus(updatedLog.getStatus());
        }
    }
}
