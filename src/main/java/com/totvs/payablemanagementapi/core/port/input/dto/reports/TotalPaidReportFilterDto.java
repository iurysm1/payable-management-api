package com.totvs.payablemanagementapi.core.port.input.dto.reports;

import com.totvs.payablemanagementapi.core.util.DatePeriodCriteriaRequired;

public record TotalPaidReportFilterDto (
        Long supplierId,
        DatePeriodCriteriaRequired periodCriteria
){
}
