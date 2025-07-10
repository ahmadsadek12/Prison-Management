package org.example.controllers;

import org.example.models.Prisoner;
import org.example.models.Cell;
import org.example.services.PrisonerService;
import org.example.services.CellService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/prisoners")
@CrossOrigin(origins = "*")
public class PrisonerRestController {

    private final PrisonerService prisonerService;
    private final CellService cellService;

    @Autowired
    public PrisonerRestController(PrisonerService prisonerService, CellService cellService) {
        this.prisonerService = prisonerService;
        this.cellService = cellService;
    }

    @GetMapping
    public ResponseEntity<List<Prisoner>> getAllPrisoners() {
        return ResponseEntity.ok(prisonerService.getAllPrisoners());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Prisoner> getPrisonerById(@PathVariable Integer id) {
        return ResponseEntity.ok(prisonerService.getPrisonerById(id));
    }

    @GetMapping("/cell/{cellId}")
    public ResponseEntity<List<Prisoner>> getPrisonersByCell(@PathVariable Integer cellId) {
        return ResponseEntity.ok(prisonerService.getPrisonersByCellId(cellId));
    }

    @GetMapping("/block/{blockId}")
    public ResponseEntity<List<Prisoner>> getPrisonersByBlock(@PathVariable Integer blockId) {
        return ResponseEntity.ok(prisonerService.getPrisonersByBlockId(blockId));
    }

    @GetMapping("/admission-date-range")
    public ResponseEntity<List<Prisoner>> getPrisonersByAdmissionDateRange(
            @RequestParam LocalDateTime startDate,
            @RequestParam LocalDateTime endDate) {
        return ResponseEntity.ok(prisonerService.getPrisonersByAdmissionDateRange(startDate, endDate));
    }

    @GetMapping("/release-date-range")
    public ResponseEntity<List<Prisoner>> getPrisonersByReleaseDateRange(
            @RequestParam LocalDateTime startDate,
            @RequestParam LocalDateTime endDate) {
        return ResponseEntity.ok(prisonerService.getPrisonersByReleaseDateRange(startDate, endDate));
    }

    @PostMapping
    public ResponseEntity<Prisoner> createPrisoner(@RequestBody PrisonerRequest request) {
        Cell cell = cellService.getCellById(request.getCellId())
            .orElseThrow(() -> new RuntimeException("Cell not found with ID: " + request.getCellId()));
        Prisoner prisoner = new Prisoner(
            request.getName(),
            request.getDateOfBirth(),
            request.getSentenceStart(),
            request.getSentenceEnd(),
            request.getGender(),
            cell
        );
        return ResponseEntity.ok(prisonerService.createPrisoner(prisoner));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Prisoner> updatePrisoner(@PathVariable Integer id, @RequestBody PrisonerRequest request) {
        Cell cell = cellService.getCellById(request.getCellId())
            .orElseThrow(() -> new RuntimeException("Cell not found with ID: " + request.getCellId()));
        Prisoner prisoner = new Prisoner(
            request.getName(),
            request.getDateOfBirth(),
            request.getSentenceStart(),
            request.getSentenceEnd(),
            request.getGender(),
            cell
        );
        return ResponseEntity.ok(prisonerService.updatePrisoner(id, prisoner));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePrisoner(@PathVariable Integer id) {
        prisonerService.deletePrisoner(id);
        return ResponseEntity.ok().build();
    }

    // DTO classes
    private static class PrisonerRequest {
        private String name;
        private LocalDate dateOfBirth;
        private LocalDate sentenceStart;
        private LocalDate sentenceEnd;
        private String gender;
        private Integer cellId;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public LocalDate getDateOfBirth() {
            return dateOfBirth;
        }

        public void setDateOfBirth(LocalDate dateOfBirth) {
            this.dateOfBirth = dateOfBirth;
        }

        public LocalDate getSentenceStart() {
            return sentenceStart;
        }

        public void setSentenceStart(LocalDate sentenceStart) {
            this.sentenceStart = sentenceStart;
        }

        public LocalDate getSentenceEnd() {
            return sentenceEnd;
        }

        public void setSentenceEnd(LocalDate sentenceEnd) {
            this.sentenceEnd = sentenceEnd;
        }

        public String getGender() {
            return gender;
        }

        public void setGender(String gender) {
            this.gender = gender;
        }

        public Integer getCellId() {
            return cellId;
        }

        public void setCellId(Integer cellId) {
            this.cellId = cellId;
        }
    }
} 