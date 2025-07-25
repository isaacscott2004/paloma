package com.paloma.paloma.javaServer.repository;

import com.paloma.paloma.javaServer.entity.ScoreHistory;
import com.paloma.paloma.javaServer.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface ScoreHistoryRepository extends JpaRepository<ScoreHistory, UUID> {
    
    List<ScoreHistory> findByUser(User user);
    
    List<ScoreHistory> findByUserAndScoreType(User user, String scoreType);
    
    @Query("SELECT sh FROM ScoreHistory sh WHERE sh.user = :user AND sh.date BETWEEN :startDate AND :endDate ORDER BY sh.date")
    List<ScoreHistory> findByUserAndDateRange(@Param("user") User user, 
                                            @Param("startDate") LocalDate startDate, 
                                            @Param("endDate") LocalDate endDate);
    
    @Query("SELECT sh FROM ScoreHistory sh WHERE sh.user = :user AND sh.scoreType = :scoreType AND sh.date BETWEEN :startDate AND :endDate ORDER BY sh.date")
    List<ScoreHistory> findByUserAndScoreTypeAndDateRange(@Param("user") User user,
                                                        @Param("scoreType") String scoreType,
                                                        @Param("startDate") LocalDate startDate, 
                                                        @Param("endDate") LocalDate endDate);
}