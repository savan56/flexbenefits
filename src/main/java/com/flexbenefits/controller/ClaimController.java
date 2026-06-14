package com.flexbenefits.controller;

import com.flexbenefits.config.TenantContext;
import com.flexbenefits.dto.ClaimResponse;
import com.flexbenefits.dto.CreateClaimRequest;
import com.flexbenefits.dto.UpdateClaimRequest;
import com.flexbenefits.service.ClaimService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/claims")
@RequiredArgsConstructor
public class ClaimController {

    private final ClaimService claimService;

    @PostMapping
    public ResponseEntity<ClaimResponse> createClaim(
            @Valid @RequestBody CreateClaimRequest request) {
        UUID tenantId = TenantContext.getTenantId();
        ClaimResponse response = claimService.createClaim(tenantId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<Page<ClaimResponse>> getClaims(Pageable pageable) {
        UUID tenantId = TenantContext.getTenantId();
        Page<ClaimResponse> claims = claimService.getClaims(tenantId, pageable);
        return ResponseEntity.ok(claims);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ClaimResponse> getClaimById(@PathVariable UUID id) {
        UUID tenantId = TenantContext.getTenantId();
        ClaimResponse response = claimService.getClaimById(tenantId, id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ClaimResponse> updateClaim(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateClaimRequest request) {
        UUID tenantId = TenantContext.getTenantId();
        ClaimResponse response = claimService.updateClaim(tenantId, id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteClaim(@PathVariable UUID id) {
        UUID tenantId = TenantContext.getTenantId();
        claimService.deleteClaim(tenantId, id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/submit")
    public ResponseEntity<ClaimResponse> submitClaim(@PathVariable UUID id) {
        UUID tenantId = TenantContext.getTenantId();
        ClaimResponse response = claimService.submitClaim(tenantId, id);
        return ResponseEntity.ok(response);
    }
}
