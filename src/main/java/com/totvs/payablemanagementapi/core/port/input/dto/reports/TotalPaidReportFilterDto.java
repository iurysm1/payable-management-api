package com.totvs.payablemanagementapi.core.port.input.dto.reports;

import com.totvs.payablemanagementapi.core.util.DatePeriodCriteria;
import com.totvs.payablemanagementapi.domain.Supplier;

public record TotalPaidReportFilterDto (
        Supplier supplier,
        DatePeriodCriteria periodCriteria
){
}
