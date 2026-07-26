package com.totvs.payablemanagementapi.core.port.input.dto;

import com.totvs.payablemanagementapi.domain.enums.StatusPayableImportationItemEnum;

public record PayableImportationItemDto(
        Long id,
        Long payableId,
        Long payableImportationId,
        StatusPayableImportationItemEnum status,
        String errorMessage
) {
}
