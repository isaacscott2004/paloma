package com.paloma.paloma.javaServer.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.paloma.paloma.javaServer.dataTransferObjects.requests.*;
import com.paloma.paloma.javaServer.dataTransferObjects.responses.AddContactResponse;
import com.paloma.paloma.javaServer.dataTransferObjects.responses.JwtResponse;
import com.paloma.paloma.javaServer.entities.RefreshAuth;
import com.paloma.paloma.javaServer.entities.User;
import com.paloma.paloma.javaServer.exceptions.AuthenticationException;
import com.paloma.paloma.javaServer.exceptions.UnauthorizedException;
import com.paloma.paloma.javaServer.exceptions.UserException;
import com.paloma.paloma.javaServer.services.RefreshService;
import com.paloma.paloma.javaServer.services.UserService;
import com.paloma.paloma.javaServer.utilites.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
public class InSessionControllerTest {

    @Mock
    private RefreshService refreshService;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private UserService userService;

    @InjectMocks
    private InSessionController inSessionController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private User testUser;
    private String validToken;
    private UUID testUserId;
    private String authHeader;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(inSessionController).build();
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        testUserId = UUID.randomUUID();
        validToken = "valid-token";
        authHeader = "Bearer " + validToken;

        testUser = new User();
        testUser.setId(testUserId);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPhone("+1234567890");
        testUser.setFullName("Test User");
        testUser.setCreatedAt(LocalDateTime.now());
    }

    @Test
    void testRefreshTokenSuccess() throws Exception {
        RefreshAuth refreshAuth = new RefreshAuth();
        refreshAuth.setToken("refresh-token");
        refreshAuth.setUser(testUser);

        when(jwtUtil.validateTokenAndGetUserId(validToken)).thenReturn(testUserId);
        when(refreshService.findByUserID(testUserId)).thenReturn(Optional.of(refreshAuth));
        when(refreshService.validate("refresh-token")).thenReturn(Optional.of(testUser));
        when(jwtUtil.generateAccessToken(testUserId)).thenReturn("new-access-token");

        mockMvc.perform(post("/insession/refresh")
                        .header("Authorization", authHeader))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("new-access-token")));

        verify(jwtUtil).validateTokenAndGetUserId(validToken);
        verify(refreshService).findByUserID(testUserId);
        verify(refreshService).validate("refresh-token");
    }

    @Test
    void testRefreshTokenInvalidToken() throws Exception {
        when(jwtUtil.validateTokenAndGetUserId(validToken)).thenThrow(new UnauthorizedException("Invalid token"));

        mockMvc.perform(post("/insession/refresh")
                        .header("Authorization", authHeader))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string(containsString("Invalid token")));

        verify(jwtUtil).validateTokenAndGetUserId(validToken);
        verifyNoInteractions(refreshService);
    }

    @Test
    void testLogout() throws Exception {
        when(jwtUtil.validateTokenAndGetUserId(validToken)).thenReturn(testUserId);
        when(userService.getUserById(testUserId)).thenReturn(Optional.of(testUser));
        doNothing().when(refreshService).revokeTokens(testUser);

        mockMvc.perform(delete("/insession/logout")
                        .header("Authorization", authHeader))
                .andExpect(status().isOk())
                .andExpect(content().string("Successfully logged out and revoked all refresh tokens"));

        verify(jwtUtil).validateTokenAndGetUserId(validToken);
        verify(userService).getUserById(testUserId);
        verify(refreshService).revokeTokens(testUser);
    }

    @Test
    void testUpdateEmail() throws Exception {
        UpdateEmailRequest updateEmailRequest = new UpdateEmailRequest("newemail@example.com");

        when(jwtUtil.validateTokenAndGetUserId(validToken)).thenReturn(testUserId);
        when(userService.getUserById(testUserId)).thenReturn(Optional.of(testUser));
        doNothing().when(userService).updateEmail(testUser, "newemail@example.com");

        mockMvc.perform(put("/insession/update/email")
                        .header("Authorization", authHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateEmailRequest)))
                .andExpect(status().isOk())
                .andExpect(content().string("Successfully updated email"));

        verify(jwtUtil).validateTokenAndGetUserId(validToken);
        verify(userService).getUserById(testUserId);
        verify(userService).updateEmail(testUser, "newemail@example.com");
    }

    @Test
    void testUpdatePassword() throws Exception {
        UpdatePasswordRequest updatePasswordRequest = new UpdatePasswordRequest("newpassword", "oldpassword");

        when(jwtUtil.validateTokenAndGetUserId(validToken)).thenReturn(testUserId);
        when(userService.getUserById(testUserId)).thenReturn(Optional.of(testUser));
        doNothing().when(userService).updatePassword(testUser, "oldpassword", "newpassword");

        mockMvc.perform(put("/insession/update/password")
                        .header("Authorization", authHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatePasswordRequest)))
                .andExpect(status().isOk())
                .andExpect(content().string("Successfully updated password"));

        verify(jwtUtil).validateTokenAndGetUserId(validToken);
        verify(userService).getUserById(testUserId);
        verify(userService).updatePassword(testUser, "oldpassword", "newpassword");
    }

    @Test
    void testUpdateUsername() throws Exception {
        UpdateUsernameRequest updateUsernameRequest = new UpdateUsernameRequest("newusername");

        when(jwtUtil.validateTokenAndGetUserId(validToken)).thenReturn(testUserId);
        when(userService.getUserById(testUserId)).thenReturn(Optional.of(testUser));
        doNothing().when(userService).updateUsername(testUser, "newusername");

        mockMvc.perform(put("/insession/update/username")
                        .header("Authorization", authHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateUsernameRequest)))
                .andExpect(status().isOk())
                .andExpect(content().string("Successfully updated username"));

        verify(jwtUtil).validateTokenAndGetUserId(validToken);
        verify(userService).getUserById(testUserId);
        verify(userService).updateUsername(testUser, "newusername");
    }

    @Test
    void testUpdatePhone() throws Exception {
        UpdatePhoneRequest updatePhoneRequest = new UpdatePhoneRequest("+19876543210");

        when(jwtUtil.validateTokenAndGetUserId(validToken)).thenReturn(testUserId);
        when(userService.getUserById(testUserId)).thenReturn(Optional.of(testUser));
        doNothing().when(userService).updatePhone(testUser, "+19876543210");

        mockMvc.perform(put("/insession/update/phone")
                        .header("Authorization", authHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatePhoneRequest)))
                .andExpect(status().isOk())
                .andExpect(content().string("Successfully updated phone"));

        verify(jwtUtil).validateTokenAndGetUserId(validToken);
        verify(userService).getUserById(testUserId);
        verify(userService).updatePhone(testUser, "+19876543210");
    }

    @Test
    void testAddContactSuccess() throws Exception {
        AddTrustedContactRequest addTrustedContactRequest = new AddTrustedContactRequest(
                "contact@example.com", "+19876543210", "Please help me if I need support");
        
        AddContactResponse successResponse = new AddContactResponse(
                true, "Contact added successfully", true);

        when(jwtUtil.validateTokenAndGetUserId(validToken)).thenReturn(testUserId);
        when(userService.getUserById(testUserId)).thenReturn(Optional.of(testUser));
        when(userService.addContact(eq(testUser), eq("contact@example.com"), 
                eq("+19876543210"), eq("Please help me if I need support")))
                .thenReturn(successResponse);

        mockMvc.perform(post("/insession/addContact")
                        .header("Authorization", authHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(addTrustedContactRequest)))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Contact added successfully")))
                .andExpect(content().string(containsString("true")));

        verify(jwtUtil).validateTokenAndGetUserId(validToken);
        verify(userService).getUserById(testUserId);
        verify(userService).addContact(eq(testUser), eq("contact@example.com"), 
                eq("+19876543210"), eq("Please help me if I need support"));
    }

    @Test
    void testAddContactNewUser() throws Exception {
        AddTrustedContactRequest addTrustedContactRequest = new AddTrustedContactRequest(
                "newcontact@example.com", "+19876543210", "Please help me if I need support");
        
        AddContactResponse successResponse = new AddContactResponse(
                true, "Contact added and invitations sent", false, true, true);

        when(jwtUtil.validateTokenAndGetUserId(validToken)).thenReturn(testUserId);
        when(userService.getUserById(testUserId)).thenReturn(Optional.of(testUser));
        when(userService.addContact(eq(testUser), eq("newcontact@example.com"), 
                eq("+19876543210"), eq("Please help me if I need support")))
                .thenReturn(successResponse);

        mockMvc.perform(post("/insession/addContact")
                        .header("Authorization", authHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(addTrustedContactRequest)))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Contact added and invitations sent")))
                .andExpect(content().string(containsString("true")));

        verify(jwtUtil).validateTokenAndGetUserId(validToken);
        verify(userService).getUserById(testUserId);
        verify(userService).addContact(eq(testUser), eq("newcontact@example.com"), 
                eq("+19876543210"), eq("Please help me if I need support"));
    }

    @Test
    void testAddContactFailure() throws Exception {
        AddTrustedContactRequest addTrustedContactRequest = new AddTrustedContactRequest(
                "contact@example.com", "+19876543210", "Please help me if I need support");
        
        AddContactResponse failureResponse = new AddContactResponse(
                false, "Failed to add contact: Database error", false);

        when(jwtUtil.validateTokenAndGetUserId(validToken)).thenReturn(testUserId);
        when(userService.getUserById(testUserId)).thenReturn(Optional.of(testUser));
        when(userService.addContact(eq(testUser), eq("contact@example.com"), 
                eq("+19876543210"), eq("Please help me if I need support")))
                .thenReturn(failureResponse);

        mockMvc.perform(post("/insession/addContact")
                        .header("Authorization", authHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(addTrustedContactRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Failed to add contact: Database error")))
                .andExpect(content().string(containsString("false")));

        verify(jwtUtil).validateTokenAndGetUserId(validToken);
        verify(userService).getUserById(testUserId);
        verify(userService).addContact(eq(testUser), eq("contact@example.com"), 
                eq("+19876543210"), eq("Please help me if I need support"));
    }

    @Test
    void testInvalidToken() throws Exception {
        when(jwtUtil.validateTokenAndGetUserId(validToken)).thenThrow(new UnauthorizedException("Invalid token"));

        mockMvc.perform(put("/insession/update/email")
                        .header("Authorization", authHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UpdateEmailRequest("newemail@example.com"))))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Invalid token"));

        verify(jwtUtil).validateTokenAndGetUserId(validToken);
        verifyNoInteractions(userService);
    }

    @Test
    void testUserNotFound() throws Exception {
        when(jwtUtil.validateTokenAndGetUserId(validToken)).thenReturn(testUserId);
        when(userService.getUserById(testUserId)).thenReturn(Optional.empty());

        mockMvc.perform(put("/insession/update/email")
                        .header("Authorization", authHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UpdateEmailRequest("newemail@example.com"))))
                .andExpect(status().isNotFound())
                .andExpect(content().string("User not found"));

        verify(jwtUtil).validateTokenAndGetUserId(validToken);
        verify(userService).getUserById(testUserId);
    }

    @Test
    void testUpdatePasswordInvalidOldPassword() throws Exception {
        UpdatePasswordRequest updatePasswordRequest = new UpdatePasswordRequest("newpassword", "wrongpassword");

        when(jwtUtil.validateTokenAndGetUserId(validToken)).thenReturn(testUserId);
        when(userService.getUserById(testUserId)).thenReturn(Optional.of(testUser));
        doThrow(new AuthenticationException("Invalid password"))
                .when(userService).updatePassword(testUser, "wrongpassword", "newpassword");

        mockMvc.perform(put("/insession/update/password")
                        .header("Authorization", authHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatePasswordRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Invalid access token"));  // The controller returns "Invalid access token" for all exceptions

        verify(jwtUtil).validateTokenAndGetUserId(validToken);
        verify(userService).getUserById(testUserId);
        verify(userService).updatePassword(testUser, "wrongpassword", "newpassword");
    }
}