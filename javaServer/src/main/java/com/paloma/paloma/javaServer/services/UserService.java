package com.paloma.paloma.javaServer.services;

import com.paloma.paloma.javaServer.dataTransferObjects.UserDTO;
import com.paloma.paloma.javaServer.dataTransferObjects.requests.LoginRequest;
import com.paloma.paloma.javaServer.dataTransferObjects.requests.RegisterRequest;
import com.paloma.paloma.javaServer.dataTransferObjects.responses.LoginResponse;
import com.paloma.paloma.javaServer.dataTransferObjects.responses.RegisterResponse;
import com.paloma.paloma.javaServer.entities.AuthCred;
import com.paloma.paloma.javaServer.entities.RoleType;
import com.paloma.paloma.javaServer.entities.User;
import com.paloma.paloma.javaServer.exceptions.AuthenticationException;
import com.paloma.paloma.javaServer.exceptions.UserException;
import com.paloma.paloma.javaServer.repositories.AuthCredRepository;
import com.paloma.paloma.javaServer.repositories.UserRepository;
import com.paloma.paloma.javaServer.utilites.JwtUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;

    private final AuthCredRepository authCredentialsRepository;

    private final PasswordEncoder passwordEncoder;


    public RegisterResponse register(RegisterRequest request) throws UserException {
        User user = request.getUser();
        if (userRepository.existsByEmail(user.getEmail())){
            throw new UserException("There is already an account with the email address: " + user.getEmail());
        }
        if (userRepository.existsByPhone(user.getPhone())){
            throw new UserException("There is already an account with the phone number: " + user.getPhone());
        }
        if (userRepository.existsByUsername(user.getUsername())){
            throw new UserException("There is already an account with the username: " + user.getUsername());
        }
        User savedUser = userRepository.save(user);
        UserDTO userDTO = convertToDTO(savedUser);
        return new RegisterResponse(userDTO);
    }


public LoginResponse login(LoginRequest request) throws AuthenticationException {
    String emailOrUsernameOrPhone = request.getEmailOrUsername();
    User user = userRepository.findByEmailOrUsername(emailOrUsernameOrPhone, emailOrUsernameOrPhone).orElseThrow(() ->
            new AuthenticationException("Invalid credentials"));
    AuthCred auth = authCredentialsRepository.findByUserId(user.getId());
    if (!passwordEncoder.matches(request.getPassword(), auth.getPasswordHash())) {
        throw new AuthenticationException("Invalid password");
    }
    user.setLastLogin(java.time.LocalDateTime.now());
    userRepository.save(user);
    return new LoginResponse(convertToDTO(user), JwtUtil.generateToken(user.getId()));
}


    private UserDTO convertToDTO(User user){
        UserDTO userDTO = new UserDTO();
        userDTO.setId(user.getId());
        userDTO.setEmail(user.getEmail());
        userDTO.setUsername(user.getUsername());
        userDTO.setPhone(user.getPhone());
        userDTO.setCreatedAt(user.getCreatedAt());
        userDTO.setLastLogin(user.getLastLogin());

        if(user.getUserRoles() != null && !user.getUserRoles().isEmpty()){
            List<RoleType> roles = user.getUserRoles().stream().map(ur -> ur.getRole().getRoleType()).toList();
            userDTO.setRoles(roles);
        }
        return userDTO;
    }


}
