package com.paloma.paloma.javaServer.repository;

import com.paloma.paloma.javaServer.entity.Alert;
import com.paloma.paloma.javaServer.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface AlertRepository extends JpaRepository<Alert, UUID> {
    
    List<Alert> findByUser(User user);
    
    List<Alert> findByUserAndWasSentFalse(User user);
    
    @Query("SELECT a FROM Alert a WHERE a.triggeredAt BETWEEN :startDateTime AND :endDateTime")
    List<Alert> findByTriggeredAtBetween(@Param("startDateTime") LocalDateTime startDateTime, 
                                       @Param("endDateTime") LocalDateTime endDateTime);
    
    @Query("SELECT a FROM Alert a WHERE a.user = :user ORDER BY a.triggeredAt DESC")
    List<Alert> findByUserOrderByTriggeredAtDesc(@Param("user") User user);
}