package org.example.repositories.mysql;

import org.example.models.Cell;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CellRepository extends JpaRepository<Cell, Integer> {
    
    // Find all cells by type
    List<Cell> findByType(String type);
    
    // Find all cells by block ID
    List<Cell> findByBlockId(Integer blockId);
    
    // Find all cells by block ID and type
    List<Cell> findByBlockIdAndType(Integer blockId, String type);
    
    // Find all cells by prisoner ID using JPQL
    @Query("SELECT c FROM Cell c JOIN c.prisoners p WHERE p.id = :prisonerId")
    List<Cell> findByPrisonerId(@Param("prisonerId") Integer prisonerId);
    
    // Find all cells by block ID and prisoner ID using JPQL
    @Query("SELECT c FROM Cell c JOIN c.prisoners p WHERE c.block.id = :blockId AND p.id = :prisonerId")
    List<Cell> findByBlockIdAndPrisonerId(@Param("blockId") Integer blockId, @Param("prisonerId") Integer prisonerId);
    
    // Find all cells by block ID and type and prisoner ID using JPQL
    @Query("SELECT c FROM Cell c JOIN c.prisoners p WHERE c.block.id = :blockId AND c.type = :type AND p.id = :prisonerId")
    List<Cell> findByBlockIdAndTypeAndPrisonerId(@Param("blockId") Integer blockId, @Param("type") String type, @Param("prisonerId") Integer prisonerId);
} 