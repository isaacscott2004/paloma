package com.paloma.paloma.javaServer.controller;

import com.paloma.paloma.javaServer.dto.MedicationDTO;
import com.paloma.paloma.javaServer.entity.Medication;
import com.paloma.paloma.javaServer.service.MedicationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/medications")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class MedicationController {
    
    private final MedicationService medicationService;
    
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<MedicationDTO>> getAllMedicationsByUser(@PathVariable UUID userId) {
        List<MedicationDTO> medications = medicationService.getAllMedicationsByUser(userId);
        return ResponseEntity.ok(medications);
    }
    
    @GetMapping("/user/{userId}/active")
    public ResponseEntity<List<MedicationDTO>> getActiveMedicationsByUser(@PathVariable UUID userId) {
        List<MedicationDTO> medications = medicationService.getActiveMedicationsByUser(userId);
        return ResponseEntity.ok(medications);
    }
    
    @GetMapping("/{medicationId}")
    public ResponseEntity<MedicationDTO> getMedicationById(@PathVariable UUID medicationId) {
        return medicationService.getMedicationById(medicationId)
                .map(medication -> ResponseEntity.ok(medication))
                .orElse(ResponseEntity.notFound().build());
    }
    
    @PostMapping("/user/{userId}")
    public ResponseEntity<MedicationDTO> createMedication(
            @PathVariable UUID userId,
            @Valid @RequestBody Medication medication) {
        try {
            MedicationDTO createdMedication = medicationService.createMedication(userId, medication);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdMedication);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PutMapping("/{medicationId}")
    public ResponseEntity<MedicationDTO> updateMedication(
            @PathVariable UUID medicationId,
            @Valid @RequestBody Medication medicationDetails) {
        try {
            MedicationDTO updatedMedication = medicationService.updateMedication(medicationId, medicationDetails);
            return ResponseEntity.ok(updatedMedication);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @DeleteMapping("/{medicationId}")
    public ResponseEntity<Void> deleteMedication(@PathVariable UUID medicationId) {
        try {
            medicationService.deleteMedication(medicationId);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @PutMapping("/{medicationId}/deactivate")
    public ResponseEntity<MedicationDTO> deactivateMedication(@PathVariable UUID medicationId) {
        try {
            MedicationDTO updatedMedication = medicationService.deactivateMedication(medicationId);
            return ResponseEntity.ok(updatedMedication);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @PutMapping("/{medicationId}/activate")
    public ResponseEntity<MedicationDTO> activateMedication(@PathVariable UUID medicationId) {
        try {
            MedicationDTO updatedMedication = medicationService.activateMedication(medicationId);
            return ResponseEntity.ok(updatedMedication);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @GetMapping("/user/{userId}/count")
    public ResponseEntity<Long> getActiveMedicationCount(@PathVariable UUID userId) {
        long count = medicationService.getActiveMedicationCount(userId);
        return ResponseEntity.ok(count);
    }
}