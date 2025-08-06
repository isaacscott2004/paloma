package com.paloma.paloma.javaServer.repositories;

import com.paloma.paloma.javaServer.entities.AuthCred;
import com.paloma.paloma.javaServer.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface AuthCredRepository extends JpaRepository<AuthCred, UUID> {
    AuthCred findByUserId(UUID id);

}
