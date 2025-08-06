package com.paloma.paloma.javaServer.dataTransferObjects.requests;

import com.paloma.paloma.javaServer.entities.enums.RoleType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Request object for removing a role from a user.
 * Used to replace PathVariables in the RoleController.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RemoveRoleRequest {
    private UUID userId;
    private RoleType roleType;
}