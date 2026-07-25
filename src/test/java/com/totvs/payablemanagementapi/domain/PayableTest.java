package com.totvs.payablemanagementapi.domain;

import com.totvs.payablemanagementapi.domain.enums.StatusPayableEnum;
import com.totvs.payablemanagementapi.domain.exception.InvalidPayableException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PayableTest {

    @Test
    void shouldRejectNullDescription() {
        assertThatThrownBy(() -> Payable.create(
                null, new BigDecimal("10.00"), null, null, null, supplier()))
                .isInstanceOf(InvalidPayableException.class)
                .hasMessage("A descrição da conta a pagar é obrigatória");
    }

    @Test
    void shouldRejectBlankDescription() {
        assertThatThrownBy(() -> Payable.create(
                "   ", new BigDecimal("10.00"), null, null, null, supplier()))
                .isInstanceOf(InvalidPayableException.class)
                .hasMessage("A descrição da conta a pagar é obrigatória");
    }

    @Test
    void shouldRejectNullAmount() {
        assertThatThrownBy(() -> Payable.create(
                "Aluguel", null, null, null, null, supplier()))
                .isInstanceOf(InvalidPayableException.class)
                .hasMessage("O valor da conta a pagar é obrigatório");
    }

    @Test
    void shouldRejectNegativeAmount() {
        assertThatThrownBy(() -> Payable.create(
                "Aluguel", new BigDecimal("-0.01"), null, null, null, supplier()))
                .isInstanceOf(InvalidPayableException.class)
                .hasMessage("O valor da conta a pagar não pode ser negativo");
    }

    @Test
    void shouldAcceptZeroAmount() {
        assertThatCode(() -> Payable.create(
                "Aluguel", BigDecimal.ZERO, null, LocalDate.now(), null, supplier()))
                .doesNotThrowAnyException();
    }

    @Test
    void shouldRejectNullSupplier() {
        assertThatThrownBy(() -> Payable.create(
                "Aluguel", new BigDecimal("10.00"), null, null, null, null))
                .isInstanceOf(InvalidPayableException.class)
                .hasMessage("O fornecedor da conta a pagar é obrigatório");
    }

    @Test
    void shouldUpdateStatus() {
        Payable payable = Payable.create(
                "Aluguel", new BigDecimal("10.00"), StatusPayableEnum.PENDENTE,
                null, null, supplier());

        payable.updateStatus(StatusPayableEnum.PAGO);

        assertThat(payable.getStatus()).isEqualTo(StatusPayableEnum.PAGO);
    }

    @Test
    void shouldRejectNullStatus() {
        Payable payable = Payable.create(
                "Aluguel", new BigDecimal("10.00"), StatusPayableEnum.PENDENTE,
                null, null, supplier());

        assertThatThrownBy(() -> payable.updateStatus(null))
                .isInstanceOf(InvalidPayableException.class)
                .hasMessage("O status da conta a pagar é obrigatório");
    }

    private Supplier supplier() {
        return new Supplier(1L, "Fornecedor");
    }
}
