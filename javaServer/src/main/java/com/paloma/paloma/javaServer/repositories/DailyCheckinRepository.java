package com.paloma.paloma.javaServer.repositories;

import com.paloma.paloma.javaServer.entities.Alert;
import com.paloma.paloma.javaServer.entities.DailyCheckin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface DailyCheckinRepository extends JpaRepository<DailyCheckin, UUID> {
}
