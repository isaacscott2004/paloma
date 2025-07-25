package com.paloma.paloma.javaServer.repository;

import com.paloma.paloma.javaServer.entity.MedLog;
import com.paloma.paloma.javaServer.entity.Medication;
import com.paloma.paloma.javaServer.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MedLogRepository extends JpaRepository<MedLog, UUID> {
    
    List<MedLog> findByUserAndDate(User user, LocalDate date);
    
    Optional<MedLog> findByUserAndMedicationAndDate(User user, Medication medication, LocalDate date);
    
    @Query("SELECT ml FROM MedLog ml WHERE ml.user = :user AND ml.date BETWEEN :startDate AND :endDate")
    List<MedLog> findByUserAndDateRange(@Param("user") User user, 
                                      @Param("startDate") LocalDate startDate, 
                                      @Param("endDate") LocalDate endDate);
    
    @Query("SELECT COUNT(ml) FROM MedLog ml WHERE ml.user = :user AND ml.date = :date AND ml.taken = true")
    long countTakenByUserAndDate(@Param("user") User user, @Param("date") LocalDate date);
    
    @Query("SELECT ml FROM MedLog ml WHERE ml.medication = :medication ORDER BY ml.date DESC")
    List<MedLog> findByMedicationOrderByDateDesc(@Param("medication") Medication medication);
}