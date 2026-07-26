package com.totvs.payablemanagementapi.core.port.input.dto.reports;

import com.totvs.payablemanagementapi.domain.enums.StatusPayableEnum;

import java.math.BigDecimal;
import java.time.LocalDate;

public record PaidPayableItemDto(
        String description,
        BigDecimal amount,
        StatusPayableEnum status,
        LocalDate expirationDate,
        LocalDate paymentDate,
        String supplierName
) {
}
