package com.paloma.paloma.javaServer.dataTransferObjects.responses;

import com.paloma.paloma.javaServer.dataTransferObjects.UserDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginResponse {
    private UserDTO user;
    private String token;
}
