package com.paloma.paloma.javaServer.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "daily_checkins")
@Data
@NoArgsConstructor
public class DailyCheckin {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "date", nullable = false)
    private LocalDate date;

    @Min(value = 0, message = "Mood score must be greater than or equal to 0")
    @Max(value = 10, message = "Mood score must be less than or equal to 10")
    @Column(name = "mood_score")
    private Integer moodScore;

    @Min(value = 0, message = "Energy score must be greater than or equal to 0")
    @Max(value = 10, message = "Energy score must be less than or equal to 10")
    @Column(name = "energy_score")
    private Integer energyScore;

    @Min(value = 0, message = "Motivation score must be greater than or equal to 0")
    @Max(value = 10, message = "Motivation score must be less than or equal to 10")
    @Column(name = "motivation_score")
    private Integer motivationScore;

    @Min(value = 0, message = "Suicidal score must be greater than or equal to 0")
    @Max(value = 10, message = "Suicidal score must be less than or equal to 10")
    @Column(name = "suicidal_score")
    private Integer suicidalScore;

    @Min(value = 0, message = "Total score must be greater than or equal to 0")
    @Max(value = 40, message = "Total score must be less than or equal to 40")
    @Column(name = "total_score")
    private Integer totalScore;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public DailyCheckin(User user, LocalDate date, Integer moodScore, Integer energyScore, Integer motivationScore,
                        Integer suicidalScore, String notes) {
        this.user = user;
        this.date = date;
        this.moodScore = moodScore;
        this.energyScore = energyScore;
        this.motivationScore = motivationScore;
        this.suicidalScore = suicidalScore;
        this.totalScore = scoreSum(moodScore, energyScore, motivationScore, suicidalScore);
        this.notes = notes;
        this.createdAt = LocalDateTime.now();
    }

    private int scoreSum(Integer ... values){
        int sum = 0;
        for (Integer value : values) {
            if (value != null) {
                sum += value;
            }
        }
        return sum;

    }


}
