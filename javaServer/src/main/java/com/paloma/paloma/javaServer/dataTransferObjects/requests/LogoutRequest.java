package com.paloma.paloma.javaServer.dataTransferObjects.requests;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class LogoutRequest {
    private String refreshToken;
}
