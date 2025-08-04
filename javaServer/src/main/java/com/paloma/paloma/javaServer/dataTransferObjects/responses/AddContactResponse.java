package com.paloma.paloma.javaServer.dataTransferObjects.responses;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddContactResponse {
    private boolean success;
    private String message;
    private boolean contactExists;
    private boolean emailSent;
    private boolean smsSent;
    
    /**
     * Constructor for when the contact exists in the system
     * 
     * @param success Whether the operation was successful
     * @param message A message describing the result of the operation
     */
    public AddContactResponse(boolean success, String message, boolean contactExists) {
        this.success = success;
        this.message = message;
        this.contactExists = contactExists;
        this.emailSent = false;
        this.smsSent = false;
    }
}