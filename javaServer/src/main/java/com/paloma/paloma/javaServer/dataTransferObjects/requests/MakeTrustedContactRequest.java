package com.paloma.paloma.javaServer.dataTransferObjects.requests;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Request object for making a user a trusted contact.
 * Used to replace PathVariables in the RoleController.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MakeTrustedContactRequest {
    private UUID userId;
}