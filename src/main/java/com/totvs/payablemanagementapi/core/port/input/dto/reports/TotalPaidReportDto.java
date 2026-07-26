package com.totvs.payablemanagementapi.core.port.input.dto.reports;


import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record TotalPaidReportDto(
        LocalDate startDate,
        LocalDate endDate,
        BigDecimal totalPaid,
        int paidCount,
        List<PaidPayableItemDto> paidPayableItems
) {
}
