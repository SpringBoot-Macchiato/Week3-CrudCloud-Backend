package com.crudzaso.crudcloud_backend.service;

import com.crudzaso.crudcloud_backend.model.User;
import com.crudzaso.crudcloud_backend.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, EmailService emailService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
    }

    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
    }

    public List<User> findAll() {
        return userRepository.findAll();
    }

    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
    }

    public User register(User user) {
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new RuntimeException("The email address is already registered.");
        }
        if (user.getPassword() == null || user.getPassword().isBlank()) {
            throw new RuntimeException("Password is required.");
        }

        // Store plain password temporarily for email
        String plainPassword = user.getPassword();

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        if (user.getRole() == null) user.setRole("USER");
        if (!user.isEnable()) user.setEnable(false);

        User savedUser = userRepository.save(user);

        // Send welcome email with credentials
        try {
            emailService.sendWelcomeEmail(savedUser.getEmail(), savedUser.getFullName(), plainPassword);
        } catch (Exception e) {
            // Don't break registration if email fails
            System.err.println("Failed to send welcome email to " + savedUser.getEmail() + ": " + e.getMessage());
        }

        return savedUser;
    }
}
