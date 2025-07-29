package com.paloma.paloma.javaServer.controllers;

import com.paloma.paloma.javaServer.dataTransferObjects.requests.AddRoleRequest;
import com.paloma.paloma.javaServer.dataTransferObjects.responses.UserRolesResponse;
import com.paloma.paloma.javaServer.entities.enums.RoleType;
import com.paloma.paloma.javaServer.exceptions.UserException;
import com.paloma.paloma.javaServer.services.RoleManagementService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/roles")
@RequiredArgsConstructor
public class RoleController {

    private final RoleManagementService roleManagementService;

    @PostMapping("/add")
    public ResponseEntity<UserRolesResponse> addRole(@RequestBody AddRoleRequest request) {
        try {
            UserRolesResponse response = roleManagementService.addRoleToUser(request);
            return ResponseEntity.ok(response);
        } catch (UserException e) {
            UserRolesResponse errorResponse = new UserRolesResponse();
            errorResponse.setMessage(e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @DeleteMapping("/remove/{userId}/{roleType}")
    public ResponseEntity<UserRolesResponse> removeRole(
            @PathVariable UUID userId, 
            @PathVariable RoleType roleType) {
        try {
            UserRolesResponse response = roleManagementService.removeRoleFromUser(userId, roleType);
            return ResponseEntity.ok(response);
        } catch (UserException e) {
            UserRolesResponse errorResponse = new UserRolesResponse();
            errorResponse.setMessage(e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<UserRolesResponse> getUserRoles(@PathVariable UUID userId) {
        try {
            UserRolesResponse response = roleManagementService.getUserRoles(userId);
            return ResponseEntity.ok(response);
        } catch (UserException e) {
            UserRolesResponse errorResponse = new UserRolesResponse();
            errorResponse.setMessage(e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @PostMapping("/make-trusted-contact/{userId}")
    public ResponseEntity<UserRolesResponse> makeTrustedContact(@PathVariable UUID userId) {
        try {
            UserRolesResponse response = roleManagementService.makeTrustedContact(userId);
            response.setMessage("Successfully added TRUSTED_CONTACT role to user");
            return ResponseEntity.ok(response);
        } catch (UserException e) {
            UserRolesResponse errorResponse = new UserRolesResponse();
            errorResponse.setMessage(e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @GetMapping("/check/{userId}/has-both-roles")
    public ResponseEntity<Boolean> checkUserHasBothRoles(@PathVariable UUID userId) {
        boolean hasBothRoles = roleManagementService.userHasBothRoles(userId);
        return ResponseEntity.ok(hasBothRoles);
    }

    @GetMapping("/check/{userId}/has-role/{roleType}")
    public ResponseEntity<Boolean> checkUserHasRole(
            @PathVariable UUID userId, 
            @PathVariable RoleType roleType) {
        boolean hasRole = roleManagementService.userHasRole(userId, roleType);
        return ResponseEntity.ok(hasRole);
    }
}