package com.paloma.paloma.javaServer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.paloma.paloma.javaServer.controllers.RoleController;
import com.paloma.paloma.javaServer.dataTransferObjects.requests.AddRoleRequest;
import com.paloma.paloma.javaServer.dataTransferObjects.responses.UserRolesResponse;
import com.paloma.paloma.javaServer.entities.enums.RoleType;
import com.paloma.paloma.javaServer.exceptions.UserException;
import com.paloma.paloma.javaServer.services.RoleManagementService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RoleController.class)
class RoleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RoleManagementService roleManagementService;

    @Autowired
    private ObjectMapper objectMapper;

    private UUID testUserId;
    private UserRolesResponse dualRoleResponse;
    private AddRoleRequest addRoleRequest;

    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID();
        
        dualRoleResponse = new UserRolesResponse();
        dualRoleResponse.setUserId(testUserId);
        dualRoleResponse.setUsername("testuser");
        dualRoleResponse.setRoles(Arrays.asList(RoleType.USER, RoleType.TRUSTED_CONTACT));
        dualRoleResponse.setPrimaryRole(RoleType.USER);

        addRoleRequest = new AddRoleRequest();
        addRoleRequest.setUserId(testUserId);
        addRoleRequest.setRoleType(RoleType.TRUSTED_CONTACT);
        addRoleRequest.setPrimary(false);
    }

    @Test
    void testAddTrustedContactRole() throws Exception {
        // Arrange
        when(roleManagementService.addRoleToUser(any(AddRoleRequest.class)))
                .thenReturn(dualRoleResponse);

        // Act & Assert
        mockMvc.perform(post("/api/roles/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(addRoleRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(testUserId.toString()))
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.roles[0]").value("USER"))
                .andExpect(jsonPath("$.roles[1]").value("TRUSTED_CONTACT"))
                .andExpect(jsonPath("$.primaryRole").value("USER"));
    }

    @Test
    void testAddRoleAlreadyExists() throws Exception {
        // Arrange
        when(roleManagementService.addRoleToUser(any(AddRoleRequest.class)))
                .thenThrow(new UserException("User already has role: TRUSTED_CONTACT"));

        // Act & Assert
        mockMvc.perform(post("/api/roles/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(addRoleRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("User already has role: TRUSTED_CONTACT"));
    }

    @Test
    void testGetUserRolesDualRole() throws Exception {
        // Arrange
        when(roleManagementService.getUserRoles(testUserId))
                .thenReturn(dualRoleResponse);

        // Act & Assert
        mockMvc.perform(get("/api/roles/user/{userId}", testUserId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(testUserId.toString()))
                .andExpect(jsonPath("$.roles.length()").value(2))
                .andExpect(jsonPath("$.primaryRole").value("USER"));
    }

    @Test
    void testRemoveRole() throws Exception {
        // Arrange
        UserRolesResponse singleRoleResponse = new UserRolesResponse();
        singleRoleResponse.setUserId(testUserId);
        singleRoleResponse.setUsername("testuser");
        singleRoleResponse.setRoles(Arrays.asList(RoleType.USER));
        singleRoleResponse.setPrimaryRole(RoleType.USER);

        when(roleManagementService.removeRoleFromUser(testUserId, RoleType.TRUSTED_CONTACT))
                .thenReturn(singleRoleResponse);

        // Act & Assert
        mockMvc.perform(delete("/api/roles/remove/{userId}/{roleType}", testUserId, "TRUSTED_CONTACT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.roles.length()").value(1))
                .andExpect(jsonPath("$.roles[0]").value("USER"));
    }

    @Test
    void testMakeTrustedContact() throws Exception {
        // Arrange
        dualRoleResponse.setMessage("Successfully added TRUSTED_CONTACT role to user");
        when(roleManagementService.makeTrustedContact(testUserId))
                .thenReturn(dualRoleResponse);

        // Act & Assert
        mockMvc.perform(post("/api/roles/make-trusted-contact/{userId}", testUserId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Successfully added TRUSTED_CONTACT role to user"))
                .andExpect(jsonPath("$.roles.length()").value(2));
    }

    @Test
    void testCheckUserHasBothRoles() throws Exception {
        // Arrange
        when(roleManagementService.userHasBothRoles(testUserId))
                .thenReturn(true);

        // Act & Assert
        mockMvc.perform(get("/api/roles/check/{userId}/has-both-roles", testUserId))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }

    @Test
    void testCheckUserHasSpecificRole() throws Exception {
        // Arrange
        when(roleManagementService.userHasRole(testUserId, RoleType.TRUSTED_CONTACT))
                .thenReturn(true);

        // Act & Assert
        mockMvc.perform(get("/api/roles/check/{userId}/has-role/{roleType}", testUserId, "TRUSTED_CONTACT"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }

    @Test
    void testCheckUserDoesNotHaveRole() throws Exception {
        // Arrange
        when(roleManagementService.userHasRole(testUserId, RoleType.TRUSTED_CONTACT))
                .thenReturn(false);

        // Act & Assert
        mockMvc.perform(get("/api/roles/check/{userId}/has-role/{roleType}", testUserId, "TRUSTED_CONTACT"))
                .andExpect(status().isOk())
                .andExpect(content().string("false"));
    }

    @Test
    void testUserNotFound() throws Exception {
        // Arrange
        when(roleManagementService.getUserRoles(testUserId))
                .thenThrow(new UserException("User not found with ID: " + testUserId));

        // Act & Assert
        mockMvc.perform(get("/api/roles/user/{userId}", testUserId))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("User not found with ID: " + testUserId));
    }

    @Test
    void testCannotRemoveLastRole() throws Exception {
        // Arrange
        when(roleManagementService.removeRoleFromUser(testUserId, RoleType.USER))
                .thenThrow(new UserException("Cannot remove role - user must have at least one role"));

        // Act & Assert
        mockMvc.perform(delete("/api/roles/remove/{userId}/{roleType}", testUserId, "USER"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Cannot remove role - user must have at least one role"));
    }
}