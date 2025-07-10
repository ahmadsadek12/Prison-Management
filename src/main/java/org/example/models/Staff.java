package org.example.models;

import lombok.Getter;
import lombok.Setter;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.math.BigDecimal;
import java.time.Period;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "staff")
public class Staff {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal salary;

    @Column(nullable = false)
    private String phone;

    @Column(nullable = false)
    private LocalDate dob;

    @Column(nullable = false)
    private String gender;

    @Column(nullable = false)
    private String role;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id", nullable = false)
    private Department department;

    // Supervisor relationships
    @OneToMany(mappedBy = "supervisor", fetch = FetchType.LAZY)
    private Set<StaffSupervision> supervisedStaff = new HashSet<>();

    @OneToMany(mappedBy = "subordinate", fetch = FetchType.LAZY)
    private Set<StaffSupervision> supervisors = new HashSet<>();

    public Staff() {
    }

    public Staff(String name, BigDecimal salary, String phone, LocalDate dob, String gender, String role, Department department) {
        setName(name);
        setSalary(salary);
        setPhone(phone);
        setDob(dob);
        setGender(gender);
        setRole(role);
        setDepartment(department);
    }

    public void setName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Name cannot be null or empty");
        }
        this.name = name.trim();
    }

    public void setSalary(BigDecimal salary) {
        if (salary == null) {
            throw new IllegalArgumentException("Salary cannot be null");
        }
        if (salary.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Salary cannot be negative");
        }
        this.salary = salary;
    }

    public void setPhone(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            throw new IllegalArgumentException("Phone cannot be null or empty");
        }
        this.phone = phone.trim();
    }

    public void setDob(LocalDate dob) {
        if (dob == null) {
            throw new IllegalArgumentException("Date of birth cannot be null");
        }
        if (dob.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Date of birth cannot be in the future");
        }
        this.dob = dob;
    }

    public void setGender(String gender) {
        if (gender == null || gender.trim().isEmpty()) {
            throw new IllegalArgumentException("Gender cannot be null or empty");
        }
        String normalizedGender = gender.trim().toUpperCase();
        if (!isValidGender(normalizedGender)) {
            throw new IllegalArgumentException("Invalid gender. Must be one of: MALE, FEMALE, OTHER");
        }
        this.gender = normalizedGender;
    }

    public void setRole(String role) {
        if (role == null || role.trim().isEmpty()) {
            throw new IllegalArgumentException("Role cannot be null or empty");
        }
        this.role = role.trim();
    }

    public void setDepartment(Department department) {
        if (department == null) {
            throw new IllegalArgumentException("Department cannot be null");
        }
        this.department = department;
    }

    private boolean isValidGender(String gender) {
        return gender.equals("MALE") || gender.equals("FEMALE") || gender.equals("OTHER");
    }

    public int getAge() {
        if (dob == null) return 0;
        return Period.between(dob, LocalDate.now()).getYears();
    }

    public boolean isRetirementEligible() {
        return getAge() >= 65;
    }

    public String getFirstName() {
        String[] parts = name.split(" ", 2);
        return parts[0];
    }

    public String getLastName() {
        String[] parts = name.split(" ", 2);
        return parts.length > 1 ? parts[1] : "";
    }

    // Supervisor methods
    public Staff getSupervisor() {
        return supervisors.stream()
            .map(StaffSupervision::getSupervisor)
            .findFirst()
            .orElse(null);
    }

    public Set<Staff> getSupervisedStaff() {
        return supervisedStaff.stream()
            .map(StaffSupervision::getSubordinate)
            .collect(java.util.stream.Collectors.toSet());
    }

    public boolean hasSupervisor() {
        return !supervisors.isEmpty();
    }

    public boolean isSupervisor() {
        return !supervisedStaff.isEmpty();
    }

    public void addSupervisedStaff(Staff subordinate) {
        if (subordinate == null) {
            throw new IllegalArgumentException("Subordinate cannot be null");
        }
        if (this.equals(subordinate)) {
            throw new IllegalArgumentException("Staff cannot supervise themselves");
        }
        StaffSupervision supervision = new StaffSupervision(this, subordinate);
        supervisedStaff.add(supervision);
    }

    public void removeSupervisedStaff(Staff subordinate) {
        if (subordinate == null) {
            throw new IllegalArgumentException("Subordinate cannot be null");
        }
        supervisedStaff.removeIf(supervision -> supervision.getSubordinate().equals(subordinate));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Staff staff = (Staff) o;
        return id.equals(staff.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
