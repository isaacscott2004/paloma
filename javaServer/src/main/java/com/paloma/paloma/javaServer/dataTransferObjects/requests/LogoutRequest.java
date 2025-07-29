package com.paloma.paloma.javaServer.dataTransferObjects.requests;

import com.paloma.paloma.javaServer.entities.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class LogoutRequest {

    private User user;
}
