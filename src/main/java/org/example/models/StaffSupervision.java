package org.example.models;

import lombok.Getter;
import lombok.Setter;
import jakarta.persistence.*;

@Getter
@Setter
@Entity
@Table(name = "supervises")
public class StaffSupervision {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "supervisor_id", nullable = false)
    private Staff supervisor;

    @ManyToOne
    @JoinColumn(name = "subordinate_id", nullable = false)
    private Staff subordinate;

    public StaffSupervision() {}

    public StaffSupervision(Staff supervisor, Staff subordinate) {
        setSupervisor(supervisor);
        setSubordinate(subordinate);
    }

    public void setSupervisor(Staff supervisor) {
        if (supervisor == null) {
            throw new IllegalArgumentException("Supervisor cannot be null");
        }
        if (supervisor.equals(subordinate)) {
            throw new IllegalArgumentException("Supervisor cannot be the same as subordinate");
        }
        this.supervisor = supervisor;
    }

    public void setSubordinate(Staff subordinate) {
        if (subordinate == null) {
            throw new IllegalArgumentException("Subordinate cannot be null");
        }
        if (subordinate.equals(supervisor)) {
            throw new IllegalArgumentException("Subordinate cannot be the same as supervisor");
        }
        this.subordinate = subordinate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StaffSupervision that = (StaffSupervision) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}
