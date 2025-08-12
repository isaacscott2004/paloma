package com.paloma.paloma.javaServer.repositories;

import com.paloma.paloma.javaServer.entities.DailyCheckin;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DailyCheckinRepository extends JpaRepository<DailyCheckin, UUID> {
    @Query("SELECT d.totalScore FROM DailyCheckin d WHERE d.user.id = :userId ORDER BY d.date DESC")
    List<Integer> findLatestOverallScoresByUserId(@Param("userId") UUID userId, Pageable pageable);
}
