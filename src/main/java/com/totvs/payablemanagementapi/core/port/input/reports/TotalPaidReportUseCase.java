package com.totvs.payablemanagementapi.core.port.input.reports;

import com.totvs.payablemanagementapi.core.port.input.dto.reports.TotalPaidReportDto;
import com.totvs.payablemanagementapi.core.port.input.dto.reports.TotalPaidReportFilterDto;

public interface TotalPaidReportUseCase {

    TotalPaidReportDto processReport(TotalPaidReportFilterDto totalPaidReportFilterDto);
}
