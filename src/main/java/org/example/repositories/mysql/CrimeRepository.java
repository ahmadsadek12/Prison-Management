package org.example.repositories.mysql;

import org.example.models.Crime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CrimeRepository extends JpaRepository<Crime, Integer> {
    @Query("SELECT c FROM Crime c WHERE c.prisoner.id = :prisonerId")
    List<Crime> findByPrisonerId(@Param("prisonerId") Integer prisonerId);
} 