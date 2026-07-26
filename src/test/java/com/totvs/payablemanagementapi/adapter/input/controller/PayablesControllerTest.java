package com.totvs.payablemanagementapi.adapter.input.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.totvs.payablemanagementapi.core.exception.PayableNotFoundException;
import com.totvs.payablemanagementapi.core.port.input.PayableUseCase;
import com.totvs.payablemanagementapi.core.port.input.dto.PayableDto;
import com.totvs.payablemanagementapi.core.port.input.dto.PayableFilterDto;
import com.totvs.payablemanagementapi.core.port.input.dto.UpdatePayableStatusDto;
import com.totvs.payablemanagementapi.core.util.DatePeriodCriteria;
import com.totvs.payablemanagementapi.domain.Payable;
import com.totvs.payablemanagementapi.domain.Supplier;
import com.totvs.payablemanagementapi.domain.enums.StatusPayableEnum;
import com.totvs.payablemanagementapi.domain.exception.InvalidPayableException;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PayablesController.class)
@Import(ApiExceptionHandler.class)
class PayablesControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private PayableUseCase payableUseCase;

    @Test
    void shouldListPayables() throws Exception {
        Payable payable = payable(1L);
        when(payableUseCase.list(any(), any())).thenReturn(new PageImpl<>(List.of(payable), PageRequest.of(0, 10), 1));

        mockMvc.perform(get("/payable")
                        .param("description", "Aluguel")
                        .param("startDate", "2026-08-01")
                        .param("endDate", "2026-08-31")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].description").value("Aluguel"));

        ArgumentCaptor<PayableFilterDto> captor = ArgumentCaptor.forClass(PayableFilterDto.class);
        verify(payableUseCase).list(any(), captor.capture());
        assertThat(captor.getValue().description()).isEqualTo("Aluguel");
        assertThat(captor.getValue().status()).isNull();
    }

    @Test
    void shouldListPayablesFilteredByStatus() throws Exception {
        when(payableUseCase.list(any(), any())).thenReturn(Page.empty());

        mockMvc.perform(get("/payable").param("status", "pago"))
                .andExpect(status().isOk());

        ArgumentCaptor<PayableFilterDto> captor = ArgumentCaptor.forClass(PayableFilterDto.class);
        verify(payableUseCase).list(any(), captor.capture());
        assertThat(captor.getValue().status()).isEqualTo(StatusPayableEnum.PAGO);
    }

    @Test
    void shouldReturnCustomBadRequestWhenStatusNameDoesNotExist() throws Exception {
        mockMvc.perform(get("/payable").param("status", "em_analise"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value(
                        "Nome de status de conta a pagar inválido: em_analise"
                ));

        verifyNoInteractions(payableUseCase);
    }

    @Test
    void shouldListPayablesWithoutDatePeriod() throws Exception {
        when(payableUseCase.list(any(), any())).thenReturn(Page.empty());

        mockMvc.perform(get("/payable"))
                .andExpect(status().isOk());

        ArgumentCaptor<PayableFilterDto> captor = ArgumentCaptor.forClass(PayableFilterDto.class);
        verify(payableUseCase).list(any(), captor.capture());
        assertThat(captor.getValue().periodCriteria())
                .isEqualTo(new DatePeriodCriteria(null, null));
        assertThat(captor.getValue().status()).isNull();
    }

    @Test
    void shouldReturnBadRequestWhenStartDateIsAfterEndDate() throws Exception {
        mockMvc.perform(get("/payable")
                        .param("startDate", "2026-08-31")
                        .param("endDate", "2026-08-01"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value(
                        "O período de datas deve possuir data inicial e final, e a data inicial não pode ser posterior à data final"
                ));

        verifyNoInteractions(payableUseCase);
    }

    @Test
    void shouldReturnBadRequestWhenStartDateIsMissing() throws Exception {
        mockMvc.perform(get("/payable").param("endDate", "2026-08-31"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(payableUseCase);
    }

    @Test
    void shouldReturnBadRequestWhenEndDateIsMissing() throws Exception {
        mockMvc.perform(get("/payable").param("startDate", "2026-08-01"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(payableUseCase);
    }

    @Test
    void shouldFindPayableById() throws Exception {
        when(payableUseCase.findById(1L)).thenReturn(payable(1L));

        mockMvc.perform(get("/payable/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void shouldReturnNotFoundWhenPayableDoesNotExist() throws Exception {
        when(payableUseCase.findById(99L)).thenThrow(new PayableNotFoundException(99L));

        mockMvc.perform(get("/payable/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("Conta a pagar com id 99 não encontrada"));
    }

    @Test
    void shouldReturnBadRequestWhenPayableViolatesDomainRule() throws Exception {
        when(payableUseCase.save(any(PayableDto.class)))
                .thenThrow(new InvalidPayableException("O valor da conta a pagar não pode ser negativo"));

        mockMvc.perform(post("/payable")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payableDto(null, 1L))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("O valor da conta a pagar não pode ser negativo"));
    }

    @Test
    void shouldCreatePayable() throws Exception {
        when(payableUseCase.save(any(PayableDto.class))).thenReturn(payable(1L));

        mockMvc.perform(post("/payable")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payableDto(null, 1L))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void shouldRejectPayableWithoutAmount() throws Exception {
        PayableDto payableDto = new PayableDto(
                null,
                "Aluguel",
                null,
                StatusPayableEnum.PENDENTE,
                LocalDate.of(2026, 8, 10),
                null,
                1L
        );
        when(payableUseCase.save(any(PayableDto.class)))
                .thenThrow(new InvalidPayableException("O valor da conta a pagar é obrigatório"));

        mockMvc.perform(post("/payable")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payableDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldRejectPayableWithoutSupplierId() throws Exception {
        when(payableUseCase.save(any(PayableDto.class)))
                .thenThrow(new InvalidPayableException("O fornecedor da conta a pagar é obrigatório"));

        mockMvc.perform(post("/payable")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payableDto(null, null))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("O fornecedor da conta a pagar é obrigatório"));
    }

    @Test
    void shouldUpdatePayableUsingIdFromPath() throws Exception {
        Payable updatedPayable = payable(1L);
        updatedPayable.updateDetails(
                "Aluguel atualizado",
                updatedPayable.getAmount(),
                updatedPayable.getStatus(),
                updatedPayable.getExpirationDate(),
                updatedPayable.getPaymentDate(),
                updatedPayable.getSupplier()
        );
        when(payableUseCase.update(any(PayableDto.class))).thenReturn(updatedPayable);

        mockMvc.perform(put("/payable/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payableDto(99L, 1L))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description").value("Aluguel atualizado"));

        ArgumentCaptor<PayableDto> captor = ArgumentCaptor.forClass(PayableDto.class);
        verify(payableUseCase).update(captor.capture());
        assertThat(captor.getValue().id()).isEqualTo(1L);
        assertThat(captor.getValue().supplierId()).isEqualTo(1L);
    }

    @Test
    void shouldDeletePayable() throws Exception {
        mockMvc.perform(delete("/payable/1"))
                .andExpect(status().isNoContent());

        verify(payableUseCase).delete(1L);
    }

    @Test
    void shouldUpdatePayableStatus() throws Exception {
        LocalDate paymentDate = LocalDate.of(2026, 8, 10);
        Payable updatedPayable = new Payable(
                1L, "Aluguel", new BigDecimal("150.00"), StatusPayableEnum.PAGO,
                LocalDate.of(2026, 8, 10), paymentDate, new Supplier(1L, "Fornecedor")
        );
        when(payableUseCase.updateStatus(
                1L, new UpdatePayableStatusDto(StatusPayableEnum.PAGO.getCode(), paymentDate)
        )).thenReturn(updatedPayable);

        mockMvc.perform(patch("/payable/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\": 1, \"paymentDate\": \"2026-08-10\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PAGO"));

        verify(payableUseCase).updateStatus(
                1L, new UpdatePayableStatusDto(StatusPayableEnum.PAGO.getCode(), paymentDate)
        );
    }

    @Test
    void shouldReturnBadRequestWhenStatusIsMissing() throws Exception {
        mockMvc.perform(patch("/payable/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("O status da conta a pagar é obrigatório"));

        verifyNoInteractions(payableUseCase);
    }

    @Test
    void shouldReturnBadRequestWhenStatusIsNull() throws Exception {
        mockMvc.perform(patch("/payable/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\": null}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("O status da conta a pagar é obrigatório"));

        verifyNoInteractions(payableUseCase);
    }

    @Test
    void shouldReturnBadRequestWhenStatusCodeDoesNotExist() throws Exception {
        when(payableUseCase.updateStatus(1L, new UpdatePayableStatusDto(99, null)))
                .thenThrow(new IllegalArgumentException("Código de status de conta a pagar inválido: 99"));

        mockMvc.perform(patch("/payable/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\": 99}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("Código de status de conta a pagar inválido: 99"));

        verify(payableUseCase).updateStatus(1L, new UpdatePayableStatusDto(99, null));
    }

    private Payable payable(Long id) {
        return new Payable(
                id,
                "Aluguel",
                new BigDecimal("150.00"),
                StatusPayableEnum.PENDENTE,
                LocalDate.of(2026, 8, 10),
                null,
                new Supplier(1L, "Fornecedor")
        );
    }

    private PayableDto payableDto(Long id, Long supplierId) {
        return new PayableDto(
                id,
                "Aluguel",
                new BigDecimal("150.00"),
                StatusPayableEnum.PENDENTE,
                LocalDate.of(2026, 8, 10),
                null,
                supplierId
        );
    }
}
