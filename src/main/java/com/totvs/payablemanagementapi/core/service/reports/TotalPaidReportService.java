package com.totvs.payablemanagementapi.core.service.reports;

import com.totvs.payablemanagementapi.core.port.input.dto.reports.PaidPayableItemDto;
import com.totvs.payablemanagementapi.core.port.input.dto.reports.TotalPaidReportDto;
import com.totvs.payablemanagementapi.core.port.input.dto.reports.TotalPaidReportFilterDto;
import com.totvs.payablemanagementapi.core.port.input.reports.TotalPaidReportUseCase;
import com.totvs.payablemanagementapi.core.port.output.reports.TotalPaidPersistencePort;
import com.totvs.payablemanagementapi.core.exception.TotalPaidReportNotFoundException;
import com.totvs.payablemanagementapi.core.service.SupplierService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TotalPaidReportService implements TotalPaidReportUseCase {

    private final SupplierService supplierService;
    private final TotalPaidPersistencePort totalPaidPersistencePort;

    @Override
    public TotalPaidReportDto processReport(TotalPaidReportFilterDto totalPaidReportFilterDto) {
        if (totalPaidReportFilterDto.supplierId() != null) {
            supplierService.findById(totalPaidReportFilterDto.supplierId());
        }

        List<PaidPayableItemDto> reportList = totalPaidPersistencePort.findPaidByPeriod(
                totalPaidReportFilterDto
        );
        if (reportList.isEmpty()) {
            throw new TotalPaidReportNotFoundException();
        }


        BigDecimal totalPaid = reportList.stream()
                .map(PaidPayableItemDto::amount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new TotalPaidReportDto(
                totalPaidReportFilterDto.periodCriteria().startDate(),
                totalPaidReportFilterDto.periodCriteria().endDate(),
                totalPaid,
                reportList.size(),
                reportList
        );
    }
}
