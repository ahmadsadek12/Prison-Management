package org.example.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Objects;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GunAssignmentId implements Serializable {
    private String gun;
    private Integer staff;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GunAssignmentId that = (GunAssignmentId) o;
        return Objects.equals(gun, that.gun) && Objects.equals(staff, that.staff);
    }

    @Override
    public int hashCode() {
        return Objects.hash(gun, staff);
    }
} 