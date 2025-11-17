package com.crudzaso.crudcloud_backend.controller;

import com.crudzaso.crudcloud_backend.dto.RegisterUserRequest;
import com.crudzaso.crudcloud_backend.model.User;
import com.crudzaso.crudcloud_backend.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import jakarta.validation.Valid;

@Tag(name = "Users", description = "User management endpoints")
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(summary = "Register a new user")
    @PostMapping("/register")
    public ResponseEntity<User> register(@Valid @RequestBody RegisterUserRequest req) {
        User u = User.builder()
                .email(req.getEmail())
                .password(req.getPassword())
                .fullName(req.getFullName())
                .role(req.getRole())
                .enable(true)
                .build();
        return ResponseEntity.ok(userService.register(u));
    }

    @Operation(summary = "Get all users")
    @GetMapping
    public ResponseEntity<List<User>> findAll() {
        return ResponseEntity.ok(userService.findAll());
    }

    @Operation(summary = "Get user by id")
    @GetMapping("/{id}")
    public ResponseEntity<User> findById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.findById(id));
    }
}
