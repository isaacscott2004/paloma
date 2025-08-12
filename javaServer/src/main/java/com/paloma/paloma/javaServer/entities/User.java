package com.paloma.paloma.javaServer.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotBlank;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Entity representing a user in the system.
 * Users can be regular users of the application or trusted contacts.
 * This is the central entity that connects to most other entities in the system.
 * Users can have multiple roles, trusted contacts, daily check-ins, medications,
 * medication logs, alerts, and score histories.
 */
@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {

    /**
     * Unique identifier for the user.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /**
     * The user's username, used for login and identification.
     * Must be unique across all users.
     */
    @NotBlank(message = "Username is required")
    @Size(max = 100, message = "Username must be less than 100 characters")
    @Column(unique = true, nullable = false)
    private String username;

    /**
     * The user's email address, used for communication and notifications.
     * Must be unique across all users.
     */
    @NotBlank(message = "Email is required")
    @Size(max = 200, message = "Email must be less than 200 characters")
    @Column(unique = true, nullable = false)
    private String email;

    /**
     * The user's full name.
     */
    @Size(max = 100, message = "Full name must be less than 100 characters")
    @Column(name = "full_name")
    private String fullName;

    /**
     * The date and time when the user account was created.
     * Automatically set when the user is created.
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * The date and time of the user's last login.
     */
    @Column(name = "last_login")
    private LocalDateTime lastLogin;

    // Relationships
    
    /**
     * The roles assigned to this user.
     */
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<UserRole> userRoles;

    /**
     * The trusted contacts that this user has added.
     */
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<TrustedContact> trustedContacts;

    /**
     * The daily check-ins submitted by this user.
     */
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<DailyCheckin> dailyCheckins;

    /**
     * The medications that this user is taking.
     */
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Medication> medications;

    /**
     * The medication logs recorded by this user.
     */
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<MedLog> medLogs;

    /**
     * The alerts triggered by this user.
     */
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Alert> alerts;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    private RefreshAuth refreshAuth;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    private AlertSensitivity alertSensitivity;

}
