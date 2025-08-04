package com.paloma.paloma.javaServer.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entity representing an alert sent to a trusted contact.
 * Alerts are triggered when a user's mental health score indicates they may need support.
 * Each alert is associated with a user and a trusted contact, and includes information
 * about why it was triggered and whether it was successfully sent.
 */
@Entity
@Table(name = "alerts")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Alert {

    /**
     * Unique identifier for the alert.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /**
     * The user who triggered the alert.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * The trusted contact who will receive the alert.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contact_id", nullable = false)
    private TrustedContact contact;

    /**
     * The reason why the alert was triggered.
     */
    @NotBlank(message = "Reason is required")
    @Column(name = "reason", nullable = false)
    private String reason;

    /**
     * The date and time when the alert was triggered.
     */
    @Column(name = "triggered_at", nullable = false)
    private LocalDateTime triggeredAt;

    /**
     * Indicates whether the alert was successfully sent to the trusted contact.
     */
    @Column(name = "was_sent")
    private Boolean wasSent;
}
