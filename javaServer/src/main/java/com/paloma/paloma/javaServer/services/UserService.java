package com.paloma.paloma.javaServer.services;

import com.paloma.paloma.javaServer.dataTransferObjects.requests.AddRoleRequest;
import com.paloma.paloma.javaServer.dataTransferObjects.requests.LoginRequest;
import com.paloma.paloma.javaServer.dataTransferObjects.requests.RegisterRequest;
import com.paloma.paloma.javaServer.dataTransferObjects.responses.AddContactResponse;
import com.paloma.paloma.javaServer.dataTransferObjects.responses.LoginResponse;
import com.paloma.paloma.javaServer.dataTransferObjects.responses.RegisterResponse;
import com.paloma.paloma.javaServer.dataTransferObjects.responses.RemoveContactResponse;
import com.paloma.paloma.javaServer.entities.*;
import com.paloma.paloma.javaServer.entities.enums.RoleType;
import com.paloma.paloma.javaServer.exceptions.AuthenticationException;
import com.paloma.paloma.javaServer.exceptions.UserException;
import com.paloma.paloma.javaServer.repositories.*;
import com.paloma.paloma.javaServer.utilites.JwtUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.paloma.paloma.javaServer.entities.enums.RoleType.TRUSTED_CONTACT;
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

    private final RoleManagementService roleManagementService;

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
        // add user to db
        userRepository.save(user);

        String hashedPassword = passwordEncoder.encode(request.getPassword());
        AuthCred authCred = new AuthCred();

        // add credentials to db
        authCred.setUser(user);
        authCred.setPasswordHash(hashedPassword);
        authCred.setCreatedAt(user.getCreatedAt());
        authCredentialsRepository.save(authCred);

        // add role infromation to db
        Role role = new Role();
        role.setRoleType(request.getRoleType());
        roleRepository.save(role);

        UserRole userRole = new UserRole();
        boolean primary = request.getRoleType().equals(USER);
        userRole.setUser(user);
        userRole.setRole(role);
        userRole.setPrimary(primary);
        userRoleRepository.save(userRole);


        return new RegisterResponse(
                "Successfully registered " + request.getFullName() + " with role: " + request.getRoleType().name());
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
        return new LoginResponse(jwtUtil.generateAccessToken(user.getId()), "Welcome " + user.getFullName() + "!");
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
    public AddContactResponse addContact(User user, String contactEmail, String contactPhone, String messageOnNotify) {
        Optional<User> contactUserOptional = userRepository.findByEmailOrUsername(contactEmail, contactEmail);

        if (contactUserOptional.isPresent()) {
            User contactUser = contactUserOptional.get();
            List<RoleType> roles = userRoleRepository.findRoleTypesByUser(contactUser);

            // If the contact does not have TRUSTED_CONTACT role, add it
            if (!roles.contains(RoleType.TRUSTED_CONTACT)) {
                AddRoleRequest addRoleRequest = new AddRoleRequest(user.getId(), RoleType.TRUSTED_CONTACT, false);
                try {
                    roleManagementService.addRoleToUser(addRoleRequest);
                } catch (UserException e) {
                    return new AddContactResponse(false, "Failed to add role: " + e.getMessage(), true);
                }
            }

            // Now create the trusted contact relationship
            return saveTrustedContact(user, contactUser, messageOnNotify);

        } else {
            // Contact not found — invite them and tell the user to re-add later
            return inviteNewContact(contactEmail, contactPhone, user.getFullName(), messageOnNotify);
        }
    }

    public RemoveContactResponse removeContact(User user, User contactUser) {

                Optional<TrustedContact> optionalTrustedContact =
                        trustedContactRepository.findByUserIdAndContactUserId(user.getId(), contactUser.getId());
                optionalTrustedContact.ifPresent(trustedContactRepository::delete);
                if(optionalTrustedContact.isEmpty()){
                    return new RemoveContactResponse("Contact not found");
                }
                return new RemoveContactResponse("Contact removed successfully");



    }

    private AddContactResponse saveTrustedContact(User user, User contactUser, String messageOnNotify) {
        try {
            TrustedContact trustedContact = new TrustedContact();
            trustedContact.setUser(user);
            trustedContact.setContactUser(contactUser);
            trustedContact.setMessageOnNotify(messageOnNotify);
            trustedContactRepository.save(trustedContact);

            return new AddContactResponse(true, "Contact added successfully", true);
        } catch (Exception e) {
            return new AddContactResponse(false, "Failed to add contact: " + e.getMessage(), true);
        }
    }

    private AddContactResponse inviteNewContact(String email, String phone, String senderName, String messageOnNotify) {
        try {
            boolean emailSent = emailService.sendInvitationEmail(email, senderName, messageOnNotify);
            boolean smsSent = smsService.sendInvitationSMS(phone, senderName, messageOnNotify);

            return new AddContactResponse(
                    true,
                    "Contact added and invitations sent. Please re-add contact when they have created an account",
                    false,
                    emailSent,
                    smsSent
            );
        } catch (Exception e) {
            return new AddContactResponse(false, "Failed to add contact: " + e.getMessage(), false);
        }
    }





}
