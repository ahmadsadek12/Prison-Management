package org.example.models;

import lombok.Getter;
import lombok.Setter;
import jakarta.persistence.*;
import java.io.Serializable;

@Getter
@Setter
@Entity
@Table(name = "contains_2")
@IdClass(Contains2.Contains2Id.class)
public class Contains2 {
    @Id
    @Column(name = "block_id")
    private Integer blockId;

    @Id
    @Column(name = "department_id")
    private Integer departmentId;

    @Id
    @Column(name = "room_id")
    private Integer roomId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "block_id", insertable = false, updatable = false)
    private Block block;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id", insertable = false, updatable = false)
    private Department department;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", insertable = false, updatable = false)
    private Room room;

    public Contains2() {
    }

    public Contains2(Block block, Department department, Room room) {
        this.block = block;
        this.department = department;
        this.room = room;
        this.blockId = block.getId();
        this.departmentId = department.getId();
        this.roomId = room.getId();
    }

    @Getter
    @Setter
    public static class Contains2Id implements Serializable {
        private Integer blockId;
        private Integer departmentId;
        private Integer roomId;

        public Contains2Id() {
        }

        public Contains2Id(Integer blockId, Integer departmentId, Integer roomId) {
            this.blockId = blockId;
            this.departmentId = departmentId;
            this.roomId = roomId;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Contains2Id that = (Contains2Id) o;
            return blockId.equals(that.blockId) &&
                   departmentId.equals(that.departmentId) &&
                   roomId.equals(that.roomId);
        }

        @Override
        public int hashCode() {
            return java.util.Objects.hash(blockId, departmentId, roomId);
        }
    }
}
