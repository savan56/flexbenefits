package com.flexbenefits.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record ClaimResponse(
        UUID id,
        UUID tenantId,
        UUID employeeId,
        String claimNumber,
        String status,
        LocalDate serviceDate,
        String providerName,
        String diagnosisCode,
        BigDecimal claimedAmount,
        BigDecimal approvedAmount,
        String rejectionReason,
        LocalDateTime submittedAt,
        LocalDateTime createdAt
) {}

