package org.example.models;

import lombok.Getter;
import lombok.Setter;
import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "block")
public class Block {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String type;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "prison_id", nullable = false)
    private Prison prison;

    @OneToMany(mappedBy = "block", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private Set<Cell> cells = new HashSet<>();

    @ManyToMany
    @JoinTable(
        name = "contains_2",
        joinColumns = @JoinColumn(name = "block_id"),
        inverseJoinColumns = @JoinColumn(name = "room_id")
    )
    private Set<Room> rooms = new HashSet<>();

    @OneToMany(mappedBy = "block", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<Contains2> contains2 = new HashSet<>();

    @Transient
    private Integer numberOfCells;

    @Transient
    private Integer numberOfPrisoners;

    @Transient
    private Double totalExpenses;

    @Transient
    private Boolean medicalDepartment;

    public Block() {
    }

    public Block(String type, Prison prison) {
        setType(type);
        setPrison(prison);
    }

    public void setType(String type) {
        if (type == null || type.trim().isEmpty()) {
            throw new IllegalArgumentException("Block type cannot be null or empty");
        }
        if (type.length() > 50) {
            throw new IllegalArgumentException("Block type cannot exceed 50 characters");
        }
        this.type = type.trim();
    }

    public void setPrison(Prison prison) {
        if (prison == null) {
            throw new IllegalArgumentException("Prison cannot be null");
        }
        this.prison = prison;
    }

    public void addCell(Cell cell) {
        if (cell == null) {
            throw new IllegalArgumentException("Cannot add null cell");
        }
        if (!cells.contains(cell)) {
            cells.add(cell);
            cell.setBlock(this);
        }
    }

    public void removeCell(Cell cell) {
        if (cell == null) {
            throw new IllegalArgumentException("Cannot remove null cell");
        }
        if (cells.remove(cell)) {
            cell.setBlock(null);
        }
    }

    public void addRoom(Room room) {
        if (room == null) {
            throw new IllegalArgumentException("Cannot add null room");
        }
        if (!rooms.contains(room)) {
            rooms.add(room);
            room.getBlocks().add(this);
        }
    }

    public void removeRoom(Room room) {
        if (room == null) {
            throw new IllegalArgumentException("Cannot remove null room");
        }
        if (rooms.remove(room)) {
            room.getBlocks().remove(this);
        }
    }

    public void addContains2(Contains2 contains2) {
        if (contains2 == null) {
            throw new IllegalArgumentException("Cannot add null contains2");
        }
        if (!this.contains2.contains(contains2)) {
            this.contains2.add(contains2);
            contains2.setBlock(this);
        }
    }

    public void removeContains2(Contains2 contains2) {
        if (contains2 == null) {
            throw new IllegalArgumentException("Cannot remove null contains2");
        }
        if (this.contains2.remove(contains2)) {
            contains2.setBlock(null);
        }
    }

    public Integer getNumberOfCells() {
        return cells.size();
    }

    public Integer getNumberOfPrisoners() {
        return cells.stream()
                .mapToInt(cell -> cell.getPrisoners().size())
                .sum();
    }

    public Double getTotalExpenses() {
        return contains2.stream()
                .map(contains2 -> contains2.getDepartment().getExpenses().stream()
                        .mapToDouble(Expense::getAmount)
                        .sum())
                .mapToDouble(Double::doubleValue)
                .sum();
    }

    public Boolean getMedicalDepartment() {
        return contains2.stream()
                .anyMatch(contains2 -> "medical".equalsIgnoreCase(contains2.getDepartment().getType()));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Block block = (Block) o;
        return id != null && id.equals(block.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    public Integer getBlockId() {
        return id;
    }
}
