package com.paloma.paloma.javaServer.dataTransferObjects.responses;

import com.paloma.paloma.javaServer.entities.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RegisterResponse {
    private User user;
    private String message;



}
