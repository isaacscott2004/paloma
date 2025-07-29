package com.paloma.paloma.javaServer.repositories;

import com.paloma.paloma.javaServer.entities.RefreshAuth;
import com.paloma.paloma.javaServer.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface RefreshAuthRepository extends JpaRepository<RefreshAuth, UUID> {
    void deleteByUser(User user);

    Optional<RefreshAuth> findByToken(String token);

}
