package org.example.repositories.mysql;

import org.example.models.Room;
import org.example.models.Equipment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoomRepository extends JpaRepository<Room, Integer> {
    List<Room> findByBlocks_Id(Integer blockId);
    
    @Query("SELECT r FROM Room r LEFT JOIN FETCH r.equipmentList e WHERE r.id = :id")
    Room findByIdWithEquipment(@Param("id") Integer id);
    
    @Query("SELECT r FROM Room r WHERE r.id = :id")
    Room findByIdBasic(@Param("id") Integer id);
    
    @Query("SELECT r FROM Room r LEFT JOIN FETCH r.blocks LEFT JOIN FETCH r.equipmentList e LEFT JOIN FETCH r.contains2Relations c LEFT JOIN FETCH c.department WHERE r.id = :id")
    Room findByIdWithRelations(@Param("id") Integer id);
    
    @Query("SELECT r FROM Room r LEFT JOIN FETCH r.blocks LEFT JOIN FETCH r.contains2Relations c LEFT JOIN FETCH c.department WHERE r.id = :id")
    Room findByIdWithRelationsSafe(@Param("id") Integer id);
    
    @Query("SELECT e FROM Equipment e WHERE e.room.id = :roomId")
    List<Equipment> findValidEquipmentByRoomId(@Param("roomId") Integer roomId);
}
