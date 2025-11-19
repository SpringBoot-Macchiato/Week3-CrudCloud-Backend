package com.crudzaso.crudcloud_backend.controller;

import com.crudzaso.crudcloud_backend.dto.GoogleAuthRequest;
import com.crudzaso.crudcloud_backend.dto.LoginResponse;
import com.crudzaso.crudcloud_backend.service.GoogleAuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Google Auth", description = "Google OAuth authentication endpoints")
@RestController
@RequestMapping("/api/auth/google")
@RequiredArgsConstructor
public class GoogleAuthController {

    private static final Logger log = LoggerFactory.getLogger(GoogleAuthController.class);

    private final GoogleAuthService googleAuthService;

    @Operation(
        summary = "Authenticate with Google",
        description = "Authenticate a user using Google ID token. If the user doesn't exist, it will be created automatically."
    )
    @PostMapping("/login")
    public ResponseEntity<?> googleLogin(@Valid @RequestBody GoogleAuthRequest request) {
        try {
            log.info("Google login attempt - Token received: {}", request.getToken() != null ? "Yes" : "No");
            log.debug("Google login - Token length: {}", request.getToken() != null ? request.getToken().length() : 0);

            LoginResponse response = googleAuthService.authenticateWithGoogle(request.getToken());

            log.info("Google login successful for user: {}", response.getEmail());
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.error("Validation error in Google login: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new ErrorResponse("Validation failed: " + e.getMessage()));
        } catch (Exception e) {
            log.error("Error in Google login: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(new ErrorResponse("Authentication failed: " + e.getMessage()));
        }
    }

    // Inner class for error responses
    private record ErrorResponse(String error) {}
}
