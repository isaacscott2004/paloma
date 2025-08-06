package com.paloma.paloma.javaServer.controllers;

import com.paloma.paloma.javaServer.controllers.accessToken.GetAccessToken;
import com.paloma.paloma.javaServer.dataTransferObjects.requests.*;
import com.paloma.paloma.javaServer.dataTransferObjects.responses.AddContactResponse;
import com.paloma.paloma.javaServer.dataTransferObjects.responses.JwtResponse;
import com.paloma.paloma.javaServer.dataTransferObjects.responses.UserRolesResponse;
import com.paloma.paloma.javaServer.entities.RefreshAuth;
import com.paloma.paloma.javaServer.entities.User;
import com.paloma.paloma.javaServer.exceptions.UnauthorizedException;
import com.paloma.paloma.javaServer.exceptions.UserException;
import com.paloma.paloma.javaServer.services.RefreshService;
import com.paloma.paloma.javaServer.services.UserService;
import com.paloma.paloma.javaServer.utilites.JwtUtil;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

/**
 * Controller for handling operations that require an authenticated user session.
 * This controller provides endpoints for user profile management, token refresh,
 * logout, and trusted contact management.
 */
@RestController
@RequestMapping("/insession")
@RequiredArgsConstructor
public class InSessionController {


    private final RefreshService refreshService;
    private final JwtUtil jwtUtil;
    private final UserService userService;


    /**
     * Refreshes the user's access token using their refresh token.
     * 
     * @param authHeader The Authorization header containing the current access token
     * @return ResponseEntity with a new access token or error message
     */
    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@RequestHeader("Authorization") String authHeader) {
        try {
            String token = GetAccessToken.getAccessToken(authHeader);
            UUID userId = jwtUtil.validateTokenAndGetUserId(token);
            RefreshAuth refreshAuth = refreshService.findByUserID(userId)
                    .orElseThrow(() -> new UnauthorizedException("Invalid refresh token"));

            String refreshToken = refreshAuth.getToken();
            try {
                return refreshService.validate(refreshToken)
                        .map(user -> {
                            String newAccessToken = jwtUtil.generateAccessToken(user.getId());
                            return ResponseEntity
                                    .ok(new JwtResponse(newAccessToken, "Successfully refreshed token"));
                        })
                        .orElse(ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                                .body(new JwtResponse(null,"Invalid refresh token")));
            } catch (Exception e) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new JwtResponse(null,"Invalid refresh token"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new JwtResponse(null,"Invalid token"));

        }
    }

    /**
     * Logs out a user by revoking all their refresh tokens.
     * 
     * @param authHeader The Authorization header containing the access token
     * @return ResponseEntity with success message or error message
     */
    @DeleteMapping("/logout")
    public ResponseEntity<?> logout(@RequestHeader("Authorization") String authHeader) {
        return executeWithUser(authHeader, user -> {
            refreshService.revokeTokens(user);
            return ResponseEntity.ok("Successfully logged out and revoked all refresh tokens");
        });
    }

    /**
     * Updates the email address of an authenticated user.
     * 
     * @param authHeader The Authorization header containing the access token
     * @param updateEmailRequest The request containing the new email
     * @return ResponseEntity with success message or error message
     */
    @PutMapping("/update/email")
    public ResponseEntity<?> updateEmail(@RequestHeader("Authorization") String authHeader,
                                         @RequestBody UpdateEmailRequest updateEmailRequest) {
        return executeWithUser(authHeader, user -> {
            userService.updateEmail(user, updateEmailRequest.getNewEmail());
            return ResponseEntity.ok("Successfully updated email");
        });
    }

    /**
     * Updates the password of an authenticated user.
     * 
     * @param authHeader The Authorization header containing the access token
     * @param updatePasswordRequest The request containing the old and new passwords
     * @return ResponseEntity with success message or error message
     */
    @PutMapping("/update/password")
    public ResponseEntity<?> updatePassword(@RequestHeader("Authorization") String authHeader,
                                            @RequestBody UpdatePasswordRequest updatePasswordRequest) {
        return executeWithUser(authHeader, user -> {
            userService.updatePassword(user,
                    updatePasswordRequest.getOldPassword(),
                    updatePasswordRequest.getNewPassword());
            return ResponseEntity.ok("Successfully updated password");
        });
    }

    /**
     * Updates the username of an authenticated user.
     * 
     * @param authHeader The Authorization header containing the access token
     * @param updateUsernameRequest The request containing the new username
     * @return ResponseEntity with success message or error message
     */
    @PutMapping("/update/username")
    public ResponseEntity<?> updateUsername(@RequestHeader("Authorization") String authHeader,
                                            @RequestBody UpdateUsernameRequest updateUsernameRequest) {
        return executeWithUser(authHeader, user -> {
            userService.updateUsername(user, updateUsernameRequest.getNewUsername());
            return ResponseEntity.ok("Successfully updated username");
        });
    }

    /**
     * Updates the phone number of an authenticated user.
     * 
     * @param authHeader The Authorization header containing the access token
     * @param updatePhoneRequest The request containing the new phone number
     * @return ResponseEntity with success message or error message
     */
    @PutMapping("/update/phone")
    public ResponseEntity<?> updatePhone(@RequestHeader("Authorization") String authHeader,
                                         @RequestBody UpdatePhoneRequest updatePhoneRequest) {
        return executeWithUser(authHeader, user -> {
            userService.updatePhone(user, updatePhoneRequest.getNewPhone());
            return ResponseEntity.ok("Successfully updated phone");
        });
    }

    /**
     * Adds a trusted contact for an authenticated user.
     * If the contact already exists in the system, a relationship is created.
     * If the contact doesn't exist, a new user is created and invitations are sent.
     * 
     * @param authHeader The Authorization header containing the access token
     * @param addTrustedContactRequest The request containing contact details
     * @return ResponseEntity with success/failure response and details
     */
    @PostMapping("/addContact")
    public ResponseEntity<?> addContact(@RequestHeader("Authorization") String authHeader,
                                        @RequestBody AddTrustedContactRequest addTrustedContactRequest){
        return executeWithUser(authHeader, user ->{
            AddContactResponse response = userService.addContact(user, addTrustedContactRequest.getEmail(),
                    addTrustedContactRequest.getPhone(), addTrustedContactRequest.getMessageOnNotify());
            
            if (response.isSuccess()) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
        });
    }

    /**
     * Adds a role to a user.
     * 
     * @param authHeader The Authorization header containing the access token
     * @param request The request containing the user ID, role type, and whether it should be primary
     * @return ResponseEntity with the user's updated roles or error message
     */
    @PostMapping("/roles/add")
    public ResponseEntity<UserRolesResponse> addRole(@RequestHeader("Authorization") String authHeader,
                                                    @RequestBody AddRoleRequest request) {
        try {
            UserRolesResponse response = userService.addRoleToUser(request);
            return ResponseEntity.ok(response);
        } catch (UserException e) {
            UserRolesResponse errorResponse = new UserRolesResponse();
            errorResponse.setMessage(e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * Removes a role from a user.
     * 
     * @param authHeader The Authorization header containing the access token
     * @param request The request containing the user ID and role type to remove
     * @return ResponseEntity with the user's updated roles or error message
     */
    @DeleteMapping("/roles/remove")
    public ResponseEntity<UserRolesResponse> removeRole(@RequestHeader("Authorization") String authHeader,
                                                       @RequestBody RemoveRoleRequest request) {
        try {
            UserRolesResponse response = userService.removeRoleFromUser(request);
            return ResponseEntity.ok(response);
        } catch (UserException e) {
            UserRolesResponse errorResponse = new UserRolesResponse();
            errorResponse.setMessage(e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * Gets a user's roles.
     * 
     * @param authHeader The Authorization header containing the access token
     * @param request The request containing the user ID
     * @return ResponseEntity with the user's roles or error message
     */
    @PostMapping("/roles/get")
    public ResponseEntity<UserRolesResponse> getUserRoles(@RequestHeader("Authorization") String authHeader,
                                                         @RequestBody GetUserRolesRequest request) {
        try {
            UserRolesResponse response = userService.getUserRoles(request);
            return ResponseEntity.ok(response);
        } catch (UserException e) {
            UserRolesResponse errorResponse = new UserRolesResponse();
            errorResponse.setMessage(e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * Makes a user a trusted contact by adding the TRUSTED_CONTACT role.
     * 
     * @param authHeader The Authorization header containing the access token
     * @param request The request containing the user ID
     * @return ResponseEntity with the user's updated roles or error message
     */
    @PostMapping("/roles/make-trusted-contact")
    public ResponseEntity<UserRolesResponse> makeTrustedContact(@RequestHeader("Authorization") String authHeader,
                                                               @RequestBody MakeTrustedContactRequest request) {
        try {
            UserRolesResponse response = userService.makeTrustedContact(request);
            response.setMessage("Successfully added TRUSTED_CONTACT role to user");
            return ResponseEntity.ok(response);
        } catch (UserException e) {
            UserRolesResponse errorResponse = new UserRolesResponse();
            errorResponse.setMessage(e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * Checks if a user has a specific role.
     * 
     * @param authHeader The Authorization header containing the access token
     * @param request The request containing the user ID and role type to check
     * @return ResponseEntity with true if the user has the role, false otherwise
     */
    @PostMapping("/roles/check")
    public ResponseEntity<Boolean> checkUserHasRole(@RequestHeader("Authorization") String authHeader,
                                                  @RequestBody CheckUserRolesRequest request) {
        boolean hasRole = userService.userHasRole(request);
        return ResponseEntity.ok(hasRole);
    }

    /**
     * Checks if a user has both USER and TRUSTED_CONTACT roles.
     * 
     * @param authHeader The Authorization header containing the access token
     * @param request The request containing the user ID
     * @return ResponseEntity with true if the user has both roles, false otherwise
     */
    @PostMapping("/roles/check-both")
    public ResponseEntity<Boolean> checkUserHasBothRoles(@RequestHeader("Authorization") String authHeader,
                                                       @RequestBody CheckUserRolesRequest request) {
        boolean hasBothRoles = userService.userHasBothRoles(request);
        return ResponseEntity.ok(hasBothRoles);
    }

    /**
     * Retrieves a user by their ID from a JWT token.
     * 
     * @param token The JWT token containing the user ID
     * @return The User object
     * @throws UserException If the user is not found
     */
    private User getUserByID(String token) throws UserException {
        UUID userId = jwtUtil.validateTokenAndGetUserId(token);
        Optional<User> userOptional = userService.getUserById(userId);
        if (userOptional.isEmpty()) {
            throw new UserException("User not found");
        }
        return userOptional.get();
    }
    
    /**
     * Helper method to execute an action with an authenticated user.
     * This method handles common authentication and error handling logic.
     * 
     * @param authHeader The Authorization header containing the access token
     * @param action The action to execute with the authenticated user
     * @return ResponseEntity with the result of the action or error message
     */
    private ResponseEntity<?> executeWithUser(String authHeader, Function<User, ResponseEntity<?>> action) {
        try {
            String token = GetAccessToken.getAccessToken(authHeader);
            User user = getUserByID(token);
            return action.apply(user);
        } catch (UnauthorizedException e) {
            return ResponseEntity.status(HttpServletResponse.SC_UNAUTHORIZED).body(e.getMessage());
        } catch (UserException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid access token");
        }
    }
}

