package com.paloma.paloma.javaServer.repositories;

import com.paloma.paloma.javaServer.entities.AlertSensitivity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;
@Repository
public interface AlertSensitivityRepository extends JpaRepository<AlertSensitivity, UUID> {
}
