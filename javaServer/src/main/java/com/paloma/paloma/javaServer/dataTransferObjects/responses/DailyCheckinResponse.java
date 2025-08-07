package com.paloma.paloma.javaServer.dataTransferObjects.responses;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DailyCheckinResponse {
    private boolean success;
    private String message;
}
