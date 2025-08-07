package com.paloma.paloma.javaServer.services;

import com.paloma.paloma.javaServer.repositories.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for clearing all data from the database.
 * This is intended for testing purposes only.
 */
@Service
@RequiredArgsConstructor
public class DbClearService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final AuthCredRepository authCredRepository;
    private final RefreshAuthRepository refreshAuthRepository;
    private final AlertRepository alertRepository;
    private final DailyCheckinRepository dailyCheckinRepository;
    private final MedLogRepository medLogRepository;
    private final MedicationRepository medicationRepository;
    private final TrustedContactRepository trustedContactRepository;

    /**
     * Clears all data from the database.
     * This method uses @Transactional to ensure that all deletions happen in a single transaction.
     * If any deletion fails, all changes will be rolled back.
     */
    @Transactional
    public void clearAllData() {
        // Clear data in a specific order to avoid foreign key constraint violations
        
        // First, clear entities that depend on other entities (child tables)
        // Delete alerts before trusted contacts since alerts reference trusted contacts
        alertRepository.deleteAll();
        
        // Delete other child entities
        refreshAuthRepository.deleteAll();
        authCredRepository.deleteAll();
        userRoleRepository.deleteAll();
        dailyCheckinRepository.deleteAll();
        trustedContactRepository.deleteAll();
        
        // Then clear parent entities
        userRepository.deleteAll();
        roleRepository.deleteAll();
        
        // The following repositories may not have been fully implemented yet
        // If they are implemented in the future, this method should be updated to include them
        // in the appropriate order based on their dependencies.
        //
        // dailyCheckinRepository
        // medLogRepository
        // medicationRepository
        // scoreHistoryRepository
        
        // Log a message indicating that the database has been cleared
        System.out.println("Database cleared successfully at " + java.time.LocalDateTime.now());
    }
}