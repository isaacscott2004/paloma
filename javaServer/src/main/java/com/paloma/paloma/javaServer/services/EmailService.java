package com.paloma.paloma.javaServer.services;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    /**
     * Sends an invitation email to a contact who doesn't have an account in the system.
     *
     * @param toEmail The email address of the contact
     * @param userName The name of the user who is adding the contact
     * @param messageOnNotify Custom message from the user
     * @return true if the email was sent successfully, false otherwise
     */
    public boolean sendInvitationEmail(String toEmail, String userName, String messageOnNotify) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject(userName + " wants to add you as a trusted contact on Paloma");

            String htmlContent = buildInvitationEmailContent(userName, messageOnNotify);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            return true;
        } catch (MessagingException e) {
            // Log the error
            System.err.println("Failed to send invitation email: " + e.getMessage());
            return false;
        }
    }

    /**
     * Builds the HTML content for the invitation email.
     *
     * @param userName The name of the user who is adding the contact
     * @param messageOnNotify Custom message from the user
     * @return The HTML content for the email
     */
    private String buildInvitationEmailContent(String userName, String messageOnNotify) {
        StringBuilder htmlBuilder = new StringBuilder();
        htmlBuilder.append("<html><body>");
        htmlBuilder.append("<h2>Invitation to Join Paloma</h2>");
        htmlBuilder.append("<p>").append(userName).append(" wants to add you as a trusted contact on Paloma.</p>");
        
        if (messageOnNotify != null && !messageOnNotify.isEmpty()) {
            htmlBuilder.append("<p>Message from ").append(userName).append(": <em>\"")
                    .append(messageOnNotify).append("\"</em></p>");
        }
        
        htmlBuilder.append("<p>Paloma is a mental health app that helps users track their" +
                " well-being and connect with " + "trusted contacts.</p>");
        htmlBuilder.append("<p>To accept this invitation, please download the Paloma app and " +
                "create an account using " + "this email address.</p>");
        htmlBuilder.append("<p><a href=\"https://paloma-app.com/download\">Download Paloma App</a></p>");
        htmlBuilder.append("<p>Thank you,<br>The Paloma Team</p>");
        htmlBuilder.append("</body></html>");
        
        return htmlBuilder.toString();
    }
}