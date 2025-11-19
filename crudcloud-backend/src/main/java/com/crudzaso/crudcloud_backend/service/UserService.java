package com.crudzaso.crudcloud_backend.service;

import com.crudzaso.crudcloud_backend.model.User;
import com.crudzaso.crudcloud_backend.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import com.crudzaso.crudcloud_backend.repository.PlanRepository;
import com.crudzaso.crudcloud_backend.repository.UsersPlansRepository;
import com.crudzaso.crudcloud_backend.model.UsersPlans;
import com.crudzaso.crudcloud_backend.model.Plan;
import java.util.Date;

@Service
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    // New repositories to auto-assign FREE plan
    private final PlanRepository planRepository;
    private final UsersPlansRepository usersPlansRepository;
    // Discord notification service
    private final DiscordNotificationService discordNotificationService;

    public UserService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       EmailService emailService,
                       PlanRepository planRepository,
                       UsersPlansRepository usersPlansRepository,
                       DiscordNotificationService discordNotificationService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
        this.planRepository = planRepository;
        this.usersPlansRepository = usersPlansRepository;
        this.discordNotificationService = discordNotificationService;
    }

    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found with email: " + email));
    }

    public List<User> findAll() {
        return userRepository.findAll();
    }

    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + id));
    }

    public User register(User user) {
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new IllegalArgumentException("The email address is already registered.");
        }
        if (user.getPassword() == null || user.getPassword().isBlank()) {
            throw new IllegalArgumentException("Password is required.");
        }

        // Store plain password temporarily for email
        String plainPassword = user.getPassword();

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        if (user.getRole() == null) user.setRole("USER");
        if (!user.isEnable()) user.setEnable(false);

        User savedUser = userRepository.save(user);

        // Auto-assign FREE plan (id=3) ACTIVE for 30 days
        Plan freePlan = planRepository.findByIdAndState(3L, "ACTIVE")
                .orElseThrow(() -> new IllegalStateException("Free plan (id=3) not found or inactive"));

        Date now = new Date();
        Date end = new Date(now.getTime() + 30L * 24 * 60 * 60 * 1000); // +30 days

        UsersPlans up = UsersPlans.builder()
                .user(savedUser)
                .plan(freePlan)
                .status("ACTIVE")
                .startDate(now)
                .endDate(end)
                .build();
        usersPlansRepository.save(up);

        // Send welcome email with credentials
        try {
            emailService.sendWelcomeEmail(savedUser.getEmail(), savedUser.getFullName(), plainPassword);
        } catch (Exception e) {
            // Don't break registration if email fails
            log.error("Failed to send welcome email to {}: {}", savedUser.getEmail(), e.getMessage(), e);
        }

        // Send Discord notification
        try {
            log.info("Calling Discord notification service for user: {}", savedUser.getEmail());
            discordNotificationService.sendUserRegistrationNotification(
                    savedUser.getEmail(),
                    savedUser.getFullName(),
                    savedUser.getRole()
            );
            log.info("Discord notification call completed");
        } catch (Exception e) {
            // Don't break registration if Discord notification fails
            log.error("Failed to send Discord notification: {}", e.getMessage(), e);
        }

        return savedUser;
    }
}