package com.paloma.paloma.javaServer.repositories;

import com.paloma.paloma.javaServer.entities.Medication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface MedicationRepository extends JpaRepository<Medication, UUID> {
    Optional<Medication> findByNameAndUserId(String medicationName, UUID userId);
}
