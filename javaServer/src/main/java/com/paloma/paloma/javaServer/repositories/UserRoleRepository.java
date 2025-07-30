package com.paloma.paloma.javaServer.repositories;

import com.paloma.paloma.javaServer.entities.Role;
import com.paloma.paloma.javaServer.entities.User;
import com.paloma.paloma.javaServer.entities.UserRole;
import com.paloma.paloma.javaServer.entities.enums.RoleType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRoleRepository extends JpaRepository<UserRole, UUID> {

    @Query("SELECT ur FROM UserRole ur WHERE ur.user = :user AND ur.role.roleType = :roleType")
    Optional<UserRole> findByUserAndRoleType(@Param("user") User user, @Param("roleType") RoleType roleType);

    @Query("SELECT ur FROM UserRole ur WHERE ur.user = :user")
    List<UserRole> findAllByUser(@Param("user") User user);

    @Query("SELECT ur.role.roleType FROM UserRole ur WHERE ur.user = :user")
    List<RoleType> findRoleTypesByUser(@Param("user") User user);

    boolean existsByUserAndRole(User user, Role role);

    void deleteByUserAndRole(User user, Role role);
}
