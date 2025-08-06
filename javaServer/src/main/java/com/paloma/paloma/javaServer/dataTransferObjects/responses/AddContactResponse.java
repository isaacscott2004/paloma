package com.paloma.paloma.javaServer.dataTransferObjects.responses;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class AddContactResponse {
    private boolean success;
    private String message;
    private boolean contactExists;
    private boolean emailSent;
    
    /**
     * Constructor for when the contact exists in the system
     * 
     * @param success Whether the operation was successful
     * @param message A message describing the result of the operation
     * @param contactExists Whether the contact exists in the system
     */
    public AddContactResponse(boolean success, String message, boolean contactExists) {
        this.success = success;
        this.message = message;
        this.contactExists = contactExists;
        this.emailSent = false;
    }
    
    /**
     * Constructor for when the contact doesn't exist and an invitation is sent
     * 
     * @param success Whether the operation was successful
     * @param message A message describing the result of the operation
     * @param contactExists Whether the contact exists in the system
     * @param emailSent Whether the email invitation was sent successfully
     */
    public AddContactResponse(boolean success, String message, boolean contactExists, boolean emailSent) {
        this.success = success;
        this.message = message;
        this.contactExists = contactExists;
        this.emailSent = emailSent;
    }
}