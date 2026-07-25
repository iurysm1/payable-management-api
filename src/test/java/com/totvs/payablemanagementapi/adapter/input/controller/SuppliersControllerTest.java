package com.totvs.payablemanagementapi.adapter.input.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.totvs.payablemanagementapi.core.exception.SupplierInUseException;
import com.totvs.payablemanagementapi.core.exception.SupplierNotFoundException;
import com.totvs.payablemanagementapi.core.port.input.SupplierUseCase;
import com.totvs.payablemanagementapi.domain.Supplier;
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

@WebMvcTest(SuppliersController.class)
@Import(ApiExceptionHandler.class)
class SuppliersControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private SupplierUseCase supplierUseCase;

    @Test
    void shouldListSuppliers() throws Exception {
        when(supplierUseCase.list(any())).thenReturn(new PageImpl<>(List.of(supplier(1L, "TOTVS")), PageRequest.of(0, 10), 1));

        mockMvc.perform(get("/suppliers").param("page", "0").param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].name").value("TOTVS"));
    }

    @Test
    void shouldFindSupplierById() throws Exception {
        when(supplierUseCase.findById(1L)).thenReturn(supplier(1L, "TOTVS"));

        mockMvc.perform(get("/suppliers/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("TOTVS"));
    }

    @Test
    void shouldReturnNotFoundWhenSupplierDoesNotExist() throws Exception {
        when(supplierUseCase.findById(99L)).thenThrow(new SupplierNotFoundException(99L));

        mockMvc.perform(get("/suppliers/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("Fornecedor com id 99 não encontrado"));
    }

    @Test
    void shouldCreateSupplier() throws Exception {
        when(supplierUseCase.save(any(Supplier.class))).thenReturn(supplier(1L, "TOTVS"));

        mockMvc.perform(post("/suppliers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(supplier(null, "TOTVS"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void shouldRejectSupplierWithoutName() throws Exception {
        mockMvc.perform(post("/suppliers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldUpdateSupplierUsingIdFromPath() throws Exception {
        when(supplierUseCase.update(any(Supplier.class))).thenReturn(supplier(1L, "Nome atualizado"));

        mockMvc.perform(put("/suppliers/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(supplier(null, "Nome atualizado"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Nome atualizado"));

        ArgumentCaptor<Supplier> captor = ArgumentCaptor.forClass(Supplier.class);
        verify(supplierUseCase).update(captor.capture());
        assertThat(captor.getValue().getId()).isEqualTo(1L);
    }

    @Test
    void shouldDeleteSupplier() throws Exception {
        mockMvc.perform(delete("/suppliers/1"))
                .andExpect(status().isNoContent());

        verify(supplierUseCase).delete(1L);
    }

    @Test
    void shouldReturnConflictWhenSupplierHasPayables() throws Exception {
        org.mockito.Mockito.doThrow(new SupplierInUseException(1L)).when(supplierUseCase).delete(1L);

        mockMvc.perform(delete("/suppliers/1"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value("Fornecedor com id 1 possui contas a pagar vinculadas"));
    }

    private Supplier supplier(Long id, String name) {
        return new Supplier(id, name);
    }
}
