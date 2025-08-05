package com.paloma.paloma.javaServer.dataTransferObjects.responses;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class JwtResponse {
    private String token;
    private String message;
    
    public JwtResponse(String token) {
        this.token = token;
    }
}
