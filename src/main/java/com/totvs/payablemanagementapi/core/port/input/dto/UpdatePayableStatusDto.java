package com.totvs.payablemanagementapi.core.port.input.dto;

import jakarta.validation.constraints.NotNull;

public record UpdatePayableStatusDto(
        @NotNull(message = "O status da conta a pagar é obrigatório")
        Integer status
) {
}
