package com.paloma.paloma.javaServer.controllers;

import com.paloma.paloma.javaServer.dataTransferObjects.requests.RegisterRequest;
import com.paloma.paloma.javaServer.dataTransferObjects.responses.RegisterResponse;
import com.paloma.paloma.javaServer.entities.AuthCred;
import com.paloma.paloma.javaServer.exceptions.UserException;
import com.paloma.paloma.javaServer.repositories.AuthCredRepository;
import com.paloma.paloma.javaServer.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class RegisterController {

//    private final UserService userService;
//    private final AuthCredRepository authCredRepository;
//    private final PasswordEncoder passwordEncoder;
//
//    @PostMapping("/register")
//    public ResponseEntity<RegisterResponse> register(@RequestBody RegisterRequest request) {
//        try {
//            // Register the user (validation + saving basic user info)
//            RegisterResponse response = userService.register(request);
//
//            // Save hashed password credentials
//            String hashedPassword = passwordEncoder.encode(request.getPassword());
//            AuthCred authCred = new AuthCred(response.getUser().getId(), hashedPassword);
//            authCredRepository.save(authCred);
//
//            return ResponseEntity.status(HttpStatus.CREATED).body(response);
//        } catch (UserException | UserException e) {
//            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
//                    new RegisterResponse(e.getMessage())
//            );
//        }
    }

