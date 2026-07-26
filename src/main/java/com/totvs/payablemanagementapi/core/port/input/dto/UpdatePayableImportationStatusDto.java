package com.totvs.payablemanagementapi.core.port.input.dto;

public record UpdatePayableImportationStatusDto(
        Integer status,
        String errorMessage
) {
}
