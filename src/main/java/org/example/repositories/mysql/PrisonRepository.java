// PrisonRepository.java
package org.example.repositories.mysql;

import org.example.models.Prison;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PrisonRepository extends JpaRepository<Prison, Integer> {
    
    // Find prison by name
    Prison findByName(String name);

    // Find prison by name with eagerly fetched blocks
    @Query("SELECT DISTINCT p FROM Prison p LEFT JOIN FETCH p.blocks WHERE p.name = :name")
    Prison findByNameWithBlocks(@Param("name") String name);
}
