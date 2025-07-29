package com.paloma.paloma.javaServer;

import com.paloma.paloma.javaServer.entities.RefreshAuth;
import com.paloma.paloma.javaServer.entities.User;
import com.paloma.paloma.javaServer.repositories.RefreshAuthRepository;
import com.paloma.paloma.javaServer.services.RefreshService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RefreshServiceTest {

    @Mock
    private RefreshAuthRepository refreshAuthRepository;

    @InjectMocks
    private RefreshService refreshService;

    private User testUser;
    private RefreshAuth testRefreshAuth;
    private String testToken;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(UUID.randomUUID());
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setCreatedAt(LocalDateTime.now());

        testToken = "test-refresh-token";
        testRefreshAuth = new RefreshAuth();
        testRefreshAuth.setId(UUID.randomUUID());
        testRefreshAuth.setUser(testUser);
        testRefreshAuth.setToken(testToken);
        testRefreshAuth.setExpiryDate(LocalDateTime.now().plusDays(7));
    }

    @Test
    void validate_ValidToken_Success() {
        when(refreshAuthRepository.findByToken(testToken))
                .thenReturn(Optional.of(testRefreshAuth));

        Optional<User> result = refreshService.validate(testToken);

        assertTrue(result.isPresent());
        assertEquals(testUser, result.get());
        verify(refreshAuthRepository).findByToken(testToken);
    }

    @Test
    void validate_TokenNotFound() {
        when(refreshAuthRepository.findByToken(anyString()))
                .thenReturn(Optional.empty());

        Optional<User> result = refreshService.validate("non-existent-token");

        assertFalse(result.isPresent());
        verify(refreshAuthRepository).findByToken("non-existent-token");
    }

    @Test
    void validate_ExpiredToken() {
        // Set token as expired
        testRefreshAuth.setExpiryDate(LocalDateTime.now().minusDays(1));
        when(refreshAuthRepository.findByToken(testToken))
                .thenReturn(Optional.of(testRefreshAuth));

        Optional<User> result = refreshService.validate(testToken);

        assertFalse(result.isPresent());
        verify(refreshAuthRepository).findByToken(testToken);
    }

    @Test
    void validate_TokenExpiresInFuture() {
        // Set token to expire in future
        testRefreshAuth.setExpiryDate(LocalDateTime.now().plusHours(1));
        when(refreshAuthRepository.findByToken(testToken))
                .thenReturn(Optional.of(testRefreshAuth));

        Optional<User> result = refreshService.validate(testToken);

        assertTrue(result.isPresent());
        assertEquals(testUser, result.get());
        verify(refreshAuthRepository).findByToken(testToken);
    }

    @Test
    void validate_TokenExpiresExactlyNow() {
        // Set token to expire exactly now (should be expired)
        testRefreshAuth.setExpiryDate(LocalDateTime.now());
        when(refreshAuthRepository.findByToken(testToken))
                .thenReturn(Optional.of(testRefreshAuth));

        Optional<User> result = refreshService.validate(testToken);

        // Since we're checking isAfter(now), a token expiring exactly now should be invalid
        assertFalse(result.isPresent());
        verify(refreshAuthRepository).findByToken(testToken);
    }

    @Test
    void createRefreshToken_Success() {
        RefreshAuth expectedRefreshAuth = new RefreshAuth(
                UUID.randomUUID(),
                testUser,
                "generated-uuid-token",
                LocalDateTime.now().plusDays(7)
        );
        
        when(refreshAuthRepository.save(any(RefreshAuth.class)))
                .thenReturn(expectedRefreshAuth);

        RefreshAuth result = refreshService.createRefreshToken(testUser);

        assertNotNull(result);
        assertEquals(testUser, result.getUser());
        assertNotNull(result.getToken());
        assertTrue(result.getExpiryDate().isAfter(LocalDateTime.now().plusDays(6))); // Should be about 7 days from now
        
        verify(refreshAuthRepository).save(any(RefreshAuth.class));
    }

    @Test
    void createRefreshToken_TokenIsUnique() {
        // Mock multiple calls to save to return different tokens
        when(refreshAuthRepository.save(any(RefreshAuth.class)))
                .thenAnswer(invocation -> {
                    RefreshAuth auth = invocation.getArgument(0);
                    auth.setId(UUID.randomUUID());
                    return auth;
                });

        RefreshAuth token1 = refreshService.createRefreshToken(testUser);
        RefreshAuth token2 = refreshService.createRefreshToken(testUser);

        assertNotEquals(token1.getToken(), token2.getToken());
        verify(refreshAuthRepository, times(2)).save(any(RefreshAuth.class));
    }

    @Test
    void revokeTokens_Success() {
        doNothing().when(refreshAuthRepository).deleteByUser(testUser);

        refreshService.revokeTokens(testUser);

        verify(refreshAuthRepository).deleteByUser(testUser);
    }

    @Test
    void revokeTokens_UserWithNoTokens() {
        // Should not throw exception even if user has no tokens
        doNothing().when(refreshAuthRepository).deleteByUser(testUser);

        assertDoesNotThrow(() -> refreshService.revokeTokens(testUser));
        
        verify(refreshAuthRepository).deleteByUser(testUser);
    }

    @Test
    void revokeTokens_NullUser() {
        doNothing().when(refreshAuthRepository).deleteByUser(null);

        assertDoesNotThrow(() -> refreshService.revokeTokens(null));
        
        verify(refreshAuthRepository).deleteByUser(null);
    }

    @Test
    void validate_NullToken() {
        when(refreshAuthRepository.findByToken(null))
                .thenReturn(Optional.empty());

        Optional<User> result = refreshService.validate(null);

        assertFalse(result.isPresent());
        verify(refreshAuthRepository).findByToken(null);
    }

    @Test
    void validate_EmptyToken() {
        when(refreshAuthRepository.findByToken(""))
                .thenReturn(Optional.empty());

        Optional<User> result = refreshService.validate("");

        assertFalse(result.isPresent());
        verify(refreshAuthRepository).findByToken("");
    }

    @Test
    void validate_DatabaseException() {
        when(refreshAuthRepository.findByToken(anyString()))
                .thenThrow(new RuntimeException("Database connection error"));

        assertThrows(RuntimeException.class, () -> {
            refreshService.validate(testToken);
        });

        verify(refreshAuthRepository).findByToken(testToken);
    }

    @Test
    void createRefreshToken_DatabaseException() {
        when(refreshAuthRepository.save(any(RefreshAuth.class)))
                .thenThrow(new RuntimeException("Database connection error"));

        assertThrows(RuntimeException.class, () -> {
            refreshService.createRefreshToken(testUser);
        });

        verify(refreshAuthRepository).save(any(RefreshAuth.class));
    }

    @Test
    void revokeTokens_DatabaseException() {
        doThrow(new RuntimeException("Database connection error"))
                .when(refreshAuthRepository).deleteByUser(testUser);

        assertThrows(RuntimeException.class, () -> {
            refreshService.revokeTokens(testUser);
        });

        verify(refreshAuthRepository).deleteByUser(testUser);
    }
}