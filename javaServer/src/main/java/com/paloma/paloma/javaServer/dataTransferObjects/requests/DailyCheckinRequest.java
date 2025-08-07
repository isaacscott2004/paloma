package com.paloma.paloma.javaServer.dataTransferObjects.requests;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
@AllArgsConstructor
@NoArgsConstructor
@Data
public class DailyCheckinRequest {
    private Integer moodScore;
    private Integer energyScore;
    private Integer motivationScore;
    private Integer suicidalScore;
    private String notes;





}
