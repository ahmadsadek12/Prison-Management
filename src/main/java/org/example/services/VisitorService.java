// VisitorService.java
package org.example.services;

import org.example.models.Visitor;
import org.example.models.Prisoner;
import org.example.repositories.mysql.VisitorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class VisitorService {

    private final VisitorRepository visitorRepository;

    @Autowired
    public VisitorService(VisitorRepository visitorRepository) {
        this.visitorRepository = visitorRepository;
    }

    @Transactional(readOnly = true)
    public List<Visitor> getAllVisitors() {
        return visitorRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Visitor> getVisitorById(Integer id) {
        Assert.notNull(id, "Visitor ID cannot be null");
        return visitorRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public Optional<Visitor> getVisitorByName(String name) {
        Assert.hasText(name, "Name cannot be null or empty");
        return visitorRepository.findByName(name);
    }

    @Transactional
    public Visitor createVisitor(String name, String relationship) {
        Assert.hasText(name, "Name cannot be null or empty");
        
        // Check if visitor with same name already exists
        if (visitorRepository.findByName(name).isPresent()) {
            throw new IllegalArgumentException("Visitor with name " + name + " already exists");
        }

        Visitor visitor = new Visitor(name, relationship);
        return visitorRepository.save(visitor);
    }

    @Transactional
    public Visitor updateVisitor(Integer id, Visitor updatedVisitor) {
        Assert.notNull(id, "Visitor ID cannot be null");
        Assert.notNull(updatedVisitor, "Updated visitor cannot be null");

        Visitor existingVisitor = getVisitorById(id)
            .orElseThrow(() -> new RuntimeException("Visitor not found with ID: " + id));

        // Check if name is being changed and if new name already exists
        if (!existingVisitor.getName().equals(updatedVisitor.getName()) &&
            visitorRepository.findByName(updatedVisitor.getName()).isPresent()) {
            throw new IllegalArgumentException("Visitor with name " + updatedVisitor.getName() + " already exists");
        }

        updateVisitorFields(existingVisitor, updatedVisitor);
        return visitorRepository.save(existingVisitor);
    }

    @Transactional
    public void deleteVisitor(Integer id) {
        Assert.notNull(id, "Visitor ID cannot be null");
        Visitor visitor = getVisitorById(id)
            .orElseThrow(() -> new RuntimeException("Visitor not found with ID: " + id));
            
        // Remove visitor from all associated prisoners (handle lazy loading)
        try {
        for (Prisoner prisoner : visitor.getPrisoners()) {
            visitor.removePrisoner(prisoner);
            }
        } catch (Exception e) {
            // If lazy loading fails, just proceed with deletion
            // The database constraints will handle the cleanup
        }
        
        visitorRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<Visitor> getVisitorsByRelationship(String relationship) {
        Assert.hasText(relationship, "Relationship cannot be null or empty");
        return visitorRepository.findByRelationship(relationship);
    }

    @Transactional(readOnly = true)
    public List<Visitor> getVisitorsByPrisoner(Integer prisonerId) {
        Assert.notNull(prisonerId, "Prisoner ID cannot be null");
        return visitorRepository.findByPrisoners_Id(prisonerId);
    }

    @Transactional(readOnly = true)
    public List<Visitor> getVisitorsByPrisonerAndRelationship(Integer prisonerId, String relationship) {
        Assert.notNull(prisonerId, "Prisoner ID cannot be null");
        Assert.hasText(relationship, "Relationship cannot be null or empty");
        return visitorRepository.findByPrisoners_IdAndRelationship(prisonerId, relationship);
    }

    @Transactional(readOnly = true)
    public List<Visitor> searchVisitorsByNameAndPrisoner(Integer prisonerId, String nameQuery) {
        Assert.notNull(prisonerId, "Prisoner ID cannot be null");
        Assert.hasText(nameQuery, "Name query cannot be null or empty");
        return visitorRepository.findByPrisoners_IdAndNameContaining(prisonerId, nameQuery);
    }

    @Transactional(readOnly = true)
    public long getVisitorCountByPrisoner(Integer prisonerId) {
        Assert.notNull(prisonerId, "Prisoner ID cannot be null");
        return visitorRepository.countByPrisoners_Id(prisonerId);
    }

    @Transactional(readOnly = true)
    public long getVisitorCountByRelationship(String relationship) {
        Assert.hasText(relationship, "Relationship cannot be null or empty");
        return visitorRepository.countByRelationship(relationship);
    }

    @Transactional(readOnly = true)
    public Map<String, Long> getVisitorCountsByRelationship() {
        List<Object[]> results = visitorRepository.countVisitorsByRelationship();
        return results.stream()
            .collect(Collectors.toMap(
                row -> (String) row[0],
                row -> (Long) row[1]
            ));
    }

    @Transactional
    public void addPrisonerToVisitor(Integer visitorId, Prisoner prisoner) {
        Assert.notNull(visitorId, "Visitor ID cannot be null");
        Assert.notNull(prisoner, "Prisoner cannot be null");

        Visitor visitor = getVisitorById(visitorId)
            .orElseThrow(() -> new RuntimeException("Visitor not found with ID: " + visitorId));

        visitor.addPrisoner(prisoner);
        visitorRepository.save(visitor);
    }

    @Transactional
    public void removePrisonerFromVisitor(Integer visitorId, Prisoner prisoner) {
        Assert.notNull(visitorId, "Visitor ID cannot be null");
        Assert.notNull(prisoner, "Prisoner cannot be null");

        Visitor visitor = getVisitorById(visitorId)
            .orElseThrow(() -> new RuntimeException("Visitor not found with ID: " + visitorId));

        visitor.removePrisoner(prisoner);
        visitorRepository.save(visitor);
    }

    private void updateVisitorFields(Visitor existingVisitor, Visitor updatedVisitor) {
        if (updatedVisitor.getName() != null) {
            existingVisitor.setName(updatedVisitor.getName());
        }
        if (updatedVisitor.getRelationship() != null) {
            existingVisitor.setRelationship(updatedVisitor.getRelationship());
        }
        // Note: Prisoner relationships should be managed through addPrisonerToVisitor and removePrisonerFromVisitor
    }
}
