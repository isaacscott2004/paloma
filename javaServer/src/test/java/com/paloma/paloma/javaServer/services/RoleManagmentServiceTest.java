package com.paloma.paloma.javaServer.services;

import com.paloma.paloma.javaServer.dataTransferObjects.requests.AddRoleRequest;
import com.paloma.paloma.javaServer.dataTransferObjects.responses.UserRolesResponse;
import com.paloma.paloma.javaServer.entities.Role;
import com.paloma.paloma.javaServer.entities.User;
import com.paloma.paloma.javaServer.entities.UserRole;
import com.paloma.paloma.javaServer.entities.enums.RoleType;
import com.paloma.paloma.javaServer.exceptions.UserException;
import com.paloma.paloma.javaServer.repositories.RoleRepository;
import com.paloma.paloma.javaServer.repositories.UserRepository;
import com.paloma.paloma.javaServer.repositories.UserRoleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoleManagementServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private UserRoleRepository userRoleRepository;

    @InjectMocks
    private RoleManagementService roleManagementService;

    private User testUser;
    private Role trustedContactRole;
    private UserRole testUserRole;
    private UserRole testTrustedContactRole;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(UUID.randomUUID());
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");

        Role userRole = new Role();
        userRole.setId(UUID.randomUUID());
        userRole.setRoleType(RoleType.USER);

        trustedContactRole = new Role();
        trustedContactRole.setId(UUID.randomUUID());
        trustedContactRole.setRoleType(RoleType.TRUSTED_CONTACT);

        testUserRole = new UserRole();
        testUserRole.setId(UUID.randomUUID());
        testUserRole.setUser(testUser);
        testUserRole.setRole(userRole);
        testUserRole.setPrimary(true);

        testTrustedContactRole = new UserRole();
        testTrustedContactRole.setId(UUID.randomUUID());
        testTrustedContactRole.setUser(testUser);
        testTrustedContactRole.setRole(trustedContactRole);
        testTrustedContactRole.setPrimary(false);
    }

    @Test
    void testAddTrustedContactRoleToExistingUser() throws UserException {
        // Arrange
        when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));
        when(userRoleRepository.findByUserAndRoleType(testUser, RoleType.TRUSTED_CONTACT))
                .thenReturn(Optional.empty());
        when(roleRepository.findByRoleType(RoleType.TRUSTED_CONTACT))
                .thenReturn(Optional.of(trustedContactRole));
        when(userRoleRepository.save(any(UserRole.class))).thenReturn(testTrustedContactRole);
        when(userRoleRepository.findAllByUser(testUser))
                .thenReturn(Arrays.asList(testUserRole, testTrustedContactRole));

        AddRoleRequest request = new AddRoleRequest();
        request.setUserId(testUser.getId());
        request.setRoleType(RoleType.TRUSTED_CONTACT);
        request.setPrimary(false);

        // Act
        UserRolesResponse response = roleManagementService.addRoleToUser(request);

        // Assert
        assertNotNull(response);
        assertEquals(testUser.getId(), response.getUserId());
        assertEquals(testUser.getUsername(), response.getUsername());
        assertEquals(2, response.getRoles().size());
        assertTrue(response.getRoles().contains(RoleType.USER));
        assertTrue(response.getRoles().contains(RoleType.TRUSTED_CONTACT));
        assertEquals(RoleType.USER, response.getPrimaryRole());

        verify(userRoleRepository).save(any(UserRole.class));
    }

    @Test
    void testUserHasBothRoles() {
        // Arrange
        when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));
        when(userRoleRepository.findByUserAndRoleType(testUser, RoleType.USER))
                .thenReturn(Optional.of(testUserRole));
        when(userRoleRepository.findByUserAndRoleType(testUser, RoleType.TRUSTED_CONTACT))
                .thenReturn(Optional.of(testTrustedContactRole));

        // Act
        boolean hasBothRoles = roleManagementService.userHasBothRoles(testUser.getId());

        // Assert
        assertTrue(hasBothRoles);
    }

    @Test
    void testUserHasOnlyUserRole() {
        // Arrange
        when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));
        when(userRoleRepository.findByUserAndRoleType(testUser, RoleType.USER))
                .thenReturn(Optional.of(testUserRole));
        when(userRoleRepository.findByUserAndRoleType(testUser, RoleType.TRUSTED_CONTACT))
                .thenReturn(Optional.empty());

        // Act
        boolean hasBothRoles = roleManagementService.userHasBothRoles(testUser.getId());

        // Assert
        assertFalse(hasBothRoles);
    }

    @Test
    void testMakeTrustedContact() throws UserException {
        // Arrange
        when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));
        when(userRoleRepository.findByUserAndRoleType(testUser, RoleType.TRUSTED_CONTACT))
                .thenReturn(Optional.empty());
        when(roleRepository.findByRoleType(RoleType.TRUSTED_CONTACT))
                .thenReturn(Optional.of(trustedContactRole));
        when(userRoleRepository.save(any(UserRole.class))).thenReturn(testTrustedContactRole);
        when(userRoleRepository.findAllByUser(testUser))
                .thenReturn(Arrays.asList(testUserRole, testTrustedContactRole));

        // Act
        UserRolesResponse response = roleManagementService.makeTrustedContact(testUser.getId());

        // Assert
        assertNotNull(response);
        assertEquals(testUser.getId(), response.getUserId());
        assertTrue(response.getRoles().contains(RoleType.TRUSTED_CONTACT));
        assertTrue(response.getRoles().contains(RoleType.USER));
        assertEquals(RoleType.USER, response.getPrimaryRole()); // Primary should remain USER
    }

    @Test
    void testAddDuplicateRoleThrowsException() {
        // Arrange
        when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));
        when(userRoleRepository.findByUserAndRoleType(testUser, RoleType.USER))
                .thenReturn(Optional.of(testUserRole));

        AddRoleRequest request = new AddRoleRequest();
        request.setUserId(testUser.getId());
        request.setRoleType(RoleType.USER);

        // Act & Assert
        UserException exception = assertThrows(UserException.class,
                () -> roleManagementService.addRoleToUser(request));
        assertEquals("User already has role: USER", exception.getMessage());
    }

    @Test
    void testRemoveRoleFromDualRoleUser() throws UserException {
        // Arrange
        when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));
        when(userRoleRepository.findByUserAndRoleType(testUser, RoleType.TRUSTED_CONTACT))
                .thenReturn(Optional.of(testTrustedContactRole));
        when(userRoleRepository.findAllByUser(testUser))
                .thenReturn(Arrays.asList(testUserRole, testTrustedContactRole));

        // Act
        UserRolesResponse response = roleManagementService.removeRoleFromUser(
                testUser.getId(), RoleType.TRUSTED_CONTACT);

        // Assert
        verify(userRoleRepository).delete(testTrustedContactRole);
        // Verify we're calling getUserRoles after deletion
        verify(userRepository, times(2)).findById(testUser.getId());
    }

    @Test
    void testCannotRemoveLastRole() {
        // Arrange
        when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));
        when(userRoleRepository.findByUserAndRoleType(testUser, RoleType.USER))
                .thenReturn(Optional.of(testUserRole));
        when(userRoleRepository.findAllByUser(testUser))
                .thenReturn(Collections.singletonList(testUserRole)); // Only one role

        // Act & Assert
        UserException exception = assertThrows(UserException.class,
                () -> roleManagementService.removeRoleFromUser(testUser.getId(), RoleType.USER));
        assertEquals("Cannot remove role - user must have at least one role", exception.getMessage());
    }

    @Test
    void testGetUserRolesForDualRoleUser() throws UserException {
        // Arrange
        when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));
        when(userRoleRepository.findAllByUser(testUser))
                .thenReturn(Arrays.asList(testUserRole, testTrustedContactRole));

        // Act
        UserRolesResponse response = roleManagementService.getUserRoles(testUser.getId());

        // Assert
        assertNotNull(response);
        assertEquals(testUser.getId(), response.getUserId());
        assertEquals(testUser.getUsername(), response.getUsername());
        assertEquals(2, response.getRoles().size());
        assertTrue(response.getRoles().contains(RoleType.USER));
        assertTrue(response.getRoles().contains(RoleType.TRUSTED_CONTACT));
        assertEquals(RoleType.USER, response.getPrimaryRole());
    }

    @Test
    void testUpdatePrimaryRoleWhenAddingSecondRole() throws UserException {
        // Arrange
        when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));
        when(userRoleRepository.findByUserAndRoleType(testUser, RoleType.TRUSTED_CONTACT))
                .thenReturn(Optional.empty());
        when(roleRepository.findByRoleType(RoleType.TRUSTED_CONTACT))
                .thenReturn(Optional.of(trustedContactRole));
        when(userRoleRepository.findAllByUser(testUser))
                .thenReturn(Collections.singletonList(testUserRole)); // Existing roles when updating primary
        when(userRoleRepository.save(any(UserRole.class))).thenReturn(testTrustedContactRole);

        AddRoleRequest request = new AddRoleRequest();
        request.setUserId(testUser.getId());
        request.setRoleType(RoleType.TRUSTED_CONTACT);
        request.setPrimary(true); // Make this the primary role

        // Act
        roleManagementService.addRoleToUser(request);

        // Assert
        verify(userRoleRepository, times(2)).save(any(UserRole.class)); // Once for updating existing, once for new
    }

    @Test
    void testCreateRoleIfNotExists() throws UserException {
        // Arrange
        when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));
        when(userRoleRepository.findByUserAndRoleType(testUser, RoleType.TRUSTED_CONTACT))
                .thenReturn(Optional.empty());
        when(roleRepository.findByRoleType(RoleType.TRUSTED_CONTACT))
                .thenReturn(Optional.empty()); // Role doesn't exist
        when(roleRepository.save(any(Role.class))).thenReturn(trustedContactRole);
        when(userRoleRepository.save(any(UserRole.class))).thenReturn(testTrustedContactRole);
        when(userRoleRepository.findAllByUser(testUser))
                .thenReturn(Arrays.asList(testUserRole, testTrustedContactRole));

        AddRoleRequest request = new AddRoleRequest();
        request.setUserId(testUser.getId());
        request.setRoleType(RoleType.TRUSTED_CONTACT);

        // Act
        UserRolesResponse response = roleManagementService.addRoleToUser(request);

        // Assert
        verify(roleRepository).save(any(Role.class)); // Verify a new role was created
        assertNotNull(response);
        assertTrue(response.getRoles().contains(RoleType.TRUSTED_CONTACT));
    }
}