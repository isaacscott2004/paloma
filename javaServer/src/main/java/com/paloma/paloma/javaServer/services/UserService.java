package com.paloma.paloma.javaServer.services;

import com.paloma.paloma.javaServer.dataTransferObjects.requests.LoginRequest;
import com.paloma.paloma.javaServer.dataTransferObjects.requests.RegisterRequest;
import com.paloma.paloma.javaServer.dataTransferObjects.responses.AddContactResponse;
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

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static com.paloma.paloma.javaServer.entities.enums.RoleType.USER;

/**
 * Service for managing user-related operations.
 * This service provides methods for user registration, authentication, profile management,
 * and trusted contact management. It interacts with various repositories to persist user data
 * and uses other services for sending notifications.
 */
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

    private final TrustedContactRepository trustedContactRepository;

    private final EmailService emailService;

    private final SMSService smsService;

    private final JwtUtil jwtUtil;



    /**
     * Registers a new user in the system.
     * Validates that the email, phone, and username are not already in use.
     * Creates a new user, assigns the requested role, and sets up the user-role relationship.
     * 
     * @param request The registration request containing user details
     * @return RegisterResponse with the registered user information
     * @throws UserException If the email, phone, or username is already in use
     */
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
        try {
            userRepository.save(user);
        } catch (Exception e) {
            throw new RuntimeException("User save failed: " + e.getMessage());
        }

        Role role = new Role();
        role.setRoleType(request.getRoleType());
        roleRepository.save(role);

        UserRole userRole = new UserRole();
        boolean primary = request.getRoleType().equals(USER);
        userRole.setUser(user);
        userRole.setRole(role);
        userRole.setPrimary(primary);
        userRoleRepository.save(userRole);

        return new RegisterResponse(user,
                "Successfully registered user with role: " + request.getRoleType().name());
    }

    /**
     * Authenticates a user and generates access and refresh tokens.
     * Updates the user's last login time and creates a new refresh token.
     * 
     * @param request The login request containing user credentials
     * @return LoginResponse with access and refresh tokens
     * @throws AuthenticationException If the credentials are invalid
     */
    public LoginResponse login(LoginRequest request) throws AuthenticationException {
        String emailOrUsername = request.getEmailOrUsername();
        User user = userRepository.findByEmailOrUsername(emailOrUsername, emailOrUsername).orElseThrow(() ->
                new AuthenticationException("Invalid credentials"));
        AuthCred auth = authCredentialsRepository.findByUserId(user.getId());
        if (!passwordEncoder.matches(request.getPassword(), auth.getPasswordHash())) {
            throw new AuthenticationException("Invalid password");
        }
        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);
        String refreshToken = jwtUtil.generateRefreshToken(user.getId());
        RefreshAuth refreshAuth = new RefreshAuth();
        refreshAuth.setUser(user);
        refreshAuth.setToken(refreshToken);
        refreshAuth.setExpiryDate(LocalDateTime.now().plusDays(7));
        refreshAuthRepository.save(refreshAuth);
        return new LoginResponse(jwtUtil.generateAccessToken(user.getId()), "Successfully logged in");
    }

    /**
     * Retrieves a user by their ID.
     * 
     * @param userId The ID of the user to retrieve
     * @return Optional containing the user if found, empty otherwise
     */
    public Optional<User> getUserById(UUID userId) {
       return userRepository.findById(userId);
    }

    /**
     * Updates a user's password.
     * Verifies that the old password is correct before updating.
     * 
     * @param user The user whose password is being updated
     * @param oldPassword The user's current password
     * @param newPassword The new password
     * @throws AuthenticationException If the old password is incorrect
     */
    public void updatePassword(User user, String oldPassword, String newPassword) throws AuthenticationException {
        AuthCred auth = authCredentialsRepository.findByUserId(user.getId());
        if (!passwordEncoder.matches(oldPassword, auth.getPasswordHash())) {
            throw new AuthenticationException("Invalid password");
        }
        auth.setPasswordHash(passwordEncoder.encode(newPassword));
        authCredentialsRepository.save(auth);
    }

    /**
     * Updates a user's email address.
     * 
     * @param user The user whose email is being updated
     * @param newEmail The new email address
     */
    public void updateEmail(User user, String newEmail) {
        user.setEmail(newEmail);
        userRepository.save(user);
    }

    /**
     * Updates a user's username.
     * 
     * @param user The user whose username is being updated
     * @param newUsername The new username
     */
    public void updateUsername(User user, String newUsername)  {
        user.setUsername(newUsername);
        userRepository.save(user);
    }

    /**
     * Updates a user's phone number.
     * 
     * @param user The user whose phone number is being updated
     * @param newPhone The new phone number
     */
    public void updatePhone(User user, String newPhone)  {
        user.setPhone(newPhone);
        userRepository.save(user);
    }

    /**
     * Adds a trusted contact for a user.
     * If the contact already exists in the system, a relationship is created.
     * If the contact doesn't exist, a new user is created and invitations are sent via email and SMS.
     * 
     * @param user The user adding the contact
     * @param contactEmail The email address of the contact
     * @param contactPhone The phone number of the contact
     * @param messageOnNotify The message to send to the contact when an alert is triggered
     * @return AddContactResponse with the result of the operation
     */
    public AddContactResponse addContact(User user, String contactEmail, String contactPhone,
                                         String messageOnNotify){
        // Check if the contact already exists in the system
        Optional<User> contactUserOptional = userRepository.findByEmailOrUsername(contactEmail, contactEmail);

        
        if(contactUserOptional.isPresent()){
            // Case 1: Contact exists in the system
            User contactUser = contactUserOptional.get();
            
            // Create a trusted contact relationship
            TrustedContact trustedContact = new TrustedContact();
            trustedContact.setUser(user);
            trustedContact.setContactUser(contactUser);
            trustedContact.setMessageOnNotify(messageOnNotify);
            
            try {
                trustedContactRepository.save(trustedContact);
                return new AddContactResponse(true, "Contact added successfully", true);
            } catch (Exception e) {
                return new AddContactResponse(false, "Failed to add contact: " + e.getMessage(), true);
            }
        } else {
            try {
                boolean emailSent = emailService.sendInvitationEmail(contactEmail, user.getFullName(), messageOnNotify);
                boolean smsSent = smsService.sendInvitationSMS(contactPhone, user.getFullName(), messageOnNotify);
                
                return new AddContactResponse(true, "Contact added and invitations sent. Please " +
                        "re-add Contact when they have created an account", false, emailSent, smsSent);
            } catch (Exception e) {
                return new AddContactResponse(false, "Failed to add contact: " + e.getMessage(), false);
            }
        }
    }




}
