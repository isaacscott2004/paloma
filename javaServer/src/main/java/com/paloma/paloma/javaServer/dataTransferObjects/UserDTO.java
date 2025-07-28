package com.paloma.paloma.javaServer.dataTransferObjects;

import com.paloma.paloma.javaServer.entities.RoleType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
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

    private List<RoleType> roles;
}
