package com.paloma.paloma.javaServer.repository;

import com.paloma.paloma.javaServer.entity.Medication;
import com.paloma.paloma.javaServer.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface MedicationRepository extends JpaRepository<Medication, UUID> {
    
    List<Medication> findByUserAndIsActiveTrue(User user);
    
    List<Medication> findByUser(User user);
    
    @Query("SELECT m FROM Medication m WHERE m.user = :user AND m.isActive = :isActive")
    List<Medication> findByUserAndActiveStatus(@Param("user") User user, @Param("isActive") boolean isActive);
    
    @Query("SELECT COUNT(m) FROM Medication m WHERE m.user = :user AND m.isActive = true")
    long countActiveByUser(@Param("user") User user);
}