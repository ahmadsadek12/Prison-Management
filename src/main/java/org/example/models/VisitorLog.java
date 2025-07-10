package org.example.models;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@Document(collection = "visitorLogs")
public class VisitorLog {

    @Id
    private String id;

    private Integer prisonerId;
    private Integer visitorId;
    private LocalDate date;
    private Integer duration; // in minutes
    private String relationship;
    private String notes;
    private String status; // PENDING, APPROVED, REJECTED, COMPLETED
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public VisitorLog() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.status = "PENDING";
    }

    public VisitorLog(Integer prisonerId, Integer visitorId, LocalDate date, Integer duration, String relationship, String notes) {
        this();
        setPrisonerId(prisonerId);
        setVisitorId(visitorId);
        setDate(date);
        setDuration(duration);
        setRelationship(relationship);
        setNotes(notes);
    }

    public void setPrisonerId(Integer prisonerId) {
        if (prisonerId == null) {
            throw new IllegalArgumentException("Prisoner ID cannot be null");
        }
        this.prisonerId = prisonerId;
    }

    public void setVisitorId(Integer visitorId) {
        if (visitorId == null) {
            throw new IllegalArgumentException("Visitor ID cannot be null");
        }
        this.visitorId = visitorId;
    }

    public void setDate(LocalDate date) {
        if (date == null) {
            throw new IllegalArgumentException("Visit date cannot be null");
        }
        // Allow past dates for existing records (editing mode)
        // Only validate future dates for new records
        if (this.id == null && date.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Visit date cannot be in the past for new records");
        }
        this.date = date;
        updateTimestamp();
    }

    public void setDuration(Integer duration) {
        if (duration == null || duration <= 0) {
            throw new IllegalArgumentException("Duration must be positive");
        }
        this.duration = duration;
        updateTimestamp();
    }

    public void setRelationship(String relationship) {
        if (relationship == null || relationship.trim().isEmpty()) {
            throw new IllegalArgumentException("Relationship cannot be null or empty");
        }
        this.relationship = relationship.trim();
        updateTimestamp();
    }

    public void setNotes(String notes) {
        this.notes = notes != null ? notes.trim() : null;
        updateTimestamp();
    }

    public void setStatus(String status) {
        if (status == null || status.trim().isEmpty()) {
            throw new IllegalArgumentException("Status cannot be null or empty");
        }
        String normalizedStatus = status.trim().toUpperCase();
        if (!isValidStatus(normalizedStatus)) {
            throw new IllegalArgumentException("Invalid status. Must be one of: PENDING, APPROVED, REJECTED, COMPLETED");
        }
        this.status = normalizedStatus;
        updateTimestamp();
    }

    private boolean isValidStatus(String status) {
        return status.equals("PENDING") || 
               status.equals("APPROVED") || 
               status.equals("REJECTED") || 
               status.equals("COMPLETED");
    }

    private void updateTimestamp() {
        this.updatedAt = LocalDateTime.now();
    }

    public void approve() {
        setStatus("APPROVED");
    }

    public void reject() {
        setStatus("REJECTED");
    }

    public void complete() {
        setStatus("COMPLETED");
    }

    public boolean isPending() {
        return "PENDING".equals(status);
    }

    public boolean isApproved() {
        return "APPROVED".equals(status);
    }

    public boolean isRejected() {
        return "REJECTED".equals(status);
    }

    public boolean isCompleted() {
        return "COMPLETED".equals(status);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VisitorLog that = (VisitorLog) o;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
