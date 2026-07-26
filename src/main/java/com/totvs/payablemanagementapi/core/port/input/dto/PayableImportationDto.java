package com.totvs.payablemanagementapi.core.port.input.dto;

import com.totvs.payablemanagementapi.domain.enums.StatusPayableImportationEnum;

import java.time.LocalDateTime;

public record PayableImportationDto(
        Long id,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        StatusPayableImportationEnum status,
        String errorMessage
) {
}
