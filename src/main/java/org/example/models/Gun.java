package org.example.models;

import lombok.Getter;
import lombok.Setter;
import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "gun")
public class Gun {

    @Id
    @Column(name = "serial_number", nullable = false)
    private String serialNumber;

    @Column(nullable = false)
    private String type;

    @Column(nullable = false)
    private String name;


    @ManyToMany
    @JoinTable(
        name = "gun_assignment",
        joinColumns = @JoinColumn(name = "gun_id"),
        inverseJoinColumns = @JoinColumn(name = "staff_id")
    )
    private Set<Staff> assignedStaff = new HashSet<>();

    // Default constructor required by Hibernate
    public Gun() {
    }

    public Gun(String serialNumber, String type, String name) {
        setSerialNumber(serialNumber);
        setType(type);
        setName(name);
    }

    public void setSerialNumber(String serialNumber) {
        if (serialNumber == null || serialNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("Serial number cannot be null or empty");
        }
        this.serialNumber = serialNumber.trim();
    }

    public void setType(String type) {
        if (type == null || type.trim().isEmpty()) {
            throw new IllegalArgumentException("Type cannot be null or empty");
        }
        this.type = type.trim();
    }

    public void setName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Name cannot be null or empty");
        }
        this.name = name.trim();
    }

    public int getAssignedStaffCount() {
        return assignedStaff.size();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Gun gun = (Gun) o;
        return serialNumber.equals(gun.serialNumber);
    }

    @Override
    public int hashCode() {
        return serialNumber.hashCode();
    }
}
