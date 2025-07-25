package com.paloma.paloma.javaServer.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DailyCheckinDTO {
    private UUID id;
    private UUID userId;
    private LocalDate date;
    private Integer moodScore;
    private Integer energyScore;
    private Integer motivationScore;
    private Integer suicidalScore;
    private Integer overallScore;
    private String notes;
    private LocalDateTime createdAt;
}