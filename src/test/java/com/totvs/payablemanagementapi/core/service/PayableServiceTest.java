package com.totvs.payablemanagementapi.core.service;

import com.totvs.payablemanagementapi.core.exception.PayableNotFoundException;
import com.totvs.payablemanagementapi.core.exception.SupplierNotFoundException;
import com.totvs.payablemanagementapi.core.port.input.dto.PayableDto;
import com.totvs.payablemanagementapi.core.port.output.PayablePersistencePort;
import com.totvs.payablemanagementapi.core.port.output.SupplierPersistencePort;
import com.totvs.payablemanagementapi.domain.Payable;
import com.totvs.payablemanagementapi.domain.Supplier;
import com.totvs.payablemanagementapi.domain.enums.StatusPayableEnum;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PayableServiceTest {

    @Mock
    private PayablePersistencePort payablePersistencePort;

    @Mock
    private SupplierPersistencePort supplierPersistencePort;

    @InjectMocks
    private PayableService payableService;

    private Payable defaultPayable;

    @BeforeEach
    void setUp() {
        defaultPayable = payable(1L);
        when(payablePersistencePort.findById(1L)).thenReturn(Optional.of(defaultPayable));
    }

    @Test
    void shouldListPayables() {
        var pageable = PageRequest.of(0, 10);
        Page<Payable> page = new PageImpl<>(List.of(payable(1L)), pageable, 1);
        when(payablePersistencePort.findAll(pageable)).thenReturn(page);

        Page<Payable> result = payableService.list(pageable);

        assertThat(result).isSameAs(page);
        verify(payablePersistencePort).findAll(pageable);
    }

    @Test
    void shouldFindPayableById() {
        Payable result = payableService.findById(1L);

        assertThat(result).isSameAs(defaultPayable);
    }

    @Test
    void shouldThrowWhenPayableDoesNotExist() {
        when(payablePersistencePort.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> payableService.findById(99L))
                .isInstanceOf(PayableNotFoundException.class)
                .hasMessage("Conta a pagar com id 99 não encontrada");
    }

    @Test
    void shouldSavePayable() {
        PayableDto payableDto = payableDto(null, 1L);
        Payable savedPayable = payable(1L);
        Supplier supplier = supplier(1L, "Fornecedor");
        when(supplierPersistencePort.findById(1L)).thenReturn(Optional.of(supplier));
        when(payablePersistencePort.save(any(Payable.class))).thenReturn(savedPayable);

        Payable result = payableService.save(payableDto);

        assertThat(result).isSameAs(savedPayable);
        ArgumentCaptor<Payable> captor = ArgumentCaptor.forClass(Payable.class);
        verify(payablePersistencePort).save(captor.capture());
        assertThat(captor.getValue().getSupplier()).isSameAs(supplier);
    }

    @Test
    void shouldUpdateExistingPayable() {
        PayableDto updatedValues = new PayableDto(
                1L,
                "Aluguel atualizado",
                new BigDecimal("250.00"),
                StatusPayableEnum.PAGO,
                LocalDate.of(2026, 8, 15),
                LocalDate.of(2026, 8, 10),
                2L
        );
        Supplier supplier = supplier(2L, "Novo fornecedor");
        when(supplierPersistencePort.findById(2L)).thenReturn(Optional.of(supplier));
        when(payablePersistencePort.save(defaultPayable)).thenReturn(defaultPayable);

        Payable result = payableService.update(updatedValues);

        assertThat(result.getDescription()).isEqualTo("Aluguel atualizado");
        assertThat(result.getAmount()).isEqualByComparingTo("250.00");
        assertThat(result.getSupplier()).isSameAs(supplier);
        verify(payablePersistencePort).save(defaultPayable);
    }

    @Test
    void shouldThrowWhenSupplierDoesNotExist() {
        PayableDto payableDto = payableDto(null, 99L);
        when(supplierPersistencePort.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> payableService.save(payableDto))
                .isInstanceOf(SupplierNotFoundException.class)
                .hasMessage("Fornecedor com id 99 não encontrado");
    }

    @Test
    void shouldRejectPayableWithoutSupplierId() {
        PayableDto payableDto = payableDto(null, null);

        assertThatThrownBy(() -> payableService.save(payableDto))
                .isInstanceOf(com.totvs.payablemanagementapi.domain.exception.InvalidPayableException.class)
                .hasMessage("O fornecedor da conta a pagar é obrigatório");
    }

    @Test
    void shouldDeleteExistingPayable() {
        payableService.delete(1L);

        verify(payablePersistencePort).delete(defaultPayable);
    }

    private Payable payable(Long id) {
        return new Payable(
                id,
                "Aluguel",
                new BigDecimal("150.00"),
                StatusPayableEnum.PENDENTE,
                LocalDate.of(2026, 8, 10),
                null,
                supplier(1L, "Fornecedor")
        );
    }

    private Supplier supplier(Long id, String name) {
        return new Supplier(id, name);
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
