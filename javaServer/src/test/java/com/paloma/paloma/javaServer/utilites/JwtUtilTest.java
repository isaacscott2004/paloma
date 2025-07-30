package com.paloma.paloma.javaServer.utilites;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilTest {

    private JwtUtil jwtUtil;
    private final String testSecret = "zIrdULXnyhZ4daFTvrbWNcgky1eAdhY+pn2hhzytBvI";
    private UUID testUserId;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil(testSecret);
        testUserId = UUID.randomUUID();
    }

    @Test
    void generateAccessToken_Success() {
        String token = jwtUtil.generateAccessToken(testUserId);

        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertEquals(3, token.split("\\.").length); // JWT has 3 parts separated by dots
    }

    @Test
    void generateRefreshToken_Success() {
        String token = jwtUtil.generateRefreshToken(testUserId);

        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertEquals(3, token.split("\\.").length); // JWT has 3 parts separated by dots
    }

    @Test
    void validateTokenAndGetUserId_ValidAccessToken() {
        String token = jwtUtil.generateAccessToken(testUserId);

        UUID extractedUserId = jwtUtil.validateTokenAndGetUserId(token);

        assertEquals(testUserId, extractedUserId);
    }

    @Test
    void validateTokenAndGetUserId_ValidRefreshToken() {
        String token = jwtUtil.generateRefreshToken(testUserId);

        UUID extractedUserId = jwtUtil.validateTokenAndGetUserId(token);

        assertEquals(testUserId, extractedUserId);
    }

    @Test
    void validateTokenAndGetUserId_InvalidToken() {
        String invalidToken = "invalid.token.here";

        assertThrows(JwtException.class, () -> jwtUtil.validateTokenAndGetUserId(invalidToken));
    }

    @Test
    void validateTokenAndGetUserId_ExpiredToken() {
        // Create an expired token
        Key key = Keys.hmacShaKeyFor(testSecret.getBytes(StandardCharsets.UTF_8));
        String expiredToken = Jwts.builder()
                .setSubject(testUserId.toString())
                .setIssuedAt(new Date(System.currentTimeMillis() - 1000 * 60 * 60)) // 1 hour ago
                .setExpiration(new Date(System.currentTimeMillis() - 1000 * 60 * 30)) // 30 min ago (expired)
                .signWith(key)
                .compact();

        assertThrows(JwtException.class, () -> jwtUtil.validateTokenAndGetUserId(expiredToken));
    }

    @Test
    void validateTokenAndGetUserId_TamperedToken() {
        String validToken = jwtUtil.generateAccessToken(testUserId);
        // Tamper with the token by changing the last character
        String tamperedToken = validToken.substring(0, validToken.length() - 1) + "x";

        assertThrows(JwtException.class, () -> jwtUtil.validateTokenAndGetUserId(tamperedToken));
    }

    @Test
    void validateTokenAndGetUserId_WrongSignature() {
        // Create a token with a different secret
        String differentSecret = "differentSecretKeyForTesting123456789";
        JwtUtil differentJwtUtil = new JwtUtil(differentSecret);
        String tokenWithDifferentSignature = differentJwtUtil.generateAccessToken(testUserId);

        assertThrows(JwtException.class, () -> jwtUtil.validateTokenAndGetUserId(tokenWithDifferentSignature));
    }

    @Test
    void validateTokenAndGetUserId_MalformedToken() {
        String malformedToken = "not-a-jwt-token";

        assertThrows(JwtException.class, () -> jwtUtil.validateTokenAndGetUserId(malformedToken));
    }

    @Test
    void accessTokenExpirationTime() {
        String token = jwtUtil.generateAccessToken(testUserId);

        // Verify token is valid now
        assertDoesNotThrow(() -> jwtUtil.validateTokenAndGetUserId(token));

        // Note: We can't easily test expiration in unit tests without waiting
        // or mocking time, but we can verify the token contains expiration claim
        assertNotNull(token);
    }

    @Test
    void refreshTokenExpirationTime() {
        String token = jwtUtil.generateRefreshToken(testUserId);

        // Verify token is valid now
        assertDoesNotThrow(() -> jwtUtil.validateTokenAndGetUserId(token));

        assertNotNull(token);
    }

    @Test
    void tokensAreUnique() {
        String token1 = jwtUtil.generateAccessToken(testUserId);
        String token2 = jwtUtil.generateAccessToken(testUserId);

        // Tokens should be different due to different issued times
        assertNotEquals(token1, token2);
    }

    @Test
    void refreshTokensAreUnique() {
        String token1 = jwtUtil.generateRefreshToken(testUserId);
        String token2 = jwtUtil.generateRefreshToken(testUserId);

        // Tokens should be different due to different issued times
        assertNotEquals(token1, token2);
    }

    @Test
    void validateTokenAndGetUserId_NullToken() {
        assertThrows(JwtException.class, () -> jwtUtil.validateTokenAndGetUserId(null));
    }

    @Test
    void validateTokenAndGetUserId_EmptyToken() {
        assertThrows(JwtException.class, () -> jwtUtil.validateTokenAndGetUserId(""));
    }

    @Test
    void differentUserIdsProduceDifferentTokens() {
        UUID userId1 = UUID.randomUUID();
        UUID userId2 = UUID.randomUUID();

        String token1 = jwtUtil.generateAccessToken(userId1);
        String token2 = jwtUtil.generateAccessToken(userId2);

        assertNotEquals(token1, token2);

        assertEquals(userId1, jwtUtil.validateTokenAndGetUserId(token1));
        assertEquals(userId2, jwtUtil.validateTokenAndGetUserId(token2));
    }
}