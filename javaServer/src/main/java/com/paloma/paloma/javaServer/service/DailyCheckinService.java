package com.paloma.paloma.javaServer.service;

import com.paloma.paloma.javaServer.dto.DailyCheckinDTO;
import com.paloma.paloma.javaServer.entity.DailyCheckin;
import com.paloma.paloma.javaServer.entity.User;
import com.paloma.paloma.javaServer.repository.DailyCheckinRepository;
import com.paloma.paloma.javaServer.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class DailyCheckinService {
    
    private final DailyCheckinRepository dailyCheckinRepository;
    private final UserRepository userRepository;
    
    public List<DailyCheckinDTO> getAllCheckinsByUser(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        return dailyCheckinRepository.findByUserOrderByDateDesc(user)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    public Optional<DailyCheckinDTO> getCheckinByUserAndDate(UUID userId, LocalDate date) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        return dailyCheckinRepository.findByUserAndDate(user, date)
                .map(this::convertToDTO);
    }
    
    public DailyCheckinDTO createCheckin(UUID userId, DailyCheckin checkin) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Check if checkin already exists for this date
        if (dailyCheckinRepository.existsByUserAndDate(user, checkin.getDate())) {
            throw new RuntimeException("Check-in already exists for this date");
        }
        
        checkin.setUser(user);
        DailyCheckin savedCheckin = dailyCheckinRepository.save(checkin);
        return convertToDTO(savedCheckin);
    }
    
    public DailyCheckinDTO updateCheckin(UUID checkinId, DailyCheckin checkinDetails) {
        DailyCheckin checkin = dailyCheckinRepository.findById(checkinId)
                .orElseThrow(() -> new RuntimeException("Check-in not found"));
        
        checkin.setMoodScore(checkinDetails.getMoodScore());
        checkin.setEnergyScore(checkinDetails.getEnergyScore());
        checkin.setMotivationScore(checkinDetails.getMotivationScore());
        checkin.setSuicidalScore(checkinDetails.getSuicidalScore());
        checkin.setOverallScore(checkinDetails.getOverallScore());
        checkin.setNotes(checkinDetails.getNotes());
        
        DailyCheckin updatedCheckin = dailyCheckinRepository.save(checkin);
        return convertToDTO(updatedCheckin);
    }
    
    public void deleteCheckin(UUID checkinId) {
        if (!dailyCheckinRepository.existsById(checkinId)) {
            throw new RuntimeException("Check-in not found");
        }
        dailyCheckinRepository.deleteById(checkinId);
    }
    
    public List<DailyCheckinDTO> getCheckinsByUserAndDateRange(UUID userId, LocalDate startDate, LocalDate endDate) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        return dailyCheckinRepository.findByUserAndDateRange(user, startDate, endDate)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    public Double getAverageOverallScore(UUID userId, LocalDate startDate, LocalDate endDate) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        return dailyCheckinRepository.getAverageOverallScore(user, startDate, endDate);
    }
    
    private DailyCheckinDTO convertToDTO(DailyCheckin checkin) {
        DailyCheckinDTO dto = new DailyCheckinDTO();
        dto.setId(checkin.getId());
        dto.setUserId(checkin.getUser().getId());
        dto.setDate(checkin.getDate());
        dto.setMoodScore(checkin.getMoodScore());
        dto.setEnergyScore(checkin.getEnergyScore());
        dto.setMotivationScore(checkin.getMotivationScore());
        dto.setSuicidalScore(checkin.getSuicidalScore());
        dto.setOverallScore(checkin.getOverallScore());
        dto.setNotes(checkin.getNotes());
        dto.setCreatedAt(checkin.getCreatedAt());
        return dto;
    }
}