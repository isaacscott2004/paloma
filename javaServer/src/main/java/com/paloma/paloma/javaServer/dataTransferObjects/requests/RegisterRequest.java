package com.paloma.paloma.javaServer.dataTransferObjects.requests;

import com.paloma.paloma.javaServer.entities.enums.RoleType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class RegisterRequest {
    private String email;
    private String username;
    private String fullName;
    private String phone;
    private String password;
    private RoleType roleType;
}
