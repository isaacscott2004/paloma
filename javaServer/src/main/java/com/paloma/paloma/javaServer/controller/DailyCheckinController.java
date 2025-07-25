package com.paloma.paloma.javaServer.controller;

import com.paloma.paloma.javaServer.dto.DailyCheckinDTO;
import com.paloma.paloma.javaServer.entity.DailyCheckin;
import com.paloma.paloma.javaServer.service.DailyCheckinService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/checkins")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class DailyCheckinController {
    
    private final DailyCheckinService dailyCheckinService;
    
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<DailyCheckinDTO>> getAllCheckinsByUser(@PathVariable UUID userId) {
        List<DailyCheckinDTO> checkins = dailyCheckinService.getAllCheckinsByUser(userId);
        return ResponseEntity.ok(checkins);
    }
    
    @GetMapping("/user/{userId}/date/{date}")
    public ResponseEntity<DailyCheckinDTO> getCheckinByUserAndDate(
            @PathVariable UUID userId,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return dailyCheckinService.getCheckinByUserAndDate(userId, date)
                .map(checkin -> ResponseEntity.ok(checkin))
                .orElse(ResponseEntity.notFound().build());
    }
    
    @PostMapping("/user/{userId}")
    public ResponseEntity<DailyCheckinDTO> createCheckin(
            @PathVariable UUID userId,
            @Valid @RequestBody DailyCheckin checkin) {
        try {
            DailyCheckinDTO createdCheckin = dailyCheckinService.createCheckin(userId, checkin);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdCheckin);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PutMapping("/{checkinId}")
    public ResponseEntity<DailyCheckinDTO> updateCheckin(
            @PathVariable UUID checkinId,
            @Valid @RequestBody DailyCheckin checkinDetails) {
        try {
            DailyCheckinDTO updatedCheckin = dailyCheckinService.updateCheckin(checkinId, checkinDetails);
            return ResponseEntity.ok(updatedCheckin);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @DeleteMapping("/{checkinId}")
    public ResponseEntity<Void> deleteCheckin(@PathVariable UUID checkinId) {
        try {
            dailyCheckinService.deleteCheckin(checkinId);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @GetMapping("/user/{userId}/range")
    public ResponseEntity<List<DailyCheckinDTO>> getCheckinsByUserAndDateRange(
            @PathVariable UUID userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<DailyCheckinDTO> checkins = dailyCheckinService.getCheckinsByUserAndDateRange(userId, startDate, endDate);
        return ResponseEntity.ok(checkins);
    }
    
    @GetMapping("/user/{userId}/average")
    public ResponseEntity<Double> getAverageOverallScore(
            @PathVariable UUID userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        Double average = dailyCheckinService.getAverageOverallScore(userId, startDate, endDate);
        return ResponseEntity.ok(average);
    }
    
    @GetMapping("/user/{userId}/today")
    public ResponseEntity<DailyCheckinDTO> getTodayCheckin(@PathVariable UUID userId) {
        return dailyCheckinService.getCheckinByUserAndDate(userId, LocalDate.now())
                .map(checkin -> ResponseEntity.ok(checkin))
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/user/{userId}/week")
    public ResponseEntity<List<DailyCheckinDTO>> getWeeklyCheckins(@PathVariable UUID userId) {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(7);
        List<DailyCheckinDTO> checkins = dailyCheckinService.getCheckinsByUserAndDateRange(userId, startDate, endDate);
        return ResponseEntity.ok(checkins);
    }
    
    @GetMapping("/user/{userId}/month")
    public ResponseEntity<List<DailyCheckinDTO>> getMonthlyCheckins(@PathVariable UUID userId) {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(30);
        List<DailyCheckinDTO> checkins = dailyCheckinService.getCheckinsByUserAndDateRange(userId, startDate, endDate);
        return ResponseEntity.ok(checkins);
    }
}