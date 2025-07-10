package org.example.repositories.mysql;

import org.example.models.Block;
import org.example.models.Cell;
import org.example.models.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface BlockRepository extends JpaRepository<Block, Integer> {
    
    // Find all blocks by type
    List<Block> findByType(String type);
    
    // Find all blocks by prison ID
    @Query("SELECT b FROM Block b WHERE b.prison.id = :prisonId")
    List<Block> findByPrisonId(@Param("prisonId") Integer prisonId);
    
    // Find all blocks by prison ID and type
    List<Block> findByPrisonIdAndType(Integer prisonId, String type);
    
    // Find all blocks by department ID through Contains2
    @Query("SELECT DISTINCT b FROM Block b JOIN b.contains2 c WHERE c.department.id = :departmentId")
    List<Block> findByDepartmentId(@Param("departmentId") Integer departmentId);
    
    // Find all blocks by prison ID and department ID through Contains2
    @Query("SELECT DISTINCT b FROM Block b JOIN b.contains2 c WHERE b.prison.id = :prisonId AND c.department.id = :departmentId")
    List<Block> findByPrisonIdAndDepartmentId(@Param("prisonId") Integer prisonId, @Param("departmentId") Integer departmentId);
    
    // Find all blocks by prison ID and department ID and type through Contains2
    @Query("SELECT DISTINCT b FROM Block b JOIN b.contains2 c WHERE b.prison.id = :prisonId AND c.department.id = :departmentId AND b.type = :type")
    List<Block> findByPrisonIdAndDepartmentIdAndType(@Param("prisonId") Integer prisonId, @Param("departmentId") Integer departmentId, @Param("type") String type);

    @Query("SELECT COUNT(c) FROM Block b JOIN b.cells c WHERE b.prison.id = :prisonId AND (SELECT COUNT(p) FROM c.prisoners p) < c.capacity")
    int countAvailableCellsByPrisonId(@Param("prisonId") Integer prisonId);

    @Query("SELECT DISTINCT b.type FROM Block b WHERE b.prison.id = :prisonId")
    List<String> findDistinctTypesByPrisonId(@Param("prisonId") Integer prisonId);

    @Query("SELECT DISTINCT b FROM Block b LEFT JOIN FETCH b.cells WHERE b.prison.id = :prisonId")
    List<Block> findByPrisonIdWithCells(@Param("prisonId") Integer prisonId);

    @Query("SELECT DISTINCT b FROM Block b LEFT JOIN FETCH b.cells")
    List<Block> findAllWithCells();

    @Query("SELECT DISTINCT c FROM Cell c LEFT JOIN FETCH c.prisoners p WHERE c.block.id IN :blockIds")
    List<Cell> findCellsWithPrisonersByBlockIds(@Param("blockIds") List<Integer> blockIds);

    @Query("SELECT DISTINCT b FROM Block b LEFT JOIN FETCH b.contains2 c LEFT JOIN FETCH c.department d LEFT JOIN FETCH d.expenses WHERE b.prison.id = :prisonId")
    List<Block> findByPrisonIdWithRelations(@Param("prisonId") Integer prisonId);

    @Query("SELECT DISTINCT b FROM Block b " +
           "LEFT JOIN FETCH b.cells " +
           "LEFT JOIN FETCH b.contains2 c2 " +
           "LEFT JOIN FETCH c2.department d " +
           "LEFT JOIN FETCH d.expenses e " +
           "WHERE b.id = :blockId")
    Block findByIdWithRelations(@Param("blockId") Integer blockId);

    @Query("SELECT DISTINCT c FROM Cell c " +
           "LEFT JOIN FETCH c.prisoners " +
           "WHERE c.block.id = :blockId")
    List<Cell> findCellsWithPrisonersByBlockId(@Param("blockId") Integer blockId);

    @Query("SELECT DISTINCT r FROM Room r " +
           "LEFT JOIN FETCH r.contains2Relations c2 " +
           "LEFT JOIN FETCH c2.department " +
           "WHERE c2.block.id = :blockId")
    List<Room> findRoomsWithDepartmentsByBlockId(@Param("blockId") Integer blockId);
} 