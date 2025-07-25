package com.paloma.paloma.javaServer.repository;

import com.paloma.paloma.javaServer.entity.DailyCheckin;
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
public interface DailyCheckinRepository extends JpaRepository<DailyCheckin, UUID> {
    
    Optional<DailyCheckin> findByUserAndDate(User user, LocalDate date);
    
    List<DailyCheckin> findByUserOrderByDateDesc(User user);
    
    @Query("SELECT dc FROM DailyCheckin dc WHERE dc.user = :user AND dc.date BETWEEN :startDate AND :endDate ORDER BY dc.date")
    List<DailyCheckin> findByUserAndDateRange(@Param("user") User user, 
                                            @Param("startDate") LocalDate startDate, 
                                            @Param("endDate") LocalDate endDate);
    
    @Query("SELECT dc FROM DailyCheckin dc WHERE dc.user = :user ORDER BY dc.date DESC")
    List<DailyCheckin> findRecentCheckinsByUser(@Param("user") User user);
    
    @Query("SELECT CASE WHEN COUNT(dc) > 0 THEN true ELSE false END FROM DailyCheckin dc WHERE dc.user = :user AND dc.date = :date")
    boolean existsByUserAndDate(@Param("user") User user, @Param("date") LocalDate date);
    
    @Query("SELECT AVG(dc.overallScore) FROM DailyCheckin dc WHERE dc.user = :user AND dc.date BETWEEN :startDate AND :endDate")
    Double getAverageOverallScore(@Param("user") User user, 
                                @Param("startDate") LocalDate startDate, 
                                @Param("endDate") LocalDate endDate);
}