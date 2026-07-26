package com.totvs.payablemanagementapi.core.service.reports;

import com.totvs.payablemanagementapi.core.port.input.dto.reports.TotalPaidReportDto;
import com.totvs.payablemanagementapi.core.port.input.dto.reports.TotalPaidReportFilterDto;
import com.totvs.payablemanagementapi.core.port.input.reports.TotalPaidReportUseCase;
import com.totvs.payablemanagementapi.core.port.output.reports.TotalPaidPersistencePort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TotalPaidReportService implements TotalPaidReportUseCase {

    private final TotalPaidPersistencePort totalPaidPersistencePort;

    @Override
    public TotalPaidReportDto processReport(TotalPaidReportFilterDto totalPaidReportFilterDto) {
        return null;
    }
}
