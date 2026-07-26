package com.totvs.payablemanagementapi.adapter.output.repository.reports;

import com.totvs.payablemanagementapi.core.port.input.dto.reports.TotalPaidReportFilterDto;
import com.totvs.payablemanagementapi.core.port.output.reports.TotalPaidPersistencePort;
import com.totvs.payablemanagementapi.domain.Payable;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class TotalPaidJpaAdapter implements TotalPaidPersistencePort {


    @Override
    public List<Payable> findPaidByPeriod(TotalPaidReportFilterDto totalPaidReportFilterDto) {
        return List.of();
    }
}
