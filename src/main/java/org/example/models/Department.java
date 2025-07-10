package org.example.models;

import lombok.Getter;
import lombok.Setter;
import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "department")
public class Department {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String type;

    @Column(nullable = false)
    private String status; // ACTIVE, INACTIVE

    @OneToMany(mappedBy = "department", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Contains2> contains2Relations = new HashSet<>();

    @OneToMany(mappedBy = "department", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Expense> expenses = new HashSet<>();

    @Column(name = "prison_id")
    private Integer prisonId;

    public Department() {
        this.status = "ACTIVE";
    }

    public Department(String name) {
        this();
        setName(name);
    }

    public void setName(String type) {
        if (type == null || type.trim().isEmpty()) {
            throw new IllegalArgumentException("Department name cannot be null or empty");
        }
        this.type = type.trim();
    }

    public void setStatus(String status) {
        if (status == null || status.trim().isEmpty()) {
            throw new IllegalArgumentException("Status cannot be null or empty");
        }
        String normalizedStatus = status.trim().toUpperCase();
        if (!isValidStatus(normalizedStatus)) {
            throw new IllegalArgumentException("Invalid status. Must be one of: ACTIVE, INACTIVE");
        }
        this.status = normalizedStatus;
    }

    private boolean isValidStatus(String status) {
        return status.equals("ACTIVE") || status.equals("INACTIVE");
    }

    public void addContains2Relation(Contains2 contains2) {
        if (contains2 == null) {
            throw new IllegalArgumentException("Contains2 relation cannot be null");
        }
        this.contains2Relations.add(contains2);
        // Ensure the relationship is bidirectional
        if (contains2.getDepartment() != this) {
            contains2.setDepartment(this);
        }
    }

    public void removeContains2Relation(Contains2 contains2) {
        if (contains2 == null) {
            throw new IllegalArgumentException("Contains2 relation cannot be null");
        }
        this.contains2Relations.remove(contains2);
        // Ensure the relationship is bidirectional
        if (contains2.getDepartment() == this) {
            contains2.setDepartment(null);
        }
    }

    public void addExpense(Expense expense) {
        if (expense == null) {
            throw new IllegalArgumentException("Expense cannot be null");
        }
        this.expenses.add(expense);
    }

    public void removeExpense(Expense expense) {
        if (expense == null) {
            throw new IllegalArgumentException("Expense cannot be null");
        }
        this.expenses.remove(expense);
    }

    public boolean isActive() {
        return "ACTIVE".equals(status);
    }

    public void deactivate() {
        setStatus("INACTIVE");
    }

    public void activate() {
        setStatus("ACTIVE");
    }

    public int getContains2RelationsCount() {
        return contains2Relations.size();
    }

    public int getExpensesCount() {
        return expenses.size();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Department department = (Department) o;
        return id != null && id.equals(department.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    public String getName() {
        return this.type;
    }
}
