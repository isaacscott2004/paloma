package com.paloma.paloma.javaServer.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "daily_checkins")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DailyCheckin {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Column(name = "date", nullable = false)
    private LocalDate date;
    
    @Min(value = 1, message = "Mood score must be at least 1")
    @Max(value = 10, message = "Mood score must be at most 10")
    @Column(name = "mood_score")
    private Integer moodScore;
    
    @Min(value = 1, message = "Energy score must be at least 1")
    @Max(value = 10, message = "Energy score must be at most 10")
    @Column(name = "energy_score")
    private Integer energyScore;
    
    @Min(value = 1, message = "Motivation score must be at least 1")
    @Max(value = 10, message = "Motivation score must be at most 10")
    @Column(name = "motivation_score")
    private Integer motivationScore;
    
    @Min(value = 1, message = "Suicidal score must be at least 1")
    @Max(value = 10, message = "Suicidal score must be at most 10")
    @Column(name = "suicidal_score")
    private Integer suicidalScore;
    
    @Min(value = 1, message = "Overall score must be at least 1")
    @Max(value = 10, message = "Overall score must be at most 10")
    @Column(name = "overall_score")
    private Integer overallScore;
    
    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}