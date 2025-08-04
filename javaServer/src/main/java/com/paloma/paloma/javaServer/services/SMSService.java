package com.paloma.paloma.javaServer.services;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SMSService {

    @Value("${twilio.account.sid}")
    private String accountSid;

    @Value("${twilio.auth.token}")
    private String authToken;

    @Value("${twilio.phone.number}")
    private String twilioPhoneNumber;

    /**
     * Initialize Twilio with account credentials.
     */
    @PostConstruct
    private void initTwilio() {
        Twilio.init(accountSid, authToken);
    }

    /**
     * Sends an invitation SMS to a contact who doesn't have an account in the system.
     *
     * @param toPhoneNumber The phone number of the contact
     * @param userName The name of the user who is adding the contact
     * @param messageOnNotify Custom message from the user
     * @return true if the SMS was sent successfully, false otherwise
     */
    public boolean sendInvitationSMS(String toPhoneNumber, String userName, String messageOnNotify) {
        try {
            // Format the phone number if needed
            if (!toPhoneNumber.startsWith("+")) {
                toPhoneNumber = "+1" + toPhoneNumber; // Default to US country code if not specified
            }

            String smsContent = buildInvitationSMSContent(userName, messageOnNotify);

            Message message = Message.creator(
                    new PhoneNumber(toPhoneNumber),
                    new PhoneNumber(twilioPhoneNumber),
                    smsContent
            ).create();

            // Check if the message was sent successfully
            return message.getStatus() == Message.Status.QUEUED || 
                   message.getStatus() == Message.Status.SENT || 
                   message.getStatus() == Message.Status.DELIVERED;
        } catch (Exception e) {
            // Log the error
            System.err.println("Failed to send invitation SMS: " + e.getMessage());
            return false;
        }
    }

    /**
     * Builds the content for the invitation SMS.
     *
     * @param userName The name of the user who is adding the contact
     * @param messageOnNotify Custom message from the user
     * @return The content for the SMS
     */
    private String buildInvitationSMSContent(String userName, String messageOnNotify) {
        StringBuilder smsBuilder = new StringBuilder();
        smsBuilder.append(userName).append(" wants to add you as a trusted contact on Paloma. ");
        
        if (messageOnNotify != null && !messageOnNotify.isEmpty()) {
            smsBuilder.append("Message: \"").append(messageOnNotify).append("\" ");
        }
        
        smsBuilder.append("Download the Paloma app to connect: https://paloma-app.com/download");
        
        return smsBuilder.toString();
    }
}