package org.example.repositories.mysql;

import org.example.models.Visitor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface VisitorRepository extends JpaRepository<Visitor, Integer> {
    
    // Basic queries
    Optional<Visitor> findByName(String name);
    List<Visitor> findByRelationship(String relationship);
    
    // Prisoner-related queries
    List<Visitor> findByPrisoners_Id(Integer prisonerId);
    List<Visitor> findByPrisoners_IdAndRelationship(Integer prisonerId, String relationship);
    
    // Complex queries
    List<Visitor> findByPrisoners_IdAndNameContaining(Integer prisonerId, String name);
    List<Visitor> findByPrisoners_IdAndRelationshipContaining(Integer prisonerId, String relationship);
    
    // Count queries
    long countByPrisoners_Id(Integer prisonerId);
    long countByRelationship(String relationship);
    
    // Custom queries
    @Query("SELECT v FROM Visitor v JOIN v.prisoners p WHERE p.id = :prisonerId AND v.name LIKE %:name%")
    List<Visitor> findVisitorsByPrisonerIdAndNameLike(
        @Param("prisonerId") Integer prisonerId,
        @Param("name") String name
    );
    
    @Query("SELECT v FROM Visitor v JOIN v.prisoners p WHERE p.id = :prisonerId AND v.relationship LIKE %:relationship%")
    List<Visitor> findVisitorsByPrisonerIdAndRelationshipLike(
        @Param("prisonerId") Integer prisonerId,
        @Param("relationship") String relationship
    );
    
    // Statistics queries
    @Query("SELECT v.relationship, COUNT(v) FROM Visitor v GROUP BY v.relationship")
    List<Object[]> countVisitorsByRelationship();
    
    @Query("SELECT COUNT(DISTINCT v) FROM Visitor v JOIN v.prisoners p WHERE p.id = :prisonerId")
    long countUniqueVisitorsForPrisoner(@Param("prisonerId") Integer prisonerId);
}
