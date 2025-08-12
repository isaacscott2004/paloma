package com.paloma.paloma.javaServer.entities;

import com.paloma.paloma.javaServer.entities.enums.SensitivityLevel;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "alert_sensitivity")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AlertSensitivity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "sensitivity_level")
    private SensitivityLevel sensitivityLevel;




}
