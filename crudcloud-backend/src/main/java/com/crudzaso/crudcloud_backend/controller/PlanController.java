package com.crudzaso.crudcloud_backend.controller;

import com.crudzaso.crudcloud_backend.dto.CreatePlanRequest;
import com.crudzaso.crudcloud_backend.dto.PlanDto;
import com.crudzaso.crudcloud_backend.dto.UpdatePlanRequest;
import com.crudzaso.crudcloud_backend.service.PlanService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Plans", description = "Plan management endpoints")
@RestController
@RequestMapping("/api/plans")
@CrossOrigin(origins = "")
public class PlanController {

    private final PlanService planService;

    public PlanController(PlanService planService) {
        this.planService = planService;
    }

    // CREATE - Crear nuevo plan (Solo Admin)
    @Operation(summary = "Create a new plan (ADMIN)")
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PlanDto> createPlan(@RequestBody CreatePlanRequest request) {
        PlanDto created = planService.createPlan(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    // READ - Obtener todos los planes activos
    @Operation(summary = "Get all active plans")
    @GetMapping
    public ResponseEntity<List<PlanDto>> getAllPlans() {
        return ResponseEntity.ok(planService.getAllPlans());
    }

    // READ - Obtener plan por ID
    @Operation(summary = "Get plan by id")
    @GetMapping("/{id}")
    public ResponseEntity<PlanDto> getPlanById(@PathVariable Long id) {
        return ResponseEntity.ok(planService.getPlanById(id));
    }

    // UPDATE - Actualizar plan (Solo Admin)
    @Operation(summary = "Update an existing plan (ADMIN)")
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PlanDto> updatePlan(
            @PathVariable Long id,
            @RequestBody UpdatePlanRequest request) {
        PlanDto updated = planService.updatePlan(id, request);
        return ResponseEntity.ok(updated);
    }

    // DELETE - Eliminar plan (Solo Admin)
    @Operation(summary = "Delete a plan (ADMIN)")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deletePlan(@PathVariable Long id) {
        planService.deletePlan(id);
        return ResponseEntity.noContent().build();
    }
}