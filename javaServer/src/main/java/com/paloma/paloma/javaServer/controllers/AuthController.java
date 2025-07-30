package com.paloma.paloma.javaServer.controllers;

import com.paloma.paloma.javaServer.dataTransferObjects.requests.LoginRequest;
import com.paloma.paloma.javaServer.dataTransferObjects.requests.LogoutRequest;
import com.paloma.paloma.javaServer.dataTransferObjects.requests.RefreshRequest;
import com.paloma.paloma.javaServer.dataTransferObjects.requests.RegisterRequest;
import com.paloma.paloma.javaServer.dataTransferObjects.responses.JwtResponse;
import com.paloma.paloma.javaServer.dataTransferObjects.responses.RegisterResponse;
import com.paloma.paloma.javaServer.entities.AuthCred;
import com.paloma.paloma.javaServer.exceptions.AuthenticationException;
import com.paloma.paloma.javaServer.exceptions.UserException;
import com.paloma.paloma.javaServer.repositories.AuthCredRepository;
import com.paloma.paloma.javaServer.services.RefreshService;
import com.paloma.paloma.javaServer.services.UserService;
import com.paloma.paloma.javaServer.utilites.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private static final String INVALID_CREDENTIALS_MESSAGE = "Invalid credentials";

    private final UserService userService;
    private final RefreshService refreshService;
    private final AuthCredRepository authCredRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;


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
                    new RegisterResponse(e.getMessage())
            );
        }
    }

    @PostMapping("/login")
    public ResponseEntity<JwtResponse> login(@RequestBody LoginRequest request) {
        try {
            String token = userService.login(request).getAccessAuthToken();
            return ResponseEntity.ok(new JwtResponse(token));
        } catch (AuthenticationException e) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(new JwtResponse(INVALID_CREDENTIALS_MESSAGE));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestBody LogoutRequest request) {
        refreshService.revokeTokens(request.getUser());
        return ResponseEntity.ok("Logout successful");
    }

    @PostMapping("/refresh")
    public ResponseEntity<JwtResponse> refresh(@RequestBody RefreshRequest request) {
        String refreshToken = request.getRefreshToken();

        try {
            return refreshService.validate(refreshToken)
                    .map(user -> {
                        String newAccessToken = jwtUtil.generateAccessToken(user.getId());
                        return ResponseEntity.ok(new JwtResponse(newAccessToken));
                    })
                    .orElse(ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                            .body(new JwtResponse("Invalid refresh token")));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new JwtResponse("Invalid refresh token"));
        }
    }







}

