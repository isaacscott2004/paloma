package com.paloma.paloma.javaServer.dataTransferObjects.requests;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Request object for getting a user's roles.
 * Used to replace PathVariables in the RoleController.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GetUserRolesRequest {
    private UUID userId;
}