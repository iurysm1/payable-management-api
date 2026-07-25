package com.totvs.payablemanagementapi.adapter.input.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.totvs.payablemanagementapi.core.exception.PayableNotFoundException;
import com.totvs.payablemanagementapi.core.port.input.PayableUseCase;
import com.totvs.payablemanagementapi.core.port.input.dto.PayableDto;
import com.totvs.payablemanagementapi.domain.Payable;
import com.totvs.payablemanagementapi.domain.Supplier;
import com.totvs.payablemanagementapi.domain.enums.StatusPayableEnum;
import com.totvs.payablemanagementapi.domain.exception.InvalidPayableException;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
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
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
        when(payableUseCase.list(any())).thenReturn(new PageImpl<>(List.of(payable), PageRequest.of(0, 10), 1));

        mockMvc.perform(get("/payables").param("page", "0").param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].description").value("Aluguel"));
    }

    @Test
    void shouldFindPayableById() throws Exception {
        when(payableUseCase.findById(1L)).thenReturn(payable(1L));

        mockMvc.perform(get("/payables/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void shouldReturnNotFoundWhenPayableDoesNotExist() throws Exception {
        when(payableUseCase.findById(99L)).thenThrow(new PayableNotFoundException(99L));

        mockMvc.perform(get("/payables/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("Conta a pagar com id 99 não encontrada"));
    }

    @Test
    void shouldReturnBadRequestWhenPayableViolatesDomainRule() throws Exception {
        when(payableUseCase.save(any(PayableDto.class)))
                .thenThrow(new InvalidPayableException("O valor da conta a pagar não pode ser negativo"));

        mockMvc.perform(post("/payables")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payableDto(null, 1L))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("O valor da conta a pagar não pode ser negativo"));
    }

    @Test
    void shouldCreatePayable() throws Exception {
        when(payableUseCase.save(any(PayableDto.class))).thenReturn(payable(1L));

        mockMvc.perform(post("/payables")
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

        mockMvc.perform(post("/payables")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payableDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldRejectPayableWithoutSupplierId() throws Exception {
        when(payableUseCase.save(any(PayableDto.class)))
                .thenThrow(new InvalidPayableException("O fornecedor da conta a pagar é obrigatório"));

        mockMvc.perform(post("/payables")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payableDto(null, null))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("O fornecedor da conta a pagar é obrigatório"));
    }

    @Test
    void shouldUpdatePayableUsingIdFromPath() throws Exception {
        Payable updatedPayable = payable(1L);
        updatedPayable.setDescription("Aluguel atualizado");
        when(payableUseCase.update(any(PayableDto.class))).thenReturn(updatedPayable);

        mockMvc.perform(put("/payables/1")
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
        mockMvc.perform(delete("/payables/1"))
                .andExpect(status().isNoContent());

        verify(payableUseCase).delete(1L);
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
