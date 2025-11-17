package com.crudzaso.crudcloud_backend.service;

import com.crudzaso.crudcloud_backend.model.Instance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;

    @Value("${app.mail.from:noreply@crudcloud.local}")
    private String from;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendWelcomeEmail(String to, String fullName, String password) {
        String subject = "Welcome to CrudCloud - Your account is ready";
        String body = "Hello " + fullName + ",\n\n" +
                "Welcome to CrudCloud! Your account has been created successfully.\n\n" +
                "Your login credentials:\n" +
                "- Email: " + to + "\n" +
                "- Password: " + password + "\n\n" +
                "Please keep this information safe and change your password after your first login.\n\n" +
                "If you didn't create this account, please contact support immediately.\n\n" +
                "Best regards,\n" +
                "The CrudCloud Team";
        send(to, subject, body);
    }

    public void sendInstanceCreatedEmail(String to, String engineName, Instance instance, String plainPassword) {
        String subject = "Your database instance is ready";
        String body = "Hello,\n\n" +
                "Your instance has been created successfully. Here are the details:\n" +
                "- Engine: " + engineName + "\n" +
                "- Database: " + instance.getDbName() + "\n" +
                "- User: " + instance.getUserDb() + "\n" +
                "- Host: " + instance.getHost() + "\n" +
                "- Port: " + instance.getPort() + "\n" +
                "- Temporary Password (store it safely): " + plainPassword + "\n\n" +
                "If you didn't request this operation, please contact support immediately.";
        send(to, subject, body);
    }

    public void sendPasswordRotatedEmail(String to, String engineName, Instance instance, String newPassword) {
        String subject = "Your database password was rotated";
        String body = "Hello,\n\n" +
                "Your database user password has been rotated. New credentials:\n" +
                "- Engine: " + engineName + "\n" +
                "- Database: " + instance.getDbName() + "\n" +
                "- User: " + instance.getUserDb() + "\n" +
                "- Host: " + instance.getHost() + "\n" +
                "- Port: " + instance.getPort() + "\n" +
                "- New Password: " + newPassword + "\n\n" +
                "If you didn't request this operation, please contact support immediately.";
        send(to, subject, body);
    }

    private void send(String to, String subject, String body) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(from);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            mailSender.send(message);
            log.info("Email sent to {} with subject '{}'", to, subject);
        } catch (Exception e) {
            // Do not break the business flow due to email errors
            log.error("Failed to send email to {}: {}", to, e.getMessage());
            throw e; // Re-throw to let caller handle it
        }
    }
}
