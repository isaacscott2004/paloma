package com.paloma.paloma.javaServer.repositories;

import com.paloma.paloma.javaServer.entities.TrustedContact;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface TrustedContactRepository extends JpaRepository<TrustedContact, UUID> {

    Optional<TrustedContact> findByUserIdAndContactUserId(UUID userId, UUID contactUserId);

}
