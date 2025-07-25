package com.paloma.paloma.javaServer.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MedicationDTO {
    private UUID id;
    private UUID userId;
    private String name;
    private String dosage;
    private String schedule;
    private Boolean isActive;
    private LocalDateTime createdAt;
}