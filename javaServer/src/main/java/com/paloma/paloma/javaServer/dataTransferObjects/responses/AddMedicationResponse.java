package com.paloma.paloma.javaServer.dataTransferObjects.responses;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class AddMedicationResponse {
    private boolean success;
    private String message;
}
