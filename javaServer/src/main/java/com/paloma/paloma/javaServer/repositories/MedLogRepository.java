package com.paloma.paloma.javaServer.repositories;

import com.paloma.paloma.javaServer.entities.MedLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface MedLogRepository extends JpaRepository<MedLog, UUID> {
}
