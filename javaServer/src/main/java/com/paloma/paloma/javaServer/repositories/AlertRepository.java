package com.paloma.paloma.javaServer.repositories;

import com.paloma.paloma.javaServer.entities.Alert;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface AlertRepository extends JpaRepository<Alert, UUID> {
}
