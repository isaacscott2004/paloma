package com.paloma.paloma.javaServer.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "score_history")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScoreHistory {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @NotBlank(message = "Score type is required")
    @Column(name = "score_type", nullable = false)
    private String scoreType;
    
    @Column(name = "value", nullable = false)
    private Integer value;
    
    @Column(name = "date", nullable = false)
    private LocalDate date;
}