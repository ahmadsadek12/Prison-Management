package org.example.models;

import lombok.Getter;
import lombok.Setter;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Setter
@Getter
@Entity
@Table(name = "prisoner")
public class Prisoner {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String name;

    @Column(name = "dob")
    private LocalDate dateOfBirth;

    @Column(name = "sentence_start")
    private LocalDate sentenceStart;

    @Column(name = "sentence_end")
    private LocalDate sentenceEnd;

    @Column(nullable = false)
    private String gender;

    @ManyToOne
    @JoinColumn(name = "cell_id")
    private Cell cell;

    @OneToMany(mappedBy = "prisoner", fetch = FetchType.LAZY, cascade = {CascadeType.DETACH, CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH})
    private Set<Crime> crimes = new HashSet<>();

    @ManyToMany(mappedBy = "prisoners", fetch = FetchType.LAZY)
    private Set<Visitor> visitors = new HashSet<>();

    @Transient
    private Set<MedicalRecord> medicalRecords = new HashSet<>();

    public Prisoner() {}

    public Prisoner(String name, LocalDate dob, LocalDate sentenceStart, LocalDate sentenceEnd, String gender, Cell cell) {
        setName(name);
        setDob(dob);
        setSentenceStart(sentenceStart);
        setSentenceEnd(sentenceEnd);
        setGender(gender);
        setCell(cell);
    }

    public void setName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Prisoner name cannot be null or empty");
        }
        this.name = name.trim();
    }

    public void setDob(LocalDate dob) {
        if (dob == null) {
            throw new IllegalArgumentException("Date of birth cannot be null");
        }
        if (dob.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Date of birth cannot be in the future");
        }
        this.dateOfBirth = dob;
    }

    public void setSentenceStart(LocalDate sentenceStart) {
        if (sentenceStart == null) {
            throw new IllegalArgumentException("Sentence start date cannot be null");
        }
        if (sentenceStart.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Sentence start date cannot be in the future");
        }
        this.sentenceStart = sentenceStart;
    }

    public void setSentenceEnd(LocalDate sentenceEnd) {
        if (sentenceEnd == null) {
            throw new IllegalArgumentException("Sentence end date cannot be null");
        }
        if (sentenceEnd.isBefore(sentenceStart)) {
            throw new IllegalArgumentException("Sentence end date cannot be before sentence start date");
        }
        this.sentenceEnd = sentenceEnd;
    }

    public void setGender(String gender) {
        if (gender == null || gender.trim().isEmpty()) {
            throw new IllegalArgumentException("Gender cannot be null or empty");
        }
        String normalizedGender = gender.trim().toUpperCase();
        if (!normalizedGender.equals("MALE") && !normalizedGender.equals("FEMALE")) {
            throw new IllegalArgumentException("Gender must be either MALE or FEMALE");
        }
        this.gender = normalizedGender;
    }

    public void setCell(Cell cell) {
        if (cell == null) {
            throw new IllegalArgumentException("Cell cannot be null");
        }
        this.cell = cell;
    }

    // Derived attribute: Age calculation
    public int getAge() {
        if (dateOfBirth == null) return 0;
        return LocalDate.now().getYear() - LocalDate.parse(dateOfBirth.toString()).getYear();
    }

    // Derived attribute: Sentence duration in years
    public int getSentenceDuration() {
        if (sentenceStart == null || sentenceEnd == null) return 0;
        return sentenceEnd.getYear() - sentenceStart.getYear();
    }

    // Derived attribute: Time served in years
    public int getTimeServed() {
        if (sentenceStart == null) return 0;
        return LocalDate.now().getYear() - sentenceStart.getYear();
    }

    // Derived attribute: Time remaining in years
    public int getTimeRemaining() {
        if (sentenceEnd == null) return 0;
        return sentenceEnd.getYear() - LocalDate.now().getYear();
    }

    public void addCrime(Crime crime) {
        if (crime == null) {
            throw new IllegalArgumentException("Cannot add null crime");
        }
        this.crimes.add(crime);
        crime.setPrisoner(this);
    }

    public void removeCrime(Crime crime) {
        if (crime == null) {
            throw new IllegalArgumentException("Cannot remove null crime");
        }
        this.crimes.remove(crime);
        crime.setPrisoner(null);
    }

    public void addVisitor(Visitor visitor) {
        if (visitor == null) {
            throw new IllegalArgumentException("Cannot add null visitor");
        }
        this.visitors.add(visitor);
        visitor.getPrisoners().add(this);
    }

    public void removeVisitor(Visitor visitor) {
        if (visitor == null) {
            throw new IllegalArgumentException("Cannot remove null visitor");
        }
        this.visitors.remove(visitor);
        visitor.getPrisoners().remove(this);
    }

    public void addMedicalRecord(MedicalRecord medicalRecord) {
        if (medicalRecord == null) {
            throw new IllegalArgumentException("Cannot add null medical record");
        }
        this.medicalRecords.add(medicalRecord);
    }

    public void removeMedicalRecord(MedicalRecord medicalRecord) {
        if (medicalRecord == null) {
            throw new IllegalArgumentException("Cannot remove null medical record");
        }
        this.medicalRecords.remove(medicalRecord);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Prisoner prisoner = (Prisoner) o;
        return id != null && id.equals(prisoner.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }
}
