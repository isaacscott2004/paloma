package com.paloma.paloma.javaServer.repositories;

import com.paloma.paloma.javaServer.entities.enums.RoleType;
import com.paloma.paloma.javaServer.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;


@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

Optional<User> findByEmail(String email);

Optional<User> findByUsername(String username);

Optional<User> findByPhone(String phone);

Optional<User> findByEmailOrUsername(String email, String username);

boolean existsByEmail(String email);

boolean existsByUsername(String username);

boolean existsByPhone(String phone);
@Query("SELECT u FROM User u WHERE u.lastLogin < :cutoffDate")
List<User> findInactiveUsers(@Param("cutoffDate") LocalDateTime cutoffDate);

@Query("SELECT u FROM User u WHERE u.lastLogin > :cutoffDate")
List<User> findActiveUsers(@Param("cutoffDate") LocalDateTime cutoffDate);

@Query("SELECT u FROM User u JOIN u.userRoles ur JOIN ur.role r WHERE r.roleType = :roleType")
List<User> findAllUsersByRoleType(@Param("roleType") RoleType roleType);

}
