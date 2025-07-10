package org.example.models;

import lombok.Getter;
import lombok.Setter;
import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;
import java.util.Objects;

@Getter
@Setter
@Entity
@Table(name = "equipment")
public class Equipment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private int amount;

    private String description;



    @Transient
    private Set<EquipmentMaintenanceLog> maintenanceLogs = new HashSet<>();

    // Default constructor required by JPA
    public Equipment() {}

    public Equipment(String name, int amount, String description, Room room) {
        setName(name);
        setAmount(amount);
        setDescription(description);
        setRoom(room);
    }

    public void setName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Equipment name cannot be null or empty");
        }
        this.name = name.trim();
    }

    public void setAmount(int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Amount cannot be negative");
        }
        this.amount = amount;
    }

    public void setDescription(String description) {
        if (description == null || description.trim().isEmpty()) {
            throw new IllegalArgumentException("Description cannot be null or empty");
        }
        this.description = description.trim();
    }

    public void setRoom(Room room) {
        if (room == null) {
            throw new IllegalArgumentException("Room cannot be null");
        }
        this.room = room;
    }



    public boolean hasPendingMaintenance() {
        return maintenanceLogs.stream()
                .anyMatch(log -> "PENDING".equals(log.getStatus()));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Equipment equipment = (Equipment) o;
        return Objects.equals(id, equipment.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }


}
