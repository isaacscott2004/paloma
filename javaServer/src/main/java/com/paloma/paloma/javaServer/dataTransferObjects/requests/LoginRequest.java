package com.paloma.paloma.javaServer.dataTransferObjects.requests;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class LoginRequest {
    @NotBlank(message = "Email or username or required")
    private String emailOrUsername;
    @NotBlank(message = "Password is required")
    private String password;
}
