package com.crudzaso.crudcloud_backend.service;

import com.crudzaso.crudcloud_backend.dto.LoginRequest;
import com.crudzaso.crudcloud_backend.dto.LoginResponse;
import com.crudzaso.crudcloud_backend.model.User;
import com.crudzaso.crudcloud_backend.repository.UserRepository;
import com.crudzaso.crudcloud_backend.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    // Register
    public User register(User user) {
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new RuntimeException("The email address is already registered.");
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        if (user.getRole() == null)
            user.setRole("USER");

        // Set provider as LOCAL for traditional registration
        if (user.getProvider() == null)
            user.setProvider("LOCAL");

        return userRepository.save(user);
    }

    // Login
    public LoginResponse login(LoginRequest loginRequest) {
        User user = userRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Check if user is a Google user (no password)
        if (user.getPassword() == null || "GOOGLE".equals(user.getProvider())) {
            throw new RuntimeException("This account uses Google Sign-In. Please login with Google.");
        }

        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            throw new RuntimeException("Password is incorrect");
        }

        String token = jwtUtil.generateToken(user.getEmail(), user.getRole());

        return new LoginResponse(token, user.getEmail(), user.getRole());
    }

}
