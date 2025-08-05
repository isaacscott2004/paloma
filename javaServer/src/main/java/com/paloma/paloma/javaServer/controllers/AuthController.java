package com.paloma.paloma.javaServer.controllers;

import com.paloma.paloma.javaServer.dataTransferObjects.requests.LoginRequest;
import com.paloma.paloma.javaServer.dataTransferObjects.requests.RegisterRequest;
import com.paloma.paloma.javaServer.dataTransferObjects.responses.JwtResponse;
import com.paloma.paloma.javaServer.dataTransferObjects.responses.LoginResponse;
import com.paloma.paloma.javaServer.dataTransferObjects.responses.RegisterResponse;
import com.paloma.paloma.javaServer.entities.AuthCred;
import com.paloma.paloma.javaServer.exceptions.AuthenticationException;
import com.paloma.paloma.javaServer.exceptions.UserException;
import com.paloma.paloma.javaServer.repositories.AuthCredRepository;
import com.paloma.paloma.javaServer.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for handling authentication-related operations.
 * This controller provides endpoints for user registration and login.
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private static final String INVALID_CREDENTIALS_MESSAGE = "Invalid credentials";

    private final UserService userService;
    private final AuthCredRepository authCredRepository;
    private final PasswordEncoder passwordEncoder;


    /**
     * Registers a new user in the system.
     * 
     * @param request The registration request containing user details
     * @return ResponseEntity with the registered user information or error message
     */
    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(@RequestBody RegisterRequest request) {
        try {
            RegisterResponse response = userService.register(request);

            String hashedPassword = passwordEncoder.encode(request.getPassword());
            AuthCred authCred = new AuthCred();

            authCred.setUser(response.getUser());
            authCred.setPasswordHash(hashedPassword);
            authCred.setCreatedAt(response.getUser().getCreatedAt());
            authCredRepository.save(authCred);

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (UserException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    new RegisterResponse(null, e.getMessage())
            );
        }
    }

    /**
     * Authenticates a user and returns a JWT token.
     * 
     * @param request The login request containing user credentials
     * @return ResponseEntity with the JWT token or error message
     */
    @PostMapping("/login")
    public ResponseEntity<JwtResponse> login(@RequestBody LoginRequest request) {
        try {
            LoginResponse loginResponse = userService.login(request);
            return ResponseEntity.ok(new JwtResponse(loginResponse.getAccessToken(), loginResponse.getMessage()));
        } catch (AuthenticationException e) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(new JwtResponse(INVALID_CREDENTIALS_MESSAGE));
        }
    }



}




