package com.paloma.paloma.javaServer.controllers;

import com.paloma.paloma.javaServer.dataTransferObjects.requests.RefreshRequest;
import com.paloma.paloma.javaServer.dataTransferObjects.responses.JwtResponse;
import com.paloma.paloma.javaServer.entities.User;
import com.paloma.paloma.javaServer.repositories.UserRepository;
import com.paloma.paloma.javaServer.services.RefreshService;
import com.paloma.paloma.javaServer.utilites.JwtUtil;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/insession")
@RequiredArgsConstructor
public class InSessionController {


    private final RefreshService refreshService;
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;



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

    @DeleteMapping ("/logout")
    public ResponseEntity<?> logout(@RequestHeader("Authorization") String authHeader) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpServletResponse.SC_UNAUTHORIZED).build();
            }
            
            // Extract the token from the Authorization header
            String token = authHeader.substring(7);
            
            // Extract the user ID from the access token and revoke all refresh tokens
            try {
                UUID userId = jwtUtil.validateTokenAndGetUserId(token);
                Optional<User> userOptional = userRepository.findById(userId);
                
                if (userOptional.isPresent()) {
                    User user = userOptional.get();
                    refreshService.revokeTokens(user);
                    return ResponseEntity.ok("Successfully logged out and revoked all refresh tokens");
                } else {
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
                }
            } catch (Exception e) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid access token");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpServletResponse.SC_UNAUTHORIZED).body("Invalid token");
        }
    }
}
