package com.crudzaso.crudcloud_backend.controller;

import com.crudzaso.crudcloud_backend.dto.GoogleAuthRequest;
import com.crudzaso.crudcloud_backend.dto.LoginResponse;
import com.crudzaso.crudcloud_backend.service.GoogleAuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Google Auth", description = "Google OAuth authentication endpoints")
@RestController
@RequestMapping("/api/auth/google")
@RequiredArgsConstructor
public class GoogleAuthController {

    private final GoogleAuthService googleAuthService;

    @Operation(
        summary = "Authenticate with Google",
        description = "Authenticate a user using Google ID token. If the user doesn't exist, it will be created automatically."
    )
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> googleLogin(@Valid @RequestBody GoogleAuthRequest request) {
        try {
            LoginResponse response = googleAuthService.authenticateWithGoogle(request.getToken());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
