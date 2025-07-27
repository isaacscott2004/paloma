package com.paloma.paloma.javaServer.services;

import com.paloma.paloma.javaServer.dataTransferObjects.requests.LoginRequest;
import com.paloma.paloma.javaServer.entities.AuthCred;
import com.paloma.paloma.javaServer.entities.User;
import com.paloma.paloma.javaServer.exceptions.AuthenticationException;
import com.paloma.paloma.javaServer.repositories.AuthCredRepository;
import com.paloma.paloma.javaServer.repositories.UserRepository;
import com.paloma.paloma.javaServer.utilites.JwtUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthenticationService {
    private final UserRepository userRepository;
    private final AuthCredRepository authCredentialsRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthenticationService(
            UserRepository userRepository,
            AuthCredRepository authCredentialsRepository,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.authCredentialsRepository = authCredentialsRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public String authenticateUser(LoginRequest request) throws AuthenticationException {
        if (!userRepository.existsByEmail(request.getEmail())) {
            throw new AuthenticationException("User not found");
        }
        User user = userRepository.findByEmail(request.getEmail());

        if(!authCredentialsRepository.existsByUserId(user.getId())){
            throw new AuthenticationException("Auth credentials not found");
        }
        AuthCred auth = authCredentialsRepository.findByUserId(user.getId());


        if (!passwordEncoder.matches(request.getPassword(), auth.getPasswordHash())) {
            throw new AuthenticationException("Invalid password");
        }

        return JwtUtil.generateToken(user.getId());
    }
}

