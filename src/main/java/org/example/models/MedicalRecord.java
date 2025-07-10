package org.example.models;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DBRef;

import java.time.LocalDateTime;

@Getter
@Setter
@Document(collection = "medicalRecords")
public class MedicalRecord {

    @Id
    private String id;

    private String diagnosis;
    private String treatment;
    private String doctorNotes;
    private LocalDateTime recordDate;
    private String status; // Active, Archived, etc.

    @DBRef
    private Prisoner prisoner;

    public MedicalRecord() {
        this.recordDate = LocalDateTime.now();
        this.status = "Active";
    }

    public MedicalRecord(String diagnosis, String treatment, String doctorNotes, Prisoner prisoner) {
        this();
        setDiagnosis(diagnosis);
        setTreatment(treatment);
        setDoctorNotes(doctorNotes);
        setPrisoner(prisoner);
    }

    public void setDiagnosis(String diagnosis) {
        if (diagnosis == null || diagnosis.trim().isEmpty()) {
            throw new IllegalArgumentException("Diagnosis cannot be null or empty");
        }
        this.diagnosis = diagnosis.trim();
    }

    public void setTreatment(String treatment) {
        if (treatment == null || treatment.trim().isEmpty()) {
            throw new IllegalArgumentException("Treatment cannot be null or empty");
        }
        this.treatment = treatment.trim();
    }

    public void setDoctorNotes(String doctorNotes) {
        if (doctorNotes == null || doctorNotes.trim().isEmpty()) {
            throw new IllegalArgumentException("Doctor notes cannot be null or empty");
        }
        this.doctorNotes = doctorNotes.trim();
    }

    public void setStatus(String status) {
        if (status == null || status.trim().isEmpty()) {
            throw new IllegalArgumentException("Status cannot be null or empty");
        }
        String normalizedStatus = status.trim().toUpperCase();
        // Accept more status values
        if (!normalizedStatus.equals("ACTIVE") && !normalizedStatus.equals("ARCHIVED") 
            && !normalizedStatus.equals("PENDING") && !normalizedStatus.equals("COMPLETED")) {
            throw new IllegalArgumentException("Status must be ACTIVE, ARCHIVED, PENDING, or COMPLETED");
        }
        this.status = normalizedStatus;
    }

    public void setPrisoner(Prisoner prisoner) {
        if (prisoner == null) {
            throw new IllegalArgumentException("Prisoner cannot be null");
        }
        if (this.prisoner != null && !this.prisoner.equals(prisoner)) {
            this.prisoner.removeMedicalRecord(this);
        }
        this.prisoner = prisoner;
        prisoner.addMedicalRecord(this);
    }

    public void removePrisoner() {
        if (this.prisoner != null) {
            this.prisoner.removeMedicalRecord(this);
            this.prisoner = null;
        }
    }

    public boolean isActive() {
        return "ACTIVE".equals(status);
    }

    public boolean isArchived() {
        return "ARCHIVED".equals(status);
    }

    public void archive() {
        setStatus("ARCHIVED");
    }

    public void activate() {
        setStatus("ACTIVE");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MedicalRecord that = (MedicalRecord) o;
        return id != null ? id.equals(that.id) : that.id == null;
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}

