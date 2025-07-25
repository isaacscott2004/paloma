package com.paloma.paloma.javaServer.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "score_history")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScoreHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "mood_score")
    private Integer moodScore;

    @Column(name = "energy_score")
    private Integer energyScore;

    @Column(name = "motivation_score")
    private Integer motivationScore;

    @Column(name = "suicidal_score")
    private Integer suicidalScore;

    @Column(name = "total_score")
    private Integer totalScore;

    @Column(name = "date", nullable = false)
    private LocalDate date;



}
