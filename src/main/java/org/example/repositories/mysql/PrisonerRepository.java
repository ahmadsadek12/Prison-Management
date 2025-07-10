package org.example.repositories.mysql;

import org.example.models.Prisoner;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.time.LocalDateTime;

@Repository
public interface PrisonerRepository extends JpaRepository<Prisoner, Integer> {
    
    // Find prisoners by cell ID
    List<Prisoner> findByCellId(Integer cellId);
    
    // Find prisoners by block ID
    List<Prisoner> findByCellBlockId(Integer blockId);
    
    // Find prisoners by block ID and cell ID
    List<Prisoner> findByCellBlockIdAndCellId(Integer blockId, Integer cellId);
    
    // Find prisoners by admission date
    List<Prisoner> findBySentenceStartBefore(LocalDateTime date);
    
    // Find prisoners by admission date after
    List<Prisoner> findBySentenceStartAfter(LocalDateTime date);
    
    // Find prisoners by admission date between
    List<Prisoner> findBySentenceStartBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    // Find prisoners by release date
    List<Prisoner> findBySentenceEndBefore(LocalDateTime date);
    
    // Find prisoners by release date after
    List<Prisoner> findBySentenceEndAfter(LocalDateTime date);
    
    // Find prisoners by release date between
    List<Prisoner> findBySentenceEndBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    // Find prisoners by cell ID and admission date
    List<Prisoner> findByCellIdAndSentenceStartBefore(Integer cellId, LocalDateTime date);
    
    // Find prisoners by cell ID and admission date after
    List<Prisoner> findByCellIdAndSentenceStartAfter(Integer cellId, LocalDateTime date);
    
    // Find prisoners by cell ID and admission date between
    List<Prisoner> findByCellIdAndSentenceStartBetween(
        Integer cellId, LocalDateTime startDate, LocalDateTime endDate);
    
    // Find prisoners by block ID and admission date
    List<Prisoner> findByCellBlockIdAndSentenceStartBefore(Integer blockId, LocalDateTime date);
    
    // Find prisoners by block ID and admission date after
    List<Prisoner> findByCellBlockIdAndSentenceStartAfter(Integer blockId, LocalDateTime date);
    
    // Find prisoners by block ID and admission date between
    List<Prisoner> findByCellBlockIdAndSentenceStartBetween(
        Integer blockId, LocalDateTime startDate, LocalDateTime endDate);
    
    // Find prisoners by cell ID and release date
    List<Prisoner> findByCellIdAndSentenceEndBefore(Integer cellId, LocalDateTime date);
    
    // Find prisoners by cell ID and release date after
    List<Prisoner> findByCellIdAndSentenceEndAfter(Integer cellId, LocalDateTime date);
    
    // Find prisoners by cell ID and release date between
    List<Prisoner> findByCellIdAndSentenceEndBetween(
        Integer cellId, LocalDateTime startDate, LocalDateTime endDate);
    
    // Find prisoners by block ID and release date
    List<Prisoner> findByCellBlockIdAndSentenceEndBefore(Integer blockId, LocalDateTime date);
    
    // Find prisoners by block ID and release date after
    List<Prisoner> findByCellBlockIdAndSentenceEndAfter(Integer blockId, LocalDateTime date);
    
    // Find prisoners by block ID and release date between
    List<Prisoner> findByCellBlockIdAndSentenceEndBetween(
        Integer blockId, LocalDateTime startDate, LocalDateTime endDate);
    
    // Find prisoners by prison ID
    List<Prisoner> findByCellBlockPrisonId(Integer prisonId);

    @Query("SELECT COUNT(p) FROM Prisoner p JOIN p.cell c JOIN c.block b WHERE b.prison.id = :prisonId")
    int countByPrisonId(@Param("prisonId") Integer prisonId);
}
