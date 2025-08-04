package com.paloma.paloma.javaServer.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.paloma.paloma.javaServer.dataTransferObjects.requests.AddRoleRequest;
import com.paloma.paloma.javaServer.dataTransferObjects.requests.AddTrustedContactRequest;
import com.paloma.paloma.javaServer.dataTransferObjects.responses.AddContactResponse;
import com.paloma.paloma.javaServer.dataTransferObjects.responses.UserRolesResponse;
import com.paloma.paloma.javaServer.entities.User;
import com.paloma.paloma.javaServer.entities.enums.RoleType;
import com.paloma.paloma.javaServer.services.RefreshService;
import com.paloma.paloma.javaServer.services.RoleManagementService;
import com.paloma.paloma.javaServer.services.UserService;
import com.paloma.paloma.javaServer.utilites.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * This test class checks for potential conflicts between the InSessionController and RoleController.
 * It tests scenarios where both controllers might interact with the same user or role data.
 */
@ExtendWith(MockitoExtension.class)
public class ControllerConflictTest {

    @Mock
    private UserService userService;

    @Mock
    private RoleManagementService roleManagementService;

    @Mock
    private RefreshService refreshService;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private InSessionController inSessionController;

    @InjectMocks
    private RoleController roleController;

    private MockMvc inSessionMockMvc;
    private MockMvc roleMockMvc;
    private ObjectMapper objectMapper;
    private User testUser;
    private UUID testUserId;
    private String validToken;
    private String authHeader;

    @BeforeEach
    void setUp() {
        inSessionMockMvc = MockMvcBuilders.standaloneSetup(inSessionController).build();
        roleMockMvc = MockMvcBuilders.standaloneSetup(roleController).build();
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

    /**
     * Test adding a trusted contact via InSessionController and then making them a trusted contact via RoleController.
     * This tests the scenario where a user adds a contact and then assigns them the TRUSTED_CONTACT role.
     */
    @Test
    void testAddContactThenMakeTrustedContact() throws Exception {
        // Setup for adding a contact
        UUID contactUserId = UUID.randomUUID();
        User contactUser = new User();
        contactUser.setId(contactUserId);
        contactUser.setUsername("contactuser");
        contactUser.setEmail("contact@example.com");
        contactUser.setPhone("+19876543210");

        AddTrustedContactRequest addTrustedContactRequest = new AddTrustedContactRequest(
                "contact@example.com", "+19876543210", "Please help me if I need support");
        
        AddContactResponse successResponse = new AddContactResponse(
                true, "Contact added successfully", true);

        // Setup for making the contact a trusted contact
        UserRolesResponse dualRoleResponse = new UserRolesResponse();
        dualRoleResponse.setUserId(contactUserId);
        dualRoleResponse.setUsername("contactuser");
        dualRoleResponse.setRoles(Arrays.asList(RoleType.USER, RoleType.TRUSTED_CONTACT));
        dualRoleResponse.setPrimaryRole(RoleType.USER);
        dualRoleResponse.setMessage("Successfully added TRUSTED_CONTACT role to user");

        // Mock the service calls
        when(jwtUtil.validateTokenAndGetUserId(validToken)).thenReturn(testUserId);
        when(userService.getUserById(testUserId)).thenReturn(Optional.of(testUser));
        when(userService.addContact(eq(testUser), eq("contact@example.com"), 
                eq("+19876543210"), eq("Please help me if I need support")))
                .thenReturn(successResponse);
        when(roleManagementService.makeTrustedContact(contactUserId)).thenReturn(dualRoleResponse);

        // First, add the contact via InSessionController
        inSessionMockMvc.perform(post("/insession/addContact")
                        .header("Authorization", authHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(addTrustedContactRequest)))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Contact added successfully")))
                .andExpect(content().string(containsString("true")));

        // Then, make the contact a trusted contact via RoleController
        roleMockMvc.perform(post("/api/roles/make-trusted-contact/{userId}", contactUserId))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Successfully added TRUSTED_CONTACT role to user")))
                .andExpect(content().string(containsString("TRUSTED_CONTACT")));
    }

    /**
     * Test adding a role via RoleController and then adding the same user as a trusted contact via InSessionController.
     * This tests the scenario where a user is assigned the TRUSTED_CONTACT role and then added as a contact.
     */
    @Test
    void testAddRoleThenAddContact() throws Exception {
        // Setup for adding a role
        UUID contactUserId = UUID.randomUUID();
        User contactUser = new User();
        contactUser.setId(contactUserId);
        contactUser.setUsername("contactuser");
        contactUser.setEmail("contact@example.com");
        contactUser.setPhone("+19876543210");

        AddRoleRequest addRoleRequest = new AddRoleRequest();
        addRoleRequest.setUserId(contactUserId);
        addRoleRequest.setRoleType(RoleType.TRUSTED_CONTACT);
        addRoleRequest.setPrimary(false);

        UserRolesResponse dualRoleResponse = new UserRolesResponse();
        dualRoleResponse.setUserId(contactUserId);
        dualRoleResponse.setUsername("contactuser");
        dualRoleResponse.setRoles(Arrays.asList(RoleType.USER, RoleType.TRUSTED_CONTACT));
        dualRoleResponse.setPrimaryRole(RoleType.USER);

        // Setup for adding a contact
        AddTrustedContactRequest addTrustedContactRequest = new AddTrustedContactRequest(
                "contact@example.com", "+19876543210", "Please help me if I need support");
        
        AddContactResponse successResponse = new AddContactResponse(
                true, "Contact added successfully", true);

        // Mock the service calls
        when(roleManagementService.addRoleToUser(any(AddRoleRequest.class))).thenReturn(dualRoleResponse);
        when(jwtUtil.validateTokenAndGetUserId(validToken)).thenReturn(testUserId);
        when(userService.getUserById(testUserId)).thenReturn(Optional.of(testUser));
        when(userService.addContact(eq(testUser), eq("contact@example.com"), 
                eq("+19876543210"), eq("Please help me if I need support")))
                .thenReturn(successResponse);

        // First, add the role via RoleController
        roleMockMvc.perform(post("/api/roles/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(addRoleRequest)))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("TRUSTED_CONTACT")));

        // Then, add the contact via InSessionController
        inSessionMockMvc.perform(post("/insession/addContact")
                        .header("Authorization", authHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(addTrustedContactRequest)))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Contact added successfully")))
                .andExpect(content().string(containsString("true")));
    }
}