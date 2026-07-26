package com.totvs.payablemanagementapi.adapter.output.repository.reports;

import com.totvs.payablemanagementapi.core.port.input.dto.reports.PaidPayableItemDto;
import com.totvs.payablemanagementapi.core.port.input.dto.reports.TotalPaidReportFilterDto;
import com.totvs.payablemanagementapi.core.port.output.reports.TotalPaidPersistencePort;
import com.totvs.payablemanagementapi.domain.enums.StatusPayableEnum;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class TotalPaidJpaAdapter implements TotalPaidPersistencePort {

    private final TotalPaidRepository totalPaidRepository;

    @Override
    public List<PaidPayableItemDto> findPaidByPeriod(TotalPaidReportFilterDto totalPaidReportFilterDto) {
        return totalPaidRepository.findPaidByPaymentDatePeriod(
                totalPaidReportFilterDto.periodCriteria().startDate(),
                totalPaidReportFilterDto.periodCriteria().endDate(),
                StatusPayableEnum.PAGO,
                totalPaidReportFilterDto.supplierId()
        );
    }
}
