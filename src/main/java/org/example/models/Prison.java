package org.example.models;

import lombok.Getter;
import lombok.Setter;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "prison")
public class Prison {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String state;

    @Column(nullable = false)
    private String city;

    @Column(nullable = false)
    private String street;

    @Column(nullable = false)
    private String country;


    @OneToMany(mappedBy = "prison", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Block> blocks = new ArrayList<>();

    public Prison() {
    }

    public Prison(String name, String state, String city, String street, String country) {
        setName(name);
        setState(state);
        setCity(city);
        setStreet(street);
        setCountry(country);
    }

    public void setName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Prison name cannot be null or empty");
        }
        this.name = name.trim();
    }

    public void setState(String state) {
        if (state == null || state.trim().isEmpty()) {
            throw new IllegalArgumentException("State cannot be null or empty");
        }
        this.state = state.trim();
    }

    public void setCity(String city) {
        if (city == null || city.trim().isEmpty()) {
            throw new IllegalArgumentException("City cannot be null or empty");
        }
        this.city = city.trim();
    }

    public void setStreet(String street) {
        if (street == null || street.trim().isEmpty()) {
            throw new IllegalArgumentException("Street cannot be null or empty");
        }
        this.street = street.trim();
    }

    public void setCountry(String country) {
        if (country == null || country.trim().isEmpty()) {
            throw new IllegalArgumentException("Country cannot be null or empty");
        }
        this.country = country.trim();
    }

    public void addBlock(Block block) {
        if (block == null) {
            throw new IllegalArgumentException("Cannot add null block");
        }
        if (!blocks.contains(block)) {
            blocks.add(block);
            block.setPrison(this);
        }
    }

    public void removeBlock(Block block) {
        if (block == null) {
            throw new IllegalArgumentException("Cannot remove null block");
        }
        if (blocks.remove(block)) {
            block.setPrison(null);
        }
    }

    public int getNBlocks() {
        return blocks.size();
    }

    public int getCapacity() {
        return blocks.stream()
            .flatMap(block -> block.getCells().stream())
            .mapToInt(cell -> 1)
            .sum();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Prison prison = (Prison) o;
        return id != null && id.equals(prison.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}
