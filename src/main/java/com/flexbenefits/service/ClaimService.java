package com.flexbenefits.service;

import com.flexbenefits.dto.ClaimResponse;
import com.flexbenefits.dto.CreateClaimRequest;
import com.flexbenefits.dto.UpdateClaimRequest;
import com.flexbenefits.entity.Claim;
import com.flexbenefits.entity.Employee;
import com.flexbenefits.entity.Enrollment;
import com.flexbenefits.entity.Tenant;
import com.flexbenefits.entity.enums.ClaimStatus;
import com.flexbenefits.exception.ResourceNotFoundException;
import com.flexbenefits.mapper.ClaimMapper;
import com.flexbenefits.repository.ClaimRepository;
import com.flexbenefits.repository.EmployeeRepository;
import com.flexbenefits.repository.EnrollmentRepository;
import com.flexbenefits.repository.TenantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.Year;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class ClaimService {

    private final ClaimRepository claimRepository;
    private final TenantRepository tenantRepository;
    private final EmployeeRepository employeeRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final ClaimMapper claimMapper;

    // Simple in-memory counter — in production, use a DB sequence
    private final AtomicLong claimSequence = new AtomicLong(1);

    public ClaimResponse createClaim(UUID tenantId, CreateClaimRequest request) {
        log.info("Creating claim for tenant: {}, employee: {}", tenantId, request.employeeId());

        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant", tenantId));

        Employee employee = employeeRepository.findById(request.employeeId())
                .orElseThrow(() -> new ResourceNotFoundException("Employee", request.employeeId()));

        Enrollment enrollment = enrollmentRepository.findById(request.enrollmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Enrollment", request.enrollmentId()));

        // Verify employee and enrollment belong to this tenant
        if (!employee.getTenant().getId().equals(tenantId)) {
            throw new IllegalStateException("Employee does not belong to tenant: " + tenantId);
        }
        if (!enrollment.getTenant().getId().equals(tenantId)) {
            throw new IllegalStateException("Enrollment does not belong to tenant: " + tenantId);
        }

        Claim claim = new Claim();
        claim.setTenant(tenant);
        claim.setEmployee(employee);
        claim.setEnrollment(enrollment);
        claim.setClaimNumber(generateClaimNumber());
        claim.setStatus(ClaimStatus.DRAFT);
        claim.setServiceDate(request.serviceDate());
        claim.setProviderName(request.providerName());
        claim.setDiagnosisCode(request.diagnosisCode());
        claim.setClaimedAmount(request.claimedAmount());

        Claim saved = claimRepository.save(claim);
        log.info("Claim created: {} with number: {}", saved.getId(), saved.getClaimNumber());
        return claimMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public Page<ClaimResponse> getClaims(UUID tenantId, Pageable pageable) {
        return claimRepository.findByTenantId(tenantId, pageable)
                .map(claimMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public ClaimResponse getClaimById(UUID tenantId, UUID claimId) {
        Claim claim = findClaimByTenantAndId(tenantId, claimId);
        return claimMapper.toResponse(claim);
    }

    public ClaimResponse updateClaim(UUID tenantId, UUID claimId, UpdateClaimRequest request) {
        Claim claim = findClaimByTenantAndId(tenantId, claimId);

        if (claim.getStatus() != ClaimStatus.DRAFT) {
            throw new IllegalStateException("Can only update claims in DRAFT status. Current status: " + claim.getStatus());
        }

        if (request.serviceDate() != null) {
            claim.setServiceDate(request.serviceDate());
        }
        if (request.providerName() != null) {
            claim.setProviderName(request.providerName());
        }
        if (request.diagnosisCode() != null) {
            claim.setDiagnosisCode(request.diagnosisCode());
        }
        if (request.claimedAmount() != null) {
            claim.setClaimedAmount(request.claimedAmount());
        }

        Claim saved = claimRepository.save(claim);
        return claimMapper.toResponse(saved);
    }

    public void deleteClaim(UUID tenantId, UUID claimId) {
        Claim claim = findClaimByTenantAndId(tenantId, claimId);

        if (claim.getStatus() != ClaimStatus.DRAFT) {
            throw new IllegalStateException("Can only delete claims in DRAFT status. Current status: " + claim.getStatus());
        }

        // Hard delete — only allowed for DRAFT claims
        // TODO: Consider adding soft delete (active flag) in a future iteration
        log.info("Deleting claim: {} for tenant: {}", claimId, tenantId);
        claimRepository.delete(claim);
    }

    public ClaimResponse submitClaim(UUID tenantId, UUID claimId) {
        Claim claim = findClaimByTenantAndId(tenantId, claimId);

        if (claim.getStatus() != ClaimStatus.DRAFT) {
            throw new IllegalStateException("Can only submit claims in DRAFT status. Current status: " + claim.getStatus());
        }

        claim.setStatus(ClaimStatus.SUBMITTED);
        claim.setSubmittedAt(LocalDateTime.now());

        Claim saved = claimRepository.save(claim);
        log.info("Claim submitted: {} with number: {}", saved.getId(), saved.getClaimNumber());
        return claimMapper.toResponse(saved);
    }

    // --- Private helpers ---

    private Claim findClaimByTenantAndId(UUID tenantId, UUID claimId) {
        Claim claim = claimRepository.findById(claimId)
                .orElseThrow(() -> new ResourceNotFoundException("Claim", claimId));

        if (!claim.getTenant().getId().equals(tenantId)) {
            throw new ResourceNotFoundException("Claim", claimId);
        }

        return claim;
    }

    private String generateClaimNumber() {
        return "CLM-" + Year.now().getValue() + "-" + String.format("%06d", claimSequence.getAndIncrement());
    }
}
