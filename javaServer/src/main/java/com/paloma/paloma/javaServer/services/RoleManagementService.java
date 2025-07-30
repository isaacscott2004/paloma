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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RoleManagementService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;

    @Transactional
    public UserRolesResponse addRoleToUser(AddRoleRequest request) throws UserException {
        // Find the user
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new UserException("User not found with ID: " + request.getUserId()));

        // Check if user already has this role
        Optional<UserRole> existingUserRole = userRoleRepository.findByUserAndRoleType(user, request.getRoleType());
        if (existingUserRole.isPresent()) {
            throw new UserException("User already has role: " + request.getRoleType());
        }

        // Find or create the role
        Role role = roleRepository.findByRoleType(request.getRoleType())
                .orElseGet(() -> {
                    Role newRole = new Role();
                    newRole.setRoleType(request.getRoleType());
                    return roleRepository.save(newRole);
                });

        // Create UserRole relationship
        UserRole userRole = new UserRole();
        userRole.setUser(user);
        userRole.setRole(role);
        userRole.setPrimary(request.isPrimary());

        // If this is set as primary, we need to update existing primary roles
        if (request.isPrimary()) {
            updatePrimaryRole(user, request.getRoleType());
        }

        userRoleRepository.save(userRole);

        return getUserRoles(user.getId());
    }

    @Transactional
    public UserRolesResponse removeRoleFromUser(UUID userId, RoleType roleType) throws UserException {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserException("User not found with ID: " + userId));

        UserRole userRole = userRoleRepository.findByUserAndRoleType(user, roleType)
                .orElseThrow(() -> new UserException("User does not have role: " + roleType));

        // Prevent removing all roles - user must have at least one role
        List<UserRole> allUserRoles = userRoleRepository.findAllByUser(user);
        if (allUserRoles.size() <= 1) {
            throw new UserException("Cannot remove role - user must have at least one role");
        }

        // If removing primary role, set another role as primary
        if (userRole.getPrimary()) {
            UserRole newPrimaryRole = allUserRoles.stream()
                    .filter(ur -> !ur.getRole().getRoleType().equals(roleType))
                    .findFirst()
                    .orElseThrow(() -> new UserException("Cannot determine new primary role"));

            newPrimaryRole.setPrimary(true);
            userRoleRepository.save(newPrimaryRole);
        }

        userRoleRepository.delete(userRole);
        return getUserRoles(userId);
    }

    public UserRolesResponse getUserRoles(UUID userId) throws UserException {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserException("User not found with ID: " + userId));

        List<UserRole> userRoles = userRoleRepository.findAllByUser(user);
        List<RoleType> roles = userRoles.stream()
                .map(ur -> ur.getRole().getRoleType())
                .toList();

        RoleType primaryRole = userRoles.stream()
                .filter(UserRole::getPrimary)
                .map(ur -> ur.getRole().getRoleType())
                .findFirst()
                .orElse(RoleType.USER); // Default to USER if no primary role found

        return new UserRolesResponse(user.getId(), user.getUsername(), roles, primaryRole);
    }

    public boolean userHasRole(UUID userId, RoleType roleType) {
        try {
            User user = userRepository.findById(userId).orElse(null);
            if (user == null) return false;

            return userRoleRepository.findByUserAndRoleType(user, roleType).isPresent();
        } catch (Exception e) {
            return false;
        }
    }

    public boolean userHasBothRoles(UUID userId) {
        return userHasRole(userId, RoleType.USER) && userHasRole(userId, RoleType.TRUSTED_CONTACT);
    }

    @Transactional
    public UserRolesResponse makeTrustedContact(UUID userId) throws UserException {
        AddRoleRequest request = new AddRoleRequest();
        request.setUserId(userId);
        request.setRoleType(RoleType.TRUSTED_CONTACT);
        request.setPrimary(false); // Keep existing primary role

        return addRoleToUser(request);
    }

    private void updatePrimaryRole(User user, RoleType newPrimaryRoleType) {
        // Remove primary flag from all existing roles
        List<UserRole> existingRoles = userRoleRepository.findAllByUser(user);
        for (UserRole ur : existingRoles) {
            ur.setPrimary(false);
            userRoleRepository.save(ur);
        }
    }
}