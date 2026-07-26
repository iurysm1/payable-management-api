package com.totvs.payablemanagementapi.adapter.output.repository.reports;

import com.totvs.payablemanagementapi.core.port.input.dto.reports.PaidPayableItemDto;
import com.totvs.payablemanagementapi.core.port.input.dto.reports.TotalPaidReportFilterDto;
import com.totvs.payablemanagementapi.core.util.DatePeriodCriteriaRequired;
import com.totvs.payablemanagementapi.domain.enums.StatusPayableEnum;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TotalPaidJpaAdapterTest {

    private static final LocalDate START_DATE = LocalDate.of(2026, 8, 1);
    private static final LocalDate END_DATE = LocalDate.of(2026, 8, 31);

    @Mock
    private TotalPaidRepository totalPaidRepository;

    @InjectMocks
    private TotalPaidJpaAdapter totalPaidJpaAdapter;

    @Test
    void shouldFilterByPaidStatusAndSupplierWhenSupplierIdIsProvided() {
        var filter = new TotalPaidReportFilterDto(
                10L, new DatePeriodCriteriaRequired(START_DATE, END_DATE)
        );
        List<PaidPayableItemDto> payables = List.of();
        when(totalPaidRepository.findPaidByPaymentDatePeriod(
                START_DATE, END_DATE, StatusPayableEnum.PAGO, 10L
        )).thenReturn(payables);

        List<PaidPayableItemDto> result = totalPaidJpaAdapter.findPaidByPeriod(filter);

        assertThat(result).isSameAs(payables);
        verify(totalPaidRepository).findPaidByPaymentDatePeriod(
                START_DATE, END_DATE, StatusPayableEnum.PAGO, 10L
        );
    }

    @Test
    void shouldSearchAllSuppliersWhenSupplierIdIsNotProvided() {
        var filter = new TotalPaidReportFilterDto(
                null, new DatePeriodCriteriaRequired(START_DATE, END_DATE)
        );
        List<PaidPayableItemDto> payables = List.of();
        when(totalPaidRepository.findPaidByPaymentDatePeriod(
                START_DATE, END_DATE, StatusPayableEnum.PAGO, null
        )).thenReturn(payables);

        List<PaidPayableItemDto> result = totalPaidJpaAdapter.findPaidByPeriod(filter);

        assertThat(result).isSameAs(payables);
        verify(totalPaidRepository).findPaidByPaymentDatePeriod(
                START_DATE, END_DATE, StatusPayableEnum.PAGO, null
        );
    }
}
