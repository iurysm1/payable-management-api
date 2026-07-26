package com.totvs.payablemanagementapi.core.service.reports;

import com.totvs.payablemanagementapi.core.exception.SupplierNotFoundException;
import com.totvs.payablemanagementapi.core.exception.TotalPaidReportNotFoundException;
import com.totvs.payablemanagementapi.core.port.input.dto.reports.PaidPayableItemDto;
import com.totvs.payablemanagementapi.core.port.input.dto.reports.TotalPaidReportDto;
import com.totvs.payablemanagementapi.core.port.input.dto.reports.TotalPaidReportFilterDto;
import com.totvs.payablemanagementapi.core.port.output.reports.TotalPaidPersistencePort;
import com.totvs.payablemanagementapi.core.service.SupplierService;
import com.totvs.payablemanagementapi.core.util.DatePeriodCriteriaRequired;
import com.totvs.payablemanagementapi.domain.Supplier;
import com.totvs.payablemanagementapi.domain.enums.StatusPayableEnum;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TotalPaidReportServiceTest {

    private static final LocalDate START_DATE = LocalDate.of(2026, 8, 1);
    private static final LocalDate END_DATE = LocalDate.of(2026, 8, 31);

    @Mock
    private SupplierService supplierService;

    @Mock
    private TotalPaidPersistencePort totalPaidPersistencePort;

    @InjectMocks
    private TotalPaidReportService totalPaidReportService;

    @Test
    void shouldCreateReportForSupplierWithDatesTotalCountAndItems() {
        var filter = filter(10L);
        List<PaidPayableItemDto> items = List.of(
                item("Aluguel", "150.00"),
                item("Internet", "89.90")
        );
        when(supplierService.findById(10L)).thenReturn(new Supplier(10L, "TOTVS"));
        when(totalPaidPersistencePort.findPaidByPeriod(filter)).thenReturn(items);

        TotalPaidReportDto report = totalPaidReportService.processReport(filter);

        assertThat(report.startDate()).isEqualTo(START_DATE);
        assertThat(report.endDate()).isEqualTo(END_DATE);
        assertThat(report.totalPaid()).isEqualByComparingTo("239.90");
        assertThat(report.paidCount()).isEqualTo(2);
        assertThat(report.paidPayableItems()).isSameAs(items);
        verify(supplierService).findById(10L);
        verify(totalPaidPersistencePort).findPaidByPeriod(filter);
    }

    @Test
    void shouldCreateReportForAllSuppliersWhenSupplierIdIsAbsent() {
        var filter = filter(null);
        List<PaidPayableItemDto> items = List.of(item("Aluguel", "150.00"));
        when(totalPaidPersistencePort.findPaidByPeriod(filter)).thenReturn(items);

        TotalPaidReportDto report = totalPaidReportService.processReport(filter);

        assertThat(report.totalPaid()).isEqualByComparingTo("150.00");
        assertThat(report.paidCount()).isEqualTo(1);
        assertThat(report.paidPayableItems()).isSameAs(items);
        verifyNoInteractions(supplierService);
        verify(totalPaidPersistencePort).findPaidByPeriod(filter);
    }

    @Test
    void shouldThrowNotFoundWhenNoPaidPayablesAreFound() {
        var filter = filter(10L);
        when(supplierService.findById(10L)).thenReturn(new Supplier(10L, "TOTVS"));
        when(totalPaidPersistencePort.findPaidByPeriod(filter)).thenReturn(List.of());

        assertThatThrownBy(() -> totalPaidReportService.processReport(filter))
                .isInstanceOf(TotalPaidReportNotFoundException.class)
                .hasMessage("Nenhuma conta paga encontrada para o período informado");
    }

    @Test
    void shouldNotQueryPayablesWhenSupplierDoesNotExist() {
        var filter = filter(99L);
        when(supplierService.findById(99L)).thenThrow(new SupplierNotFoundException(99L));

        assertThatThrownBy(() -> totalPaidReportService.processReport(filter))
                .isInstanceOf(SupplierNotFoundException.class)
                .hasMessage("Fornecedor com id 99 não encontrado");

        verify(totalPaidPersistencePort, never()).findPaidByPeriod(filter);
    }

    private TotalPaidReportFilterDto filter(Long supplierId) {
        return new TotalPaidReportFilterDto(
                supplierId, new DatePeriodCriteriaRequired(START_DATE, END_DATE)
        );
    }

    private PaidPayableItemDto item(String description, String amount) {
        return new PaidPayableItemDto(
                description,
                new BigDecimal(amount),
                StatusPayableEnum.PAGO,
                LocalDate.of(2026, 8, 10),
                LocalDate.of(2026, 8, 5),
                "TOTVS"
        );
    }
}
