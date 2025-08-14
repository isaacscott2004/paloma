package com.paloma.paloma.javaServer.dataTransferObjects.requests;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class UpdateMedicationRequest {
    private String oldMedicationName;
    private String newMedicationName;
    private String dosage;
    private String schedule;
}
