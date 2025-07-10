package org.example.models;

import lombok.Getter;
import lombok.Setter;
import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Setter
@Getter
@Entity
@Table(name = "visitor")
public class Visitor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true, length = 255)
    private String name;

    @Column(length = 100)
    private String relationship;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "visitor_prisoner",
        joinColumns = @JoinColumn(name = "visitor_id"),
        inverseJoinColumns = @JoinColumn(name = "prisoner_id")
    )
    private Set<Prisoner> prisoners = new HashSet<>();

    public Visitor() {}

    public Visitor(String name, String relationship) {
        setName(name);
        setRelationship(relationship);
    }

    public void setName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Visitor name cannot be null or empty");
        }
        if (name.length() > 255) {
            throw new IllegalArgumentException("Visitor name cannot exceed 255 characters");
        }
        this.name = name.trim();
    }

    public void setRelationship(String relationship) {
        if (relationship != null && relationship.length() > 100) {
            throw new IllegalArgumentException("Relationship cannot exceed 100 characters");
        }
        this.relationship = relationship != null ? relationship.trim() : null;
    }

    public void addPrisoner(Prisoner prisoner) {
        if (prisoner == null) {
            throw new IllegalArgumentException("Cannot add null prisoner");
        }
        this.prisoners.add(prisoner);
        prisoner.addVisitor(this);
    }

    public void removePrisoner(Prisoner prisoner) {
        if (prisoner == null) {
            throw new IllegalArgumentException("Cannot remove null prisoner");
        }
        this.prisoners.remove(prisoner);
        prisoner.removeVisitor(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Visitor visitor = (Visitor) o;
        return name.equals(visitor.name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }
}
