package com.totvs.payablemanagementapi.core.port.output.reports;

import com.totvs.payablemanagementapi.core.port.input.dto.reports.TotalPaidReportFilterDto;
import com.totvs.payablemanagementapi.domain.Payable;

import java.util.List;

public interface TotalPaidPersistencePort {
    List<Payable> findPaidByPeriod(TotalPaidReportFilterDto totalPaidReportFilterDto);
}
