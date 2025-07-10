package org.example.models;

import lombok.Getter;
import lombok.Setter;
import jakarta.persistence.*;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.ArrayList;

@Getter
@Setter
@Entity
@Table(name = "room")
public class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String type;

    @Column
    private String description;

    @ManyToMany(mappedBy = "rooms", fetch = FetchType.EAGER)
    private Set<Block> blocks = new HashSet<>();

    @OneToMany(mappedBy = "room", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Equipment> equipmentList = new HashSet<>();

    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Contains2> contains2Relations = new HashSet<>();

    public Room() {}

    public Room(String type, String description) {
        setType(type);
        setDescription(description);
    }

    public Room(String type, String description, Set<Equipment> equipmentList) {
        setType(type);
        setDescription(description);
        setEquipmentList(equipmentList);
    }

    public void setType(String type) {
        if (type == null || type.trim().isEmpty()) {
            throw new IllegalArgumentException("Room type cannot be null or empty");
        }
        this.type = type.trim();
    }

    public void setDescription(String description) {
        this.description = description != null ? description.trim() : null;
    }

    public void addBlock(Block block) {
        if (block == null) {
            throw new IllegalArgumentException("Block cannot be null");
        }
        this.blocks.add(block);
        block.getRooms().add(this);
    }

    public void removeBlock(Block block) {
        if (block == null) {
            throw new IllegalArgumentException("Block cannot be null");
        }
        if (blocks.remove(block)) {
            block.getRooms().remove(this);
        }
    }

    public void setEquipmentList(Set<Equipment> equipmentList) {
        if (equipmentList == null) {
            throw new IllegalArgumentException("Equipment list cannot be null");
        }
        this.equipmentList = equipmentList;
        equipmentList.forEach(equipment -> equipment.setRoom(this));
    }

    public void addContains2Relation(Contains2 contains2) {
        if (contains2 == null) {
            throw new IllegalArgumentException("Cannot add null contains2 relation");
        }
        this.contains2Relations.add(contains2);
        // Ensure the relationship is bidirectional
        if (contains2.getRoom() != this) {
            contains2.setRoom(this);
        }
    }

    public void removeContains2Relation(Contains2 contains2) {
        if (contains2 == null) {
            throw new IllegalArgumentException("Cannot remove null contains2 relation");
        }
        if (this.contains2Relations.remove(contains2)) {
            // Ensure the relationship is bidirectional
            if (contains2.getRoom() == this) {
                contains2.setRoom(null);
            }
        }
    }

    public void addEquipment(Equipment equipment) {
        if (equipment == null) {
            throw new IllegalArgumentException("Equipment cannot be null");
        }
        if (!equipmentList.contains(equipment)) {
            equipmentList.add(equipment);
            equipment.setRoom(this);
        }
    }

    public void removeEquipment(Equipment equipment) {
        if (equipment == null) {
            throw new IllegalArgumentException("Equipment cannot be null");
        }
        if (equipmentList.remove(equipment)) {
            equipment.setRoom(null);
        }
    }

    public int getEquipmentCount() {
        return equipmentList.size();
    }

    public boolean hasEquipment() {
        return !equipmentList.isEmpty();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Room room = (Room) o;
        return id != null && id.equals(room.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    public Integer getId() {
        return id;
    }
}
