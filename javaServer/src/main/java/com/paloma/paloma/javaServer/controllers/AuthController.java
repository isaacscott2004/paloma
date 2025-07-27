package com.paloma.paloma.javaServer.controllers;

import com.paloma.paloma.javaServer.dataTransferObjects.requests.LoginRequest;
import com.paloma.paloma.javaServer.dataTransferObjects.responses.JwtResponse;
import com.paloma.paloma.javaServer.exceptions.AuthenticationException;
import com.paloma.paloma.javaServer.services.AuthenticationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private static final String INVALID_CREDENTIALS_MESSAGE = "Invalid credentials";

    private final AuthenticationService authenticationService;

    public AuthController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @PostMapping("/login")
    public ResponseEntity<JwtResponse> login(@RequestBody LoginRequest request) {
        try {
            String token = authenticationService.authenticateUser(request);
            return ResponseEntity.ok(new JwtResponse(token));
        } catch (AuthenticationException e) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(new JwtResponse(INVALID_CREDENTIALS_MESSAGE));
        }
    }
}

