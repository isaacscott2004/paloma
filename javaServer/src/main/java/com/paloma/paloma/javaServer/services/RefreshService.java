package com.paloma.paloma.javaServer.services;

import com.paloma.paloma.javaServer.entities.RefreshAuth;
import com.paloma.paloma.javaServer.entities.User;
import com.paloma.paloma.javaServer.repositories.RefreshAuthRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class RefreshService {

    @Autowired
    private final RefreshAuthRepository refreshAuthRepository;

    public Optional<User> validate(String token) {
        return refreshAuthRepository.findByToken(token)
                .filter(rt -> rt.getExpiryDate().isAfter(LocalDateTime.now()))
                .map(RefreshAuth::getUser);
    }

    public RefreshAuth createRefreshToken(User user) {
        String token = UUID.randomUUID().toString();
        RefreshAuth refreshToken = new RefreshAuth(
                null,
                user,
                token,
                LocalDateTime.now().plusDays(7) // adjust duration as needed
        );
        return refreshAuthRepository.save(refreshToken);
    }

    public void deleteByToken(String token) {
        refreshAuthRepository.findByToken(token)
                .ifPresent(refreshAuthRepository::delete);
    }




    public void revokeTokens(User user) {
        refreshAuthRepository.deleteByUser(user);
    }

}
