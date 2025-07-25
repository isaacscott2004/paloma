package com.paloma.paloma.javaServer.service;

import com.paloma.paloma.javaServer.dto.MedicationDTO;
import com.paloma.paloma.javaServer.entity.Medication;
import com.paloma.paloma.javaServer.entity.User;
import com.paloma.paloma.javaServer.repository.MedicationRepository;
import com.paloma.paloma.javaServer.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class MedicationService {
    
    private final MedicationRepository medicationRepository;
    private final UserRepository userRepository;
    
    public List<MedicationDTO> getAllMedicationsByUser(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        return medicationRepository.findByUser(user)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    public List<MedicationDTO> getActiveMedicationsByUser(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        return medicationRepository.findByUserAndIsActiveTrue(user)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    public Optional<MedicationDTO> getMedicationById(UUID medicationId) {
        return medicationRepository.findById(medicationId)
                .map(this::convertToDTO);
    }
    
    public MedicationDTO createMedication(UUID userId, Medication medication) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        medication.setUser(user);
        medication.setIsActive(true); // New medications are active by default
        
        Medication savedMedication = medicationRepository.save(medication);
        return convertToDTO(savedMedication);
    }
    
    public MedicationDTO updateMedication(UUID medicationId, Medication medicationDetails) {
        Medication medication = medicationRepository.findById(medicationId)
                .orElseThrow(() -> new RuntimeException("Medication not found"));
        
        medication.setName(medicationDetails.getName());
        medication.setDosage(medicationDetails.getDosage());
        medication.setSchedule(medicationDetails.getSchedule());
        medication.setIsActive(medicationDetails.getIsActive());
        
        Medication updatedMedication = medicationRepository.save(medication);
        return convertToDTO(updatedMedication);
    }
    
    public void deleteMedication(UUID medicationId) {
        if (!medicationRepository.existsById(medicationId)) {
            throw new RuntimeException("Medication not found");
        }
        medicationRepository.deleteById(medicationId);
    }
    
    public MedicationDTO deactivateMedication(UUID medicationId) {
        Medication medication = medicationRepository.findById(medicationId)
                .orElseThrow(() -> new RuntimeException("Medication not found"));
        
        medication.setIsActive(false);
        Medication updatedMedication = medicationRepository.save(medication);
        return convertToDTO(updatedMedication);
    }
    
    public MedicationDTO activateMedication(UUID medicationId) {
        Medication medication = medicationRepository.findById(medicationId)
                .orElseThrow(() -> new RuntimeException("Medication not found"));
        
        medication.setIsActive(true);
        Medication updatedMedication = medicationRepository.save(medication);
        return convertToDTO(updatedMedication);
    }
    
    public long getActiveMedicationCount(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        return medicationRepository.countActiveByUser(user);
    }
    
    private MedicationDTO convertToDTO(Medication medication) {
        MedicationDTO dto = new MedicationDTO();
        dto.setId(medication.getId());
        dto.setUserId(medication.getUser().getId());
        dto.setName(medication.getName());
        dto.setDosage(medication.getDosage());
        dto.setSchedule(medication.getSchedule());
        dto.setIsActive(medication.getIsActive());
        dto.setCreatedAt(medication.getCreatedAt());
        return dto;
    }
}