package com.paloma.paloma.javaServer.repository;

import com.paloma.paloma.javaServer.entity.TrustedContact;
import com.paloma.paloma.javaServer.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TrustedContactRepository extends JpaRepository<TrustedContact, UUID> {
    
    List<TrustedContact> findByUser(User user);
    
    List<TrustedContact> findByContactUser(User contactUser);
    
    boolean existsByUserAndContactUser(User user, User contactUser);
}