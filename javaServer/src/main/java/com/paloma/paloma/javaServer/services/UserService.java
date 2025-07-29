package com.paloma.paloma.javaServer.services;

import com.paloma.paloma.javaServer.dataTransferObjects.requests.LoginRequest;
import com.paloma.paloma.javaServer.dataTransferObjects.requests.RegisterRequest;
import com.paloma.paloma.javaServer.dataTransferObjects.responses.LoginResponse;
import com.paloma.paloma.javaServer.dataTransferObjects.responses.RegisterResponse;
import com.paloma.paloma.javaServer.entities.*;
import com.paloma.paloma.javaServer.exceptions.AuthenticationException;
import com.paloma.paloma.javaServer.exceptions.UserException;
import com.paloma.paloma.javaServer.repositories.*;
import com.paloma.paloma.javaServer.utilites.JwtUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import static com.paloma.paloma.javaServer.entities.enums.RoleType.USER;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;

    private final AuthCredRepository authCredentialsRepository;

    private final PasswordEncoder passwordEncoder;

    private final RoleRepository roleRepository;

    private final UserRoleRepository userRoleRepository;

    private final RefreshAuthRepository refreshAuthRepository;

    private final JwtUtil jwtUtil;



public RegisterResponse register(RegisterRequest request) throws UserException {
    User user = new User();
    user.setUsername(request.getUsername());
    user.setEmail(request.getEmail());
    user.setPhone(request.getPhone());
    user.setFullName(request.getFullName());

    if (userRepository.existsByEmail(user.getEmail())){
        throw new UserException("There is already an account with the email address: " + user.getEmail());
    }
    if (userRepository.existsByPhone(user.getPhone())){
        throw new UserException("There is already an account with the phone number: " + user.getPhone());
    }
    if (userRepository.existsByUsername(user.getUsername())){
        throw new UserException("There is already an account with the username: " + user.getUsername());
    }
    user.setCreatedAt(java.time.LocalDateTime.now());
    userRepository.save(user);

    Role role = new Role();
    role.setRoleType(request.getRoleType());
    roleRepository.save(role);

    UserRole userRole = new UserRole();
    boolean primary = request.getRoleType().equals(USER);
    userRole.setUser(user);
    userRole.setRole(role);
    userRole.setPrimary(primary);
    userRoleRepository.save(userRole);

    return new RegisterResponse(user);
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
    String refreshToken = jwtUtil.generateRefreshToken(user.getId());
    RefreshAuth refreshAuth = new RefreshAuth();
    refreshAuth.setUser(user);
    refreshAuth.setToken(refreshToken);
    refreshAuth.setExpiryDate(java.time.LocalDateTime.now().plusDays(7));
    refreshAuthRepository.save(refreshAuth);
    return new LoginResponse(jwtUtil.generateAccessToken(user.getId()), refreshToken);
}



//public LogoutResponse logout(LogoutRequest request){
//
//}

}
