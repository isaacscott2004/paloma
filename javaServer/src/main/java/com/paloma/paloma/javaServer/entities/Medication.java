package com.paloma.paloma.javaServer.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.List;


@Entity
@Table(name = "medications")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Medication {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @NotBlank(message = "Medication name is required")
    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "dosage", nullable = false)
    private String dosage;

    @Column(name = "daily_schedule", nullable = false)
    private String dailySchedule;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    //Relationships
    @OneToMany(mappedBy = "medication", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<MedLog> medLogs;

    public void addMedLog(MedLog medLog) {
        medLogs.add(medLog);
        medLog.setMedication(this); // keeps the relationship consistent
    }

    public void removeMedLog(MedLog medLog) {
        medLogs.remove(medLog);
        medLog.setMedication(null);
    }

}
