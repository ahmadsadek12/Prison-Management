package org.example.models;

import lombok.Getter;
import lombok.Setter;
import jakarta.persistence.*;
import java.util.Objects;
import lombok.Data;
import lombok.NoArgsConstructor;

@Getter
@Setter
@Data
@NoArgsConstructor
@Entity
@Table(name = "gun_assignment")
@IdClass(GunAssignmentId.class)
public class GunAssignment {

    @Id
    @ManyToOne
    @JoinColumn(name = "gun_id", referencedColumnName = "serial_number")
    private Gun gun;

    @Id
    @ManyToOne
    @JoinColumn(name = "staff_id")
    private Staff staff;

    @Column(name = "returned", nullable = false)
    private boolean returned = false;

    public GunAssignment(Gun gun, Staff staff) {
        setGun(gun);
        setStaff(staff);
    }

    public void setGun(Gun gun) {
        if (gun == null) {
            throw new IllegalArgumentException("Gun cannot be null");
        }
        this.gun = gun;
    }

    public void setStaff(Staff staff) {
        if (staff == null) {
            throw new IllegalArgumentException("Staff cannot be null");
        }
        this.staff = staff;
    }

    public void markAsReturned() {
        this.returned = true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GunAssignment that = (GunAssignment) o;
        return gun.equals(that.gun) && staff.equals(that.staff);
    }

    @Override
    public int hashCode() {
        return Objects.hash(gun, staff);
    }
}
