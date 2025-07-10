package org.example.models;

import lombok.Getter;
import lombok.Setter;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "cell")
public class Cell {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "type", nullable = false)
    private String type;

    @Column(name = "capacity", nullable = false)
    private Integer capacity;

    @ManyToOne
    @JoinColumn(name = "block_id", nullable = false)
    private Block block;

    @OneToMany(mappedBy = "cell", fetch = FetchType.EAGER)
    private List<Prisoner> prisoners = new ArrayList<>();

    public Cell() {
    }

    public Cell(String type, Integer capacity, Block block) {
        setType(type);
        setCapacity(capacity);
        setBlock(block);
    }

    public void setType(String type) {
        if (type == null || type.trim().isEmpty()) {
            throw new IllegalArgumentException("Cell type cannot be null or empty");
        }
        this.type = type.trim();
    }

    public void setCapacity(Integer capacity) {
        if (capacity == null) {
            throw new IllegalArgumentException("Cell capacity cannot be null");
        }
        if (capacity <= 0) {
            throw new IllegalArgumentException("Cell capacity must be greater than 0");
        }
        
        // Validate solitary cell capacity
        if (isSolitaryCell() && capacity != 1) {
            throw new IllegalArgumentException("Solitary cells can only have a capacity of 1");
        }
        
        this.capacity = capacity;
    }

    private boolean isSolitaryCell() {
        if (type == null) return false;
        String lowerType = type.toLowerCase().trim();
        return lowerType.contains("solitary") || 
               lowerType.contains("isolation") || 
               lowerType.contains("segregation") ||
               lowerType.equals("solitary") ||
               lowerType.equals("isolation") ||
               lowerType.equals("segregation");
    }

    public void setBlock(Block block) {
        this.block = block;
    }

    public void addPrisoner(Prisoner prisoner) {
        if (prisoner == null) {
            throw new IllegalArgumentException("Cannot add null prisoner");
        }
        if (prisoners.size() >= capacity) {
            throw new IllegalStateException("Cell is at maximum capacity");
        }
        if (!prisoners.contains(prisoner)) {
            prisoners.add(prisoner);
            prisoner.setCell(this);
        }
    }

    public void removePrisoner(Prisoner prisoner) {
        if (prisoner == null) {
            throw new IllegalArgumentException("Cannot remove null prisoner");
        }
        if (prisoners.remove(prisoner)) {
            prisoner.setCell(null);
        }
    }

    public int getNPrisoners() {
        return prisoners.size();
    }

    public boolean isAtCapacity() {
        return prisoners.size() >= capacity;
    }

    public int getAvailableSpace() {
        return capacity - prisoners.size();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Cell cell = (Cell) o;
        return id != null && id.equals(cell.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    public Integer getCellId() {
        return id;
    }
}
