package com.paloma.paloma.javaServer.services;

import com.paloma.paloma.javaServer.dataTransferObjects.requests.AddRoleRequest;
import com.paloma.paloma.javaServer.dataTransferObjects.requests.LoginRequest;
import com.paloma.paloma.javaServer.dataTransferObjects.requests.RegisterRequest;
import com.paloma.paloma.javaServer.dataTransferObjects.responses.*;
import com.paloma.paloma.javaServer.entities.*;
import com.paloma.paloma.javaServer.entities.enums.RoleType;
import com.paloma.paloma.javaServer.entities.enums.SensitivityLevel;
import com.paloma.paloma.javaServer.exceptions.AuthenticationException;
import com.paloma.paloma.javaServer.exceptions.NoDailyCheckinsFoundException;
import com.paloma.paloma.javaServer.exceptions.UserException;
import com.paloma.paloma.javaServer.repositories.*;
import com.paloma.paloma.javaServer.utilites.JwtUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.data.web.SpringDataWebProperties;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
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

    private final RoleManagementService roleManagementService;

    private final DailyCheckinRepository dailyCheckinRepository;

    private final AlertSensitivityRepository alertSensitivityRepository;

    private final MedicationRepository medicationRepository;

    private final MedLogRepository medLogRepository;

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
    @Transactional
    public RegisterResponse register(RegisterRequest request) throws UserException {
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setFullName(request.getFullName());

        if (userRepository.existsByEmail(user.getEmail())){
            throw new UserException("There is already an account with the email address: " + user.getEmail());
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
    @Transactional
    public LoginResponse login(LoginRequest request) throws AuthenticationException {
        String identifier = request.getEmailOrUsername();

        User user = userRepository.findByEmailOrUsername(identifier, identifier)
                .orElseThrow(() -> new AuthenticationException("Invalid credentials"));

        AuthCred auth = authCredentialsRepository.findByUserId(user.getId());
        if (auth == null || !passwordEncoder.matches(request.getPassword(), auth.getPasswordHash())) {
            throw new AuthenticationException("Invalid password");
        }

        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user); 

        handleRefreshToken(user);

        String accessToken = jwtUtil.generateAccessToken(user.getId());
        return new LoginResponse(accessToken, "Welcome " + user.getFullName() + "!");
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
    @Transactional
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
    @Transactional
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
    @Transactional
    public void updateUsername(User user, String newUsername)  {
        user.setUsername(newUsername);
        userRepository.save(user);
    }

    @Transactional
    public UpdateAlertSensitivityResponse updateSensitivity(User user, SensitivityLevel sensitivity){
        try {
            AlertSensitivity alertSensitivity = alertSensitivityRepository.getReferenceById(user.getId());
            alertSensitivity.setSensitivityLevel(sensitivity);
            alertSensitivityRepository.save(alertSensitivity);
            return new UpdateAlertSensitivityResponse(true, "Alert sensitivity updated successfully");
        } catch (Exception e) {
            return new UpdateAlertSensitivityResponse(false, e.getMessage());
        }
    }

    /**
     * Adds a trusted contact for a user.
     * If the contact already exists in the system, a relationship is created.
     * If the contact doesn't exist, a new user is created and an invitation is sent via email.
     *
     * @param user The user adding the contact
     * @param contactEmail The email address of the contact
     * @param messageOnNotify The message to send to the contact when an alert is triggered
     * @return AddContactResponse with the result of the operation
     */
    @Transactional
    public AddContactResponse addContact(User user, String contactEmail, String messageOnNotify) {
        Optional<User> contactUserOptional = userRepository.findByEmail(contactEmail);

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
            return inviteNewContact(contactEmail, user.getFullName(), messageOnNotify);
        }
    }
    @Transactional
    public RemoveContactResponse removeContact(User user, String contactEmail) {
        Optional<User> optionalContactUser = userRepository.findByEmail(contactEmail);
        User contactUser;
        if (optionalContactUser.isPresent()) {
            contactUser = optionalContactUser.get();
        } else{
            return new RemoveContactResponse(false,"Contact not found");
        }
        Optional<TrustedContact> optionalTrustedContact =
                trustedContactRepository.findByUserIdAndContactUserId(user.getId(), contactUser.getId());
        if (optionalTrustedContact.isPresent()) {
            trustedContactRepository.delete(optionalTrustedContact.get());
            if (trustedContactRepository.findAllByContactUserId(contactUser.getId()).isEmpty()
                    && roleManagementService.userHasBothRoles(contactUser.getId())) {
                try {
                    roleManagementService.removeRoleFromUser(contactUser.getId(), TRUSTED_CONTACT);
                    
                } catch (UserException e) {
                    return new RemoveContactResponse(false,"Contact not found");
                }
            }
        } else {
            return new RemoveContactResponse(false,"Contact not found");
        }
        return new RemoveContactResponse(true, "Contact removed successfully");


    }

    @Transactional
    public DailyCheckinResponse dailyCheckin(User user, Integer moodScore, Integer energyScore,
                                             Integer motivationScore, Integer suicidalScore, String notes) {
        try {
            DailyCheckin dailyCheckin = new DailyCheckin(user, LocalDate.now(), moodScore,
                    energyScore, motivationScore, suicidalScore, notes);
            dailyCheckinRepository.save(dailyCheckin);
            
            // Create medication logs for all active medications
            List<Medication> activeMedications = medicationRepository.findByUserIdAndIsActiveTrue(user.getId());
            for (Medication medication : activeMedications) {
                MedLog medLog = new MedLog();
                medLog.setUser(user);
                medLog.setMedication(medication);
                medLog.setDate(LocalDate.now());
                medLog.setTaken(true);
                medLog.setCreatedAt(LocalDateTime.now());
                medLogRepository.save(medLog);
                medication.addMedLog(medLog);
            }
            
            return new DailyCheckinResponse(true, "Daily checkin recorded successfully");
        } catch (Exception e) {
            return new DailyCheckinResponse(false, e.getMessage());
        }
    }

    @Transactional
    public AddAlertSensitivityResponse addAlertSensitivity(User user, SensitivityLevel sensitivity) {
        try {
            AlertSensitivity alertSensitivity = new AlertSensitivity();
            alertSensitivity.setSensitivityLevel(sensitivity);
            alertSensitivity.setUser(user);
            alertSensitivityRepository.save(alertSensitivity);
            return new AddAlertSensitivityResponse(true, "Alert sensitivity added successfully");
        } catch (Exception e) {
            return new AddAlertSensitivityResponse(false, e.getMessage());
        }

    }

    @Transactional
    public GetOverallScoresResponse getOverallScores(User user, Integer days) {
        UUID userId = user.getId();
        Pageable pageable = PageRequest.of(0, days);
        List<Integer> latestOverallScores = dailyCheckinRepository.findLatestOverallScoresByUserId(userId, pageable);

        if (latestOverallScores.isEmpty()) {
            // No scores found — let controller return 204
            return new GetOverallScoresResponse(true, "No scores found", latestOverallScores);
        }

        if (latestOverallScores.size() < days) {
            return new GetOverallScoresResponse(true,
                    "Only " + latestOverallScores.size() + " days of scores were found.", latestOverallScores);
        }

        return new GetOverallScoresResponse(true, "Scores retrieved successfully", latestOverallScores);
    }

    @Transactional
    public AddMedicationResponse addMedication(User user, String medicationName, String dosage, String schedule){
        try{
            Medication medication = new Medication();
            medication.setUser(user);
            medication.setName(medicationName);
            medication.setDosage(dosage);
            medication.setDailySchedule(schedule);
            medication.setIsActive(true);
            medication.setCreatedAt(LocalDateTime.now());
            medicationRepository.save(medication);
            return new AddMedicationResponse(true, "Medication added successfully");
        } catch (Exception e) {
            return new AddMedicationResponse(false, "Failed to add medication " + e.getMessage());
        }

    }

    @Transactional
    public AddMedicationLogResponse addMedicationLog(User user, String medicationName){
        try{
            Optional<Medication> medicationOptional = medicationRepository.findByNameAndUserId(medicationName,
                    user.getId());
            if(medicationOptional.isPresent()){
                Medication medication = medicationOptional.get();
                MedLog medLog = new MedLog();
                medLog.setUser(user);
                medLog.setMedication(medication);
                medLog.setDate(LocalDate.now());
                medLog.setTaken(true);
                medLog.setCreatedAt(LocalDateTime.now());
                medLogRepository.save(medLog);
                medication.addMedLog(medLog);
                return new AddMedicationLogResponse(true, "Medication log added successfully");
            } else {
                return new AddMedicationLogResponse(false, "Medication not found");
            }

        } catch (Exception e) {
            return new AddMedicationLogResponse(false, "Failed to add medication log " + e.getMessage());
        }
    }


    private AddContactResponse saveTrustedContact(User user, User contactUser, String messageOnNotify) {
        try {
            TrustedContact trustedContact = new TrustedContact();
            trustedContact.setUser(user);
            trustedContact.setContactUser(contactUser);
            trustedContact.setMessageOnNotify(messageOnNotify);
            trustedContactRepository.save(trustedContact);

            // Create medication logs for all active medications
            List<Medication> activeMedications = medicationRepository.findByUserIdAndIsActiveTrue(user.getId());
            for (Medication medication : activeMedications) {
                MedLog medLog = new MedLog();
                medLog.setUser(user);
                medLog.setMedication(medication);
                medLog.setDate(LocalDate.now());
                medLog.setTaken(true);
                medLog.setCreatedAt(LocalDateTime.now());
                medLogRepository.save(medLog);
                medication.addMedLog(medLog);
            }

            return new AddContactResponse(true, "Contact added successfully", true);
        } catch (Exception e) {
            return new AddContactResponse(false, "Failed to add contact: " + e.getMessage(), true);
        }
    }

    private AddContactResponse inviteNewContact(String email, String senderName, String messageOnNotify) {
        try {
            boolean emailSent = emailService.sendInvitationEmail(email, senderName, messageOnNotify);

            return new AddContactResponse(
                    true,
                    "Contact added and invitation email sent. Please re-add contact when they have created an account",
                    false,
                    emailSent
            );
        } catch (Exception e) {
            return new AddContactResponse(false, "Failed to add contact: " + e.getMessage(), false);
        }
    }

    private void handleRefreshToken(User user) {
        Optional<RefreshAuth> existing = refreshAuthRepository.findByUserId(user.getId());

        if (existing.isEmpty() || existing.get().getExpiryDate().isBefore(LocalDateTime.now())) {
            existing.ifPresent(refreshAuthRepository::delete);

            RefreshAuth newToken = new RefreshAuth();
            newToken.setUser(user);
            newToken.setToken(jwtUtil.generateRefreshToken(user.getId()));
            newToken.setExpiryDate(LocalDateTime.now().plusDays(7));

            refreshAuthRepository.save(newToken);
        }
    }





}
