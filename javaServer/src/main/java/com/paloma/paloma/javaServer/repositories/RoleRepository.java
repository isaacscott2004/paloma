package com.paloma.paloma.javaServer.repositories;

import com.paloma.paloma.javaServer.entities.Role;
import com.paloma.paloma.javaServer.entities.enums.RoleType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface RoleRepository extends JpaRepository<Role, UUID> {
    Optional<Role> findByRoleType(RoleType roleType);
}
