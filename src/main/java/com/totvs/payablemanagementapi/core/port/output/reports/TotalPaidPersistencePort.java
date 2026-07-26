package com.totvs.payablemanagementapi.core.port.output.reports;

import com.totvs.payablemanagementapi.core.port.input.dto.reports.PaidPayableItemDto;
import com.totvs.payablemanagementapi.core.port.input.dto.reports.TotalPaidReportFilterDto;

import java.util.List;

public interface TotalPaidPersistencePort {
    List<PaidPayableItemDto> findPaidByPeriod(TotalPaidReportFilterDto totalPaidReportFilterDto);
}
