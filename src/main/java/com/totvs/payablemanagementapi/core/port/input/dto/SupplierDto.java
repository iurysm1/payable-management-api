package com.totvs.payablemanagementapi.core.port.input.dto;

import jakarta.validation.constraints.NotBlank;

public record SupplierDto(
        Long id,
        @NotBlank String name
) {
}
