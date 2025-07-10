package org.example.models;

import lombok.Getter;
import lombok.Setter;
import jakarta.persistence.*;
import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(name = "expense")
public class Expense {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id", nullable = false)
    private Department department;

    @Column(nullable = false)
    private double amount;

    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    @Column(nullable = false)
    private String status; // PAID, PENDING

    public Expense() {
        // Default constructor for JPA
    }

    public Expense(Department department, double amount, LocalDate dueDate) {
        setDepartment(department);
        setAmount(amount);
        setDueDate(dueDate);
    }

    public void setDepartment(Department department) {
        if (department == null) {
            throw new IllegalArgumentException("Department cannot be null");
        }
        this.department = department;
    }

    public void setAmount(double amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Amount must be greater than 0");
        }
        this.amount = amount;
    }

    public void setDueDate(LocalDate dueDate) {
        if (dueDate == null) {
            throw new IllegalArgumentException("Due date cannot be null");
        }
        if (dueDate.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Due date cannot be in the past");
        }
        this.dueDate = dueDate;
    }

    public void setStatus(String status) {
        if (status == null || status.trim().isEmpty()) {
            throw new IllegalArgumentException("Status cannot be null or empty");
        }
        String normalizedStatus = status.trim().toUpperCase();
        if (!isValidStatus(normalizedStatus)) {
            throw new IllegalArgumentException("Invalid status. Must be one of: PAID, PENDING");
        }
        this.status = normalizedStatus;
    }

    private boolean isValidStatus(String status) {
        return status.equals("PAID") || status.equals("PENDING");
    }

    public void markAsPaid() {
        setStatus("PAID");
    }

    public boolean isPaid() {
        return "PAID".equals(status);
    }

    public boolean isPending() {
        return "PENDING".equals(status);
    }

    public boolean isOverdue() {
        return isPending() && dueDate.isBefore(LocalDate.now());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Expense expense = (Expense) o;
        return id.equals(expense.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
