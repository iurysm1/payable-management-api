package com.totvs.payablemanagementapi.core.port.input.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record UpdatePayableStatusDto(
        @NotNull(message = "O status da conta a pagar é obrigatório")
        Integer status,
        LocalDate paymentDate
) {
}
