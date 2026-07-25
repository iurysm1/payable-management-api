package com.totvs.payablemanagementapi.core.port.input.dto;

import com.totvs.payablemanagementapi.domain.enums.StatusPayableEnum;

import java.math.BigDecimal;
import java.time.LocalDate;

public record PayableDto(
        Long id,
        String description,
        BigDecimal amount,
        StatusPayableEnum status,
        LocalDate expirationDate,
        LocalDate paymentDate,
        Long supplierId
) {
}
