package com.paloma.paloma.javaServer.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.paloma.paloma.javaServer.dataTransferObjects.requests.*;
import com.paloma.paloma.javaServer.dataTransferObjects.responses.*;
import com.paloma.paloma.javaServer.entities.enums.SensitivityLevel;
import com.paloma.paloma.javaServer.entities.RefreshAuth;
import com.paloma.paloma.javaServer.entities.User;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
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
import static org.hamcrest.Matchers.hasSize;
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

        mockMvc.perform(post("/insession/logout")
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
    void testAddContactSuccess() throws Exception {
        AddTrustedContactRequest addTrustedContactRequest = new AddTrustedContactRequest(
                "contact@example.com", "Please help me if I need support");
        
        AddContactResponse successResponse = new AddContactResponse(
                true, "Contact added successfully", true);

        when(jwtUtil.validateTokenAndGetUserId(validToken)).thenReturn(testUserId);
        when(userService.getUserById(testUserId)).thenReturn(Optional.of(testUser));
        when(userService.addContact(eq(testUser), eq("contact@example.com"), 
                eq("Please help me if I need support")))
                .thenReturn(successResponse);

        mockMvc.perform(post("/insession/add/contact")
                        .header("Authorization", authHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(addTrustedContactRequest)))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Contact added successfully")))
                .andExpect(content().string(containsString("true")));

        verify(jwtUtil).validateTokenAndGetUserId(validToken);
        verify(userService).getUserById(testUserId);
        verify(userService).addContact(eq(testUser), eq("contact@example.com"), 
                eq("Please help me if I need support"));
    }

    @Test
    void testAddContactNewUser() throws Exception {
        AddTrustedContactRequest addTrustedContactRequest = new AddTrustedContactRequest(
                "newcontact@example.com", "Please help me if I need support");
        
        AddContactResponse successResponse = new AddContactResponse(
                true, "Contact added and invitation email sent", false, true);

        when(jwtUtil.validateTokenAndGetUserId(validToken)).thenReturn(testUserId);
        when(userService.getUserById(testUserId)).thenReturn(Optional.of(testUser));
        when(userService.addContact(eq(testUser), eq("newcontact@example.com"), 
                eq("Please help me if I need support")))
                .thenReturn(successResponse);

        mockMvc.perform(post("/insession/add/contact")
                        .header("Authorization", authHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(addTrustedContactRequest)))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Contact added and invitation email sent")))
                .andExpect(content().string(containsString("true")));

        verify(jwtUtil).validateTokenAndGetUserId(validToken);
        verify(userService).getUserById(testUserId);
        verify(userService).addContact(eq(testUser), eq("newcontact@example.com"), 
                eq("Please help me if I need support"));
    }

    @Test
    void testAddContactFailure() throws Exception {
        AddTrustedContactRequest addTrustedContactRequest = new AddTrustedContactRequest(
                "contact@example.com", "Please help me if I need support");
        
        AddContactResponse failureResponse = new AddContactResponse(
                false, "Failed to add contact: Database error", false);

        when(jwtUtil.validateTokenAndGetUserId(validToken)).thenReturn(testUserId);
        when(userService.getUserById(testUserId)).thenReturn(Optional.of(testUser));
        when(userService.addContact(eq(testUser), eq("contact@example.com"), 
                eq("Please help me if I need support")))
                .thenReturn(failureResponse);

        mockMvc.perform(post("/insession/add/contact")
                        .header("Authorization", authHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(addTrustedContactRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Failed to add contact: Database error")))
                .andExpect(content().string(containsString("false")));

        verify(jwtUtil).validateTokenAndGetUserId(validToken);
        verify(userService).getUserById(testUserId);
        verify(userService).addContact(eq(testUser), eq("contact@example.com"), 
                eq("Please help me if I need support"));
    }
    
    @Test
    void testRemoveContactSuccess() throws Exception {
        RemoveTrustedContactRequest removeTrustedContactRequest = new RemoveTrustedContactRequest(
                "contact@example.com");
        
        RemoveContactResponse successResponse = new RemoveContactResponse(
                true, "Contact removed successfully");

        when(jwtUtil.validateTokenAndGetUserId(validToken)).thenReturn(testUserId);
        when(userService.getUserById(testUserId)).thenReturn(Optional.of(testUser));
        when(userService.removeContact(eq(testUser), eq("contact@example.com")))
                .thenReturn(successResponse);

        mockMvc.perform(delete("/insession/remove/contact")
                        .header("Authorization", authHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(removeTrustedContactRequest)))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Contact removed successfully")))
                .andExpect(content().string(containsString("true")));

        verify(jwtUtil).validateTokenAndGetUserId(validToken);
        verify(userService).getUserById(testUserId);
        verify(userService).removeContact(eq(testUser), eq("contact@example.com"));
    }
    
    @Test
    void testRemoveContactFailure() throws Exception {
        RemoveTrustedContactRequest removeTrustedContactRequest = new RemoveTrustedContactRequest(
                "nonexistent@example.com");
        
        RemoveContactResponse failureResponse = new RemoveContactResponse(
                false, "Contact not found");

        when(jwtUtil.validateTokenAndGetUserId(validToken)).thenReturn(testUserId);
        when(userService.getUserById(testUserId)).thenReturn(Optional.of(testUser));
        when(userService.removeContact(eq(testUser), eq("nonexistent@example.com")))
                .thenReturn(failureResponse);

        mockMvc.perform(delete("/insession/remove/contact")
                        .header("Authorization", authHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(removeTrustedContactRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Contact not found")))
                .andExpect(content().string(containsString("false")));

        verify(jwtUtil).validateTokenAndGetUserId(validToken);
        verify(userService).getUserById(testUserId);
        verify(userService).removeContact(eq(testUser), eq("nonexistent@example.com"));
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
    
    @Test
    void testDailyCheckinSuccess() throws Exception {
        DailyCheckinRequest dailyCheckinRequest = new DailyCheckinRequest(8, 7, 6, 2, "Feeling good today");
        DailyCheckinResponse successResponse = new DailyCheckinResponse(true, "Daily checkin recorded successfully");
        
        when(jwtUtil.validateTokenAndGetUserId(validToken)).thenReturn(testUserId);
        when(userService.getUserById(testUserId)).thenReturn(Optional.of(testUser));
        when(userService.dailyCheckin(eq(testUser), eq(8), eq(7), eq(6), eq(2), eq("Feeling good today")))
                .thenReturn(successResponse);

        mockMvc.perform(post("/insession/dailyCheckin")
                        .header("Authorization", authHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dailyCheckinRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Daily checkin recorded successfully"));

        verify(jwtUtil).validateTokenAndGetUserId(validToken);
        verify(userService).getUserById(testUserId);
        verify(userService).dailyCheckin(testUser, 8, 7, 6, 2, "Feeling good today");
    }
    
    @Test
    void testDailyCheckinFailure() throws Exception {
        DailyCheckinRequest dailyCheckinRequest = new DailyCheckinRequest(12, 7, 6, 2, "Invalid mood score");
        DailyCheckinResponse failureResponse = new DailyCheckinResponse(false, "Mood score must be less than or equal to 10");
        
        when(jwtUtil.validateTokenAndGetUserId(validToken)).thenReturn(testUserId);
        when(userService.getUserById(testUserId)).thenReturn(Optional.of(testUser));
        when(userService.dailyCheckin(eq(testUser), eq(12), eq(7), eq(6), eq(2), eq("Invalid mood score")))
                .thenReturn(failureResponse);

        mockMvc.perform(post("/insession/dailyCheckin")
                        .header("Authorization", authHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dailyCheckinRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Mood score must be less than or equal to 10"));

        verify(jwtUtil).validateTokenAndGetUserId(validToken);
        verify(userService).getUserById(testUserId);
        verify(userService).dailyCheckin(testUser, 12, 7, 6, 2, "Invalid mood score");
    }
    
    @Test
    void testAddAlertSensitivitySuccess() throws Exception {
        AddAlertSensitivityRequest addAlertSensitivityRequest = new AddAlertSensitivityRequest(SensitivityLevel.MEDIUM);
        AddAlertSensitivityResponse successResponse = new AddAlertSensitivityResponse(true, "Alert sensitivity added successfully");
        
        when(jwtUtil.validateTokenAndGetUserId(validToken)).thenReturn(testUserId);
        when(userService.getUserById(testUserId)).thenReturn(Optional.of(testUser));
        when(userService.addAlertSensitivity(eq(testUser), eq(SensitivityLevel.MEDIUM)))
                .thenReturn(successResponse);

        mockMvc.perform(post("/insession/add/sensitivityLevel")
                        .header("Authorization", authHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(addAlertSensitivityRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Alert sensitivity added successfully"));

        verify(jwtUtil).validateTokenAndGetUserId(validToken);
        verify(userService).getUserById(testUserId);
        verify(userService).addAlertSensitivity(testUser, SensitivityLevel.MEDIUM);
    }
    
    @Test
    void testAddAlertSensitivityFailure() throws Exception {
        AddAlertSensitivityRequest addAlertSensitivityRequest = new AddAlertSensitivityRequest(SensitivityLevel.HIGH);
        AddAlertSensitivityResponse failureResponse = new AddAlertSensitivityResponse(false, "Alert sensitivity already exists for this user");
        
        when(jwtUtil.validateTokenAndGetUserId(validToken)).thenReturn(testUserId);
        when(userService.getUserById(testUserId)).thenReturn(Optional.of(testUser));
        when(userService.addAlertSensitivity(eq(testUser), eq(SensitivityLevel.HIGH)))
                .thenReturn(failureResponse);

        mockMvc.perform(post("/insession/add/sensitivityLevel")
                        .header("Authorization", authHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(addAlertSensitivityRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Alert sensitivity already exists for this user"));

        verify(jwtUtil).validateTokenAndGetUserId(validToken);
        verify(userService).getUserById(testUserId);
        verify(userService).addAlertSensitivity(testUser, SensitivityLevel.HIGH);
    }
    
    @Test
    void testUpdateAlertSensitivitySuccess() throws Exception {
        UpdateAlertSensitivityRequest updateAlertSensitivityRequest = new UpdateAlertSensitivityRequest(SensitivityLevel.LOW);
        UpdateAlertSensitivityResponse successResponse = new UpdateAlertSensitivityResponse(true, "Alert sensitivity updated successfully");
        
        when(jwtUtil.validateTokenAndGetUserId(validToken)).thenReturn(testUserId);
        when(userService.getUserById(testUserId)).thenReturn(Optional.of(testUser));
        when(userService.updateSensitivity(eq(testUser), eq(SensitivityLevel.LOW)))
                .thenReturn(successResponse);

        mockMvc.perform(put("/insession/update/sensitivityLevel")
                        .header("Authorization", authHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateAlertSensitivityRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Alert sensitivity updated successfully"));

        verify(jwtUtil).validateTokenAndGetUserId(validToken);
        verify(userService).getUserById(testUserId);
        verify(userService).updateSensitivity(testUser, SensitivityLevel.LOW);
    }
    
    @Test
    void testUpdateAlertSensitivityFailure() throws Exception {
        UpdateAlertSensitivityRequest updateAlertSensitivityRequest = new UpdateAlertSensitivityRequest(SensitivityLevel.HIGH);
        UpdateAlertSensitivityResponse failureResponse = new UpdateAlertSensitivityResponse(false, "Alert sensitivity not found for this user");
        
        when(jwtUtil.validateTokenAndGetUserId(validToken)).thenReturn(testUserId);
        when(userService.getUserById(testUserId)).thenReturn(Optional.of(testUser));
        when(userService.updateSensitivity(eq(testUser), eq(SensitivityLevel.HIGH)))
                .thenReturn(failureResponse);

        mockMvc.perform(put("/insession/update/sensitivityLevel")
                        .header("Authorization", authHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateAlertSensitivityRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Alert sensitivity not found for this user"));

        verify(jwtUtil).validateTokenAndGetUserId(validToken);
        verify(userService).getUserById(testUserId);
        verify(userService).updateSensitivity(testUser, SensitivityLevel.HIGH);
    }
    
    @Test
    void testGetOverallScoresSuccess() throws Exception {
        GetOverallScoresRequest getOverallScoresRequest = new GetOverallScoresRequest(7);
        List<Integer> scores = Arrays.asList(75, 80, 85, 70, 90, 85, 80);
        GetOverallScoresResponse successResponse = new GetOverallScoresResponse(true, "Scores retrieved successfully", scores);
        
        when(jwtUtil.validateTokenAndGetUserId(validToken)).thenReturn(testUserId);
        when(userService.getUserById(testUserId)).thenReturn(Optional.of(testUser));
        when(userService.getOverallScores(eq(testUser), eq(7)))
                .thenReturn(successResponse);

        mockMvc.perform(get("/insession/dailyCheckin/getOverallScores")
                        .header("Authorization", authHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(getOverallScoresRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Scores retrieved successfully"))
                .andExpect(jsonPath("$.scores").isArray())
                .andExpect(jsonPath("$.scores", hasSize(7)))
                .andExpect(jsonPath("$.scores[0]").value(75))
                .andExpect(jsonPath("$.scores[6]").value(80));

        verify(jwtUtil).validateTokenAndGetUserId(validToken);
        verify(userService).getUserById(testUserId);
        verify(userService).getOverallScores(testUser, 7);
    }
    
    @Test
    void testGetOverallScoresNoContent() throws Exception {
        GetOverallScoresRequest getOverallScoresRequest = new GetOverallScoresRequest(7);
        List<Integer> emptyScores = Collections.emptyList();
        GetOverallScoresResponse emptyResponse = new GetOverallScoresResponse(true, "No scores found", emptyScores);
        
        when(jwtUtil.validateTokenAndGetUserId(validToken)).thenReturn(testUserId);
        when(userService.getUserById(testUserId)).thenReturn(Optional.of(testUser));
        when(userService.getOverallScores(eq(testUser), eq(7)))
                .thenReturn(emptyResponse);

        mockMvc.perform(get("/insession/dailyCheckin/getOverallScores")
                        .header("Authorization", authHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(getOverallScoresRequest)))
                .andExpect(status().isNoContent());

        verify(jwtUtil).validateTokenAndGetUserId(validToken);
        verify(userService).getUserById(testUserId);
        verify(userService).getOverallScores(testUser, 7);
    }
    
    @Test
    void testGetOverallScoresFailure() throws Exception {
        GetOverallScoresRequest getOverallScoresRequest = new GetOverallScoresRequest(-1); // Invalid number of days
        GetOverallScoresResponse failureResponse = new GetOverallScoresResponse(false, "Number of days must be positive", null);
        
        when(jwtUtil.validateTokenAndGetUserId(validToken)).thenReturn(testUserId);
        when(userService.getUserById(testUserId)).thenReturn(Optional.of(testUser));
        when(userService.getOverallScores(eq(testUser), eq(-1)))
                .thenReturn(failureResponse);

        mockMvc.perform(get("/insession/dailyCheckin/getOverallScores")
                        .header("Authorization", authHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(getOverallScoresRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Number of days must be positive"));

        verify(jwtUtil).validateTokenAndGetUserId(validToken);
        verify(userService).getUserById(testUserId);
        verify(userService).getOverallScores(testUser, -1);
    }
}