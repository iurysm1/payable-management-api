package com.totvs.payablemanagementapi.core.service;

import com.totvs.payablemanagementapi.core.exception.SupplierInUseException;
import com.totvs.payablemanagementapi.core.exception.SupplierNotFoundException;
import com.totvs.payablemanagementapi.core.port.input.dto.SupplierDto;
import com.totvs.payablemanagementapi.core.port.output.PayablePersistencePort;
import com.totvs.payablemanagementapi.core.port.output.SupplierPersistencePort;
import com.totvs.payablemanagementapi.domain.Supplier;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SupplierServiceTest {

    @Mock
    private SupplierPersistencePort supplierPersistencePort;

    @Mock
    private PayablePersistencePort payablePersistencePort;

    @InjectMocks
    private SupplierService supplierService;

    @Test
    void shouldListSuppliers() {
        var pageable = PageRequest.of(0, 10);
        Page<Supplier> page = new PageImpl<>(List.of(supplier(1L, "TOTVS")), pageable, 1);
        when(supplierPersistencePort.findAll(pageable)).thenReturn(page);

        Page<Supplier> result = supplierService.list(pageable);

        assertThat(result).isSameAs(page);
        verify(supplierPersistencePort).findAll(pageable);
    }

    @Test
    void shouldFindSupplierById() {
        Supplier supplier = supplier(1L, "TOTVS");
        when(supplierPersistencePort.findById(1L)).thenReturn(Optional.of(supplier));

        Supplier result = supplierService.findById(1L);

        assertThat(result).isSameAs(supplier);
    }

    @Test
    void shouldThrowWhenSupplierDoesNotExist() {
        when(supplierPersistencePort.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> supplierService.findById(99L))
                .isInstanceOf(SupplierNotFoundException.class)
                .hasMessage("Fornecedor com id 99 não encontrado");
    }

    @Test
    void shouldSaveSupplier() {
        SupplierDto supplierDto = supplierDto(null, "TOTVS");
        Supplier savedSupplier = supplier(1L, "TOTVS");
        when(supplierPersistencePort.save(any(Supplier.class))).thenReturn(savedSupplier);

        Supplier result = supplierService.save(supplierDto);

        assertThat(result).isSameAs(savedSupplier);
        ArgumentCaptor<Supplier> captor = ArgumentCaptor.forClass(Supplier.class);
        verify(supplierPersistencePort).save(captor.capture());
        assertThat(captor.getValue().getId()).isNull();
        assertThat(captor.getValue().getName()).isEqualTo("TOTVS");
    }

    @Test
    void shouldUpdateExistingSupplier() {
        Supplier existingSupplier = supplier(1L, "Nome antigo");
        SupplierDto updatedValues = supplierDto(1L, "Nome atualizado");
        when(supplierPersistencePort.findById(1L)).thenReturn(Optional.of(existingSupplier));
        when(supplierPersistencePort.save(existingSupplier)).thenReturn(existingSupplier);

        Supplier result = supplierService.update(updatedValues);

        assertThat(result.getName()).isEqualTo("Nome atualizado");
        verify(supplierPersistencePort).save(existingSupplier);
    }

    @Test
    void shouldDeleteSupplierWithoutPayables() {
        Supplier supplier = supplier(1L, "TOTVS");
        when(supplierPersistencePort.findById(1L)).thenReturn(Optional.of(supplier));
        when(payablePersistencePort.existsBySupplierId(1L)).thenReturn(false);

        supplierService.delete(1L);

        verify(supplierPersistencePort).delete(supplier);
    }

    @Test
    void shouldBlockDeletionOfSupplierWithPayables() {
        Supplier supplier = supplier(1L, "TOTVS");
        when(supplierPersistencePort.findById(1L)).thenReturn(Optional.of(supplier));
        when(payablePersistencePort.existsBySupplierId(1L)).thenReturn(true);

        assertThatThrownBy(() -> supplierService.delete(1L))
                .isInstanceOf(SupplierInUseException.class)
                .hasMessage("Fornecedor com id 1 possui contas a pagar vinculadas");

        verify(supplierPersistencePort, never()).delete(supplier);
    }

    private Supplier supplier(Long id, String name) {
        return new Supplier(id, name);
    }

    private SupplierDto supplierDto(Long id, String name) {
        return new SupplierDto(id, name);
    }
}
