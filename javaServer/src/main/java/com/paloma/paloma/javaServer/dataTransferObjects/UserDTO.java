package com.paloma.paloma.javaServer.dataTransferObjects;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
    private UUID id;

    private String username;

    private String email;

    private Integer phone;

    private String fullName;

    private LocalDateTime createdAt;

    private LocalDateTime lastLogin;
}
