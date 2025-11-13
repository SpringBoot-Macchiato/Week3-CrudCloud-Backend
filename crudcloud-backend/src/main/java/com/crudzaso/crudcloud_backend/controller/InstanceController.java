    package com.crudzaso.crudcloud_backend.controller;

    import com.crudzaso.crudcloud_backend.dto.InstanceRequest;
    import com.crudzaso.crudcloud_backend.dto.InstanceResponse;
    import com.crudzaso.crudcloud_backend.service.InstanceService;
    import com.crudzaso.crudcloud_backend.service.UserService;
    import io.swagger.v3.oas.annotations.Operation;
    import io.swagger.v3.oas.annotations.tags.Tag;
    import jakarta.validation.Valid;
    import org.springframework.http.ResponseEntity;
    import org.springframework.security.core.Authentication;
    import org.springframework.web.bind.annotation.*;

    import java.util.List;

    @Tag(name = "Instances", description = "Instance management endpoints")
    @RestController
    @RequestMapping("/api/instances")
    public class InstanceController {

        private final InstanceService instanceService;
        private final UserService userService;
        public InstanceController(InstanceService instanceService, UserService userService) {
            this.instanceService = instanceService;
            this.userService = userService;
        }

        // Create instance
        @Operation(summary = "Create a new instance")
        @PostMapping
        public ResponseEntity<InstanceResponse> createInstance(@Valid @RequestBody InstanceRequest req,
                                                               Authentication auth) throws Exception {
            // Extract userId from auth (assuming token subject is email; you'd map to userId)
            // For demo: you must implement a method to get userId by email, e.g., UserService.findByEmail(...)
            Long userId = getUserIdFromAuth(auth);

            // TODO: fetch plan limit and whether custom names allowed from user's plan.
            int planMaxInstances = 5; // placeholder; fetch real value from user/plan
            boolean allowCustomName = true; // fetch policy from plan

            InstanceResponse resp = instanceService.createInstance(userId, req, planMaxInstances, allowCustomName);
            return ResponseEntity.status(201).body(resp);
        }

        @Operation(summary = "List user instances")
        // List user instances
        @GetMapping
        public ResponseEntity<List<InstanceResponse>> listInstances(Authentication auth) {
            Long userId = getUserIdFromAuth(auth);
            return ResponseEntity.ok(instanceService.listInstancesForUser(userId));
        }

        // Get single
        @Operation(summary = "Get instance details")
        @GetMapping("/{id}")
        public ResponseEntity<InstanceResponse> getInstance(@PathVariable Long id, Authentication auth) {
            Long userId = getUserIdFromAuth(auth);
            return ResponseEntity.ok(instanceService.getInstance(id, userId));
        }

        @Operation(summary = "Suspend an instance")
        @PostMapping("/{id}/suspend")
        public ResponseEntity<?> suspend(@PathVariable Long id, Authentication auth) throws Exception {
            Long userId = getUserIdFromAuth(auth);
            instanceService.suspendInstance(id, userId);
            return ResponseEntity.ok().build();
        }

        @Operation(summary = "Resume an instance")
        @PostMapping("/{id}/resume")
        public ResponseEntity<?> resume(@PathVariable Long id, Authentication auth) throws Exception {
            Long userId = getUserIdFromAuth(auth);
            instanceService.resumeInstance(id, userId);
            return ResponseEntity.ok().build();
        }

        @Operation(summary = "Rotate instance password")
        @PostMapping("/{id}/rotate-password")
        public ResponseEntity<String> rotate(@PathVariable Long id, Authentication auth) throws Exception {
            Long userId = getUserIdFromAuth(auth);
            String newPass = instanceService.rotatePassword(id, userId);
            return ResponseEntity.ok(newPass); // show password once
        }

        @Operation(summary = "Delete an instance")
        @DeleteMapping("/{id}")
        public ResponseEntity<?> delete(@PathVariable Long id, Authentication auth) throws Exception {
            Long userId = getUserIdFromAuth(auth);
            instanceService.deleteInstance(id, userId);
            return ResponseEntity.noContent().build();
        }

        // --- helper to map Authentication -> userId (implement with your UserService)
        private Long getUserIdFromAuth(Authentication auth) {
            String email = (String) auth.getPrincipal(); // because we set principal = email in Jwt filter
            // TODO: call UserService to obtain userId by email. For now, assume email maps to id via some method.
            // e.g., return userService.findByEmail(email).getId();
            return userService.findByEmail((String) auth.getPrincipal()).getId();

        }
    }
