package com.paloma.paloma.javaServer.dataTransferObjects.requests;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddMedicationRequest {
    private String medicationName;
    private String dosage;
    private String schedule;


}
