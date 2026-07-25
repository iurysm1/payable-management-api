package com.totvs.payablemanagementapi.adapter.output.repository;

import com.totvs.payablemanagementapi.core.port.input.dto.PayableFilterDto;
import com.totvs.payablemanagementapi.core.util.DatePeriodCriteria;
import com.totvs.payablemanagementapi.domain.Payable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PayableJpaAdapterTest {

    @Mock
    private PayableRepository payableRepository;

    @InjectMocks
    private PayableJpaAdapter payableJpaAdapter;

    @Test
    void shouldForwardDescriptionAndDatePeriodToRepository() {
        var pageable = PageRequest.of(0, 10);
        var startDate = LocalDate.of(2026, 8, 1);
        var endDate = LocalDate.of(2026, 8, 31);
        var filter = new PayableFilterDto(
                "Aluguel", new DatePeriodCriteria(startDate, endDate)
        );
        Page<Payable> page = new PageImpl<>(List.of(), pageable, 0);
        when(payableRepository.findAllByFilters("Aluguel", startDate, endDate, pageable))
                .thenReturn(page);

        Page<Payable> result = payableJpaAdapter.findAll(pageable, filter);

        assertThat(result).isSameAs(page);
        verify(payableRepository).findAllByFilters("Aluguel", startDate, endDate, pageable);
    }

    @Test
    void shouldForwardNullDatesWhenThereIsNoDatePeriod() {
        var pageable = PageRequest.of(0, 10);
        var filter = new PayableFilterDto(null, new DatePeriodCriteria(null, null));
        Page<Payable> page = new PageImpl<>(List.of(), pageable, 0);
        when(payableRepository.findAllByFilters(null, null, null, pageable)).thenReturn(page);

        Page<Payable> result = payableJpaAdapter.findAll(pageable, filter);

        assertThat(result).isSameAs(page);
        verify(payableRepository).findAllByFilters(null, null, null, pageable);
    }
}
