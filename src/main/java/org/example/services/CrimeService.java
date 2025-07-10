package org.example.services;

import org.example.models.Crime;
import org.example.repositories.mysql.CrimeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class CrimeService {
    @Autowired
    private CrimeRepository crimeRepository;

    public List<Crime> getCrimesByPrisoner(String prisonerId) {
        return crimeRepository.findByPrisonerId(Integer.parseInt(prisonerId));
    }

    public Crime saveCrime(Crime crime) {
        return crimeRepository.save(crime);
    }

    public void deleteCrime(Integer crimeId) {
        crimeRepository.deleteById(crimeId);
    }
} 