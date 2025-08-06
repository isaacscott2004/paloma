package com.paloma.paloma.javaServer.dataTransferObjects.requests;

import com.paloma.paloma.javaServer.entities.enums.RoleType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Request object for checking a user's roles.
 * Used to replace PathVariables in the RoleController.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CheckUserRolesRequest {
    private UUID userId;
    private RoleType roleType;
    
    /**
     * Constructor for checking if a user has both roles.
     * 
     * @param userId The ID of the user to check
     */
    public CheckUserRolesRequest(UUID userId) {
        this.userId = userId;
        this.roleType = null;
    }
}