package com.paloma.paloma.javaServer.dataTransferObjects.responses;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RefreshResponse {
    private String accessToken;
}
