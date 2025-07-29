package com.paloma.paloma.javaServer.dataTransferObjects.requests;

import com.paloma.paloma.javaServer.entities.enums.RoleType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddRoleRequest {
    private UUID userId;
    private RoleType roleType;
    private boolean isPrimary = false;
}