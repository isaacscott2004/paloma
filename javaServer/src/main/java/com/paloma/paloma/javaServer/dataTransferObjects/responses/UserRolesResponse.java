package com.paloma.paloma.javaServer.dataTransferObjects.responses;

import com.paloma.paloma.javaServer.entities.enums.RoleType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserRolesResponse {
    private UUID userId;
    private String username;
    private List<RoleType> roles;
    private RoleType primaryRole;
    private String message;

    public UserRolesResponse(UUID userId, String username, List<RoleType> roles, RoleType primaryRole) {
        this.userId = userId;
        this.username = username;
        this.roles = roles;
        this.primaryRole = primaryRole;
    }
}