package com.paloma.paloma.javaServer.services;

import com.paloma.paloma.javaServer.entities.RefreshAuth;
import com.paloma.paloma.javaServer.entities.User;
import com.paloma.paloma.javaServer.repositories.RefreshAuthRepository;
import com.paloma.paloma.javaServer.repositories.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class RefreshService {
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




    public void revokeTokens(User user) {
        refreshAuthRepository.deleteByUser(user);
    }

}
