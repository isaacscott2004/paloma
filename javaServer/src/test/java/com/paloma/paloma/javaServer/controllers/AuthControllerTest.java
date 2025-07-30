package com.paloma.paloma.javaServer.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.paloma.paloma.javaServer.dataTransferObjects.requests.LoginRequest;
import com.paloma.paloma.javaServer.dataTransferObjects.requests.LogoutRequest;
import com.paloma.paloma.javaServer.dataTransferObjects.requests.RefreshRequest;
import com.paloma.paloma.javaServer.dataTransferObjects.requests.RegisterRequest;
import com.paloma.paloma.javaServer.dataTransferObjects.responses.JwtResponse;
import com.paloma.paloma.javaServer.dataTransferObjects.responses.LoginResponse;
import com.paloma.paloma.javaServer.dataTransferObjects.responses.RegisterResponse;
import com.paloma.paloma.javaServer.entities.User;
import com.paloma.paloma.javaServer.entities.enums.RoleType;
import com.paloma.paloma.javaServer.exceptions.AuthenticationException;
import com.paloma.paloma.javaServer.exceptions.UserException;
import com.paloma.paloma.javaServer.repositories.AuthCredRepository;
import com.paloma.paloma.javaServer.services.RefreshService;
import com.paloma.paloma.javaServer.services.UserService;
import com.paloma.paloma.javaServer.utilites.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

@ExtendWith(MockitoExtension.class)
public class AuthControllerTest {

    @Mock
    private UserService userService;

    @Mock
    private RefreshService refreshService;

    @Mock
    private AuthCredRepository authCredRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthController authController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private User testUser;
    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(authController).build();
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        testUser = new User();
        testUser.setId(UUID.randomUUID());
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPhone("+1234567890");
        testUser.setFullName("Test User");
        testUser.setCreatedAt(LocalDateTime.now());

        registerRequest = new RegisterRequest();
        registerRequest.setUsername("testuser");
        registerRequest.setPassword("password123");
        registerRequest.setEmail("test@example.com");
        registerRequest.setPhone("+1234567890");
        registerRequest.setFullName("Test User");
        registerRequest.setRoleType(RoleType.USER);

        loginRequest = new LoginRequest();
        loginRequest.setEmailOrUsername("testuser");
        loginRequest.setPassword("password123");
    }

    @Test
    void testRegisterSuccess() throws Exception {
        RegisterResponse expectedResponse = new RegisterResponse(testUser, "Registration successful");
        when(userService.register(any(RegisterRequest.class))).thenReturn(expectedResponse);
        when(passwordEncoder.encode(anyString())).thenReturn("hashed-password");

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.user.username").value("testuser"))
                .andExpect(jsonPath("$.message").value("Registration successful"));

        verify(userService).register(any(RegisterRequest.class));
    }

    @Test
    void testRegisterFailure() throws Exception {
        when(userService.register(any(RegisterRequest.class)))
                .thenThrow(new UserException("Username already exists"));

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Username already exists"));

        verify(userService).register(any(RegisterRequest.class));
    }

    @Test
    void testLoginSuccess() throws Exception {
        LoginResponse expectedResponse = new LoginResponse("access-token", "refresh-token");
        when(userService.login(any(LoginRequest.class))).thenReturn(expectedResponse);

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("access-token"));

        verify(userService).login(any(LoginRequest.class));
    }

    @Test
    void testLoginFailure() throws Exception {
        when(userService.login(any(LoginRequest.class)))
                .thenThrow(new AuthenticationException("Invalid credentials"));

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.token").value("Invalid credentials"));

        verify(userService).login(any(LoginRequest.class));
    }

    @Test
    void testLogoutSuccess() throws Exception {
        LogoutRequest logoutRequest = new LogoutRequest();
        logoutRequest.setUser(testUser);

        doNothing().when(refreshService).revokeTokens(testUser);

        mockMvc.perform(post("/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(logoutRequest)))
                .andExpect(status().isOk())
                .andExpect(content().string("Logout successful"));

        verify(refreshService).revokeTokens(testUser);
    }

    @Test
    void testRefreshTokenSuccess() throws Exception {
        RefreshRequest refreshRequest = new RefreshRequest();
        refreshRequest.setRefreshToken("valid-refresh-token");

        when(refreshService.validate("valid-refresh-token")).thenReturn(Optional.of(testUser));
        when(jwtUtil.generateAccessToken(testUser.getId())).thenReturn("new-access-token");

        mockMvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(refreshRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("new-access-token"));

        verify(refreshService).validate("valid-refresh-token");
        verify(jwtUtil).generateAccessToken(testUser.getId());
    }

    @Test
    void testRefreshTokenInvalid() throws Exception {
        RefreshRequest refreshRequest = new RefreshRequest();
        refreshRequest.setRefreshToken("invalid-refresh-token");

        when(refreshService.validate("invalid-refresh-token")).thenReturn(Optional.empty());

        mockMvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(refreshRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.token").value("Invalid refresh token"));

        verify(refreshService).validate("invalid-refresh-token");
        verify(jwtUtil, never()).generateAccessToken(any());
    }

    // Unit tests for direct controller method calls
    @Test
    void testRegisterDirectCall() throws UserException {
        RegisterResponse expectedResponse = new RegisterResponse(testUser, "Registration successful");
        when(userService.register(any(RegisterRequest.class))).thenReturn(expectedResponse);
        when(passwordEncoder.encode(anyString())).thenReturn("hashed-password");

        ResponseEntity<RegisterResponse> response = authController.register(registerRequest);

        assertEquals(201, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(testUser.getUsername(), response.getBody().getUser().getUsername());
        assertEquals("Registration successful", response.getBody().getMessage());
        verify(userService).register(registerRequest);
    }

    @Test
    void testLoginDirectCall() {
        LoginResponse expectedResponse = new LoginResponse("access-token", "refresh-token");
        when(userService.login(any(LoginRequest.class))).thenReturn(expectedResponse);

        ResponseEntity<JwtResponse> response = authController.login(loginRequest);

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals("access-token", response.getBody().getToken());
        verify(userService).login(loginRequest);
    }
}