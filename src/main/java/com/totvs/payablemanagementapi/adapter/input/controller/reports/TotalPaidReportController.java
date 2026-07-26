package com.totvs.payablemanagementapi.adapter.input.controller.reports;

import com.totvs.payablemanagementapi.core.port.input.dto.reports.TotalPaidReportDto;
import com.totvs.payablemanagementapi.core.port.input.dto.reports.TotalPaidReportFilterDto;
import com.totvs.payablemanagementapi.core.port.input.reports.TotalPaidReportUseCase;
import com.totvs.payablemanagementapi.core.util.DatePeriodCriteriaRequired;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/report")
@RequiredArgsConstructor
public class TotalPaidReportController {

    private final TotalPaidReportUseCase totalPaidReportUseCase;

    @GetMapping("/total-paid")
    public ResponseEntity<TotalPaidReportDto> totalPaid(
            @RequestParam(required = false) Long supplierId,
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate
    ) {
        TotalPaidReportFilterDto filter = new TotalPaidReportFilterDto(
                supplierId,
                new DatePeriodCriteriaRequired(startDate, endDate)
        );

        return ResponseEntity.ok(totalPaidReportUseCase.processReport(filter));
    }
}
