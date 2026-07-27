package com.totvs.payablemanagementapi.adapter.input.controller.reports;

import com.totvs.payablemanagementapi.adapter.input.controller.ApiExceptionHandler;
import com.totvs.payablemanagementapi.adapter.input.security.SecurityConfiguration;
import com.totvs.payablemanagementapi.core.exception.SupplierNotFoundException;
import com.totvs.payablemanagementapi.core.exception.TotalPaidReportNotFoundException;
import com.totvs.payablemanagementapi.core.port.input.dto.reports.PaidPayableItemDto;
import com.totvs.payablemanagementapi.core.port.input.dto.reports.TotalPaidReportDto;
import com.totvs.payablemanagementapi.core.port.input.dto.reports.TotalPaidReportFilterDto;
import com.totvs.payablemanagementapi.core.port.input.reports.TotalPaidReportUseCase;
import com.totvs.payablemanagementapi.core.util.DatePeriodCriteriaRequired;
import com.totvs.payablemanagementapi.domain.enums.StatusPayableEnum;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = TotalPaidReportController.class,
        properties = {
                "app.security.username=test-user",
                "app.security.password=test-password"
        }
)
@Import({ApiExceptionHandler.class, SecurityConfiguration.class})
@WithMockUser
class TotalPaidReportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TotalPaidReportUseCase totalPaidReportUseCase;

    @Test
    void shouldReturnTotalPaidReportWithSupplierFilter() throws Exception {
        when(totalPaidReportUseCase.processReport(any())).thenReturn(report());

        mockMvc.perform(get("/report/total-paid")
                        .param("supplierId", "10")
                        .param("startDate", "2026-08-01")
                        .param("endDate", "2026-08-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.startDate").value("2026-08-01"))
                .andExpect(jsonPath("$.endDate").value("2026-08-31"))
                .andExpect(jsonPath("$.totalPaid").value(239.90))
                .andExpect(jsonPath("$.paidCount").value(2))
                .andExpect(jsonPath("$.paidPayableItems[0].description").value("Aluguel"));

        ArgumentCaptor<TotalPaidReportFilterDto> captor =
                ArgumentCaptor.forClass(TotalPaidReportFilterDto.class);
        verify(totalPaidReportUseCase).processReport(captor.capture());
        assertThat(captor.getValue().supplierId()).isEqualTo(10L);
        assertThat(captor.getValue().periodCriteria())
                .isEqualTo(new DatePeriodCriteriaRequired(
                        LocalDate.of(2026, 8, 1), LocalDate.of(2026, 8, 31)
                ));
    }

    @Test
    void shouldReturnTotalPaidReportForAllSuppliersWhenSupplierIdIsAbsent() throws Exception {
        when(totalPaidReportUseCase.processReport(any())).thenReturn(report());

        mockMvc.perform(get("/report/total-paid")
                        .param("startDate", "2026-08-01")
                        .param("endDate", "2026-08-31"))
                .andExpect(status().isOk());

        ArgumentCaptor<TotalPaidReportFilterDto> captor =
                ArgumentCaptor.forClass(TotalPaidReportFilterDto.class);
        verify(totalPaidReportUseCase).processReport(captor.capture());
        assertThat(captor.getValue().supplierId()).isNull();
    }

    @Test
    void shouldReturnBadRequestWhenStartDateIsMissing() throws Exception {
        mockMvc.perform(get("/report/total-paid").param("endDate", "2026-08-31"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(totalPaidReportUseCase);
    }

    @Test
    void shouldReturnBadRequestWhenEndDateIsMissing() throws Exception {
        mockMvc.perform(get("/report/total-paid").param("startDate", "2026-08-01"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(totalPaidReportUseCase);
    }

    @Test
    void shouldReturnBadRequestWhenStartDateIsAfterEndDate() throws Exception {
        mockMvc.perform(get("/report/total-paid")
                        .param("startDate", "2026-08-31")
                        .param("endDate", "2026-08-01"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value(
                        "O período de datas deve possuir data inicial e final, e a data inicial não pode ser posterior à data final"
                ));

        verifyNoInteractions(totalPaidReportUseCase);
    }

    @Test
    void shouldReturnNotFoundWhenSupplierDoesNotExist() throws Exception {
        when(totalPaidReportUseCase.processReport(any()))
                .thenThrow(new SupplierNotFoundException(99L));

        mockMvc.perform(get("/report/total-paid")
                        .param("supplierId", "99")
                        .param("startDate", "2026-08-01")
                        .param("endDate", "2026-08-31"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("Fornecedor com id 99 não encontrado"));
    }

    @Test
    void shouldReturnNotFoundWhenNoPaidPayablesAreFound() throws Exception {
        when(totalPaidReportUseCase.processReport(any()))
                .thenThrow(new TotalPaidReportNotFoundException());

        mockMvc.perform(get("/report/total-paid")
                        .param("startDate", "2026-08-01")
                        .param("endDate", "2026-08-31"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value(
                        "Nenhuma conta paga encontrada para o período informado"
                ));
    }

    private TotalPaidReportDto report() {
        return new TotalPaidReportDto(
                LocalDate.of(2026, 8, 1),
                LocalDate.of(2026, 8, 31),
                new BigDecimal("239.90"),
                2,
                List.of(
                        item("Aluguel", "150.00"),
                        item("Internet", "89.90")
                )
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
