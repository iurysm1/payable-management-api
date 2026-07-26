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

        payable.updateStatus(StatusPayableEnum.PAGO, LocalDate.of(2026, 8, 10));

        assertThat(payable.getStatus()).isEqualTo(StatusPayableEnum.PAGO);
        assertThat(payable.getPaymentDate()).isEqualTo(LocalDate.of(2026, 8, 10));
    }

    @Test
    void shouldRejectNullStatus() {
        Payable payable = Payable.create(
                "Aluguel", new BigDecimal("10.00"), StatusPayableEnum.PENDENTE,
                null, null, supplier());

        assertThatThrownBy(() -> payable.updateStatus(null, null))
                .isInstanceOf(InvalidPayableException.class)
                .hasMessage("O status da conta a pagar é obrigatório");
    }

    @Test
    void shouldRejectPaidPayableWithoutPaymentDate() {
        assertThatThrownBy(() -> Payable.create(
                "Aluguel", new BigDecimal("10.00"), StatusPayableEnum.PAGO,
                null, null, supplier()))
                .isInstanceOf(InvalidPayableException.class)
                .hasMessage("A data de pagamento é obrigatória quando uma conta está com status PAGO");
    }

    @Test
    void shouldRejectUnpaidPayableWithPaymentDate() {
        assertThatThrownBy(() -> Payable.create(
                "Aluguel", new BigDecimal("10.00"), StatusPayableEnum.PENDENTE,
                null, LocalDate.of(2026, 8, 10), supplier()))
                .isInstanceOf(InvalidPayableException.class)
                .hasMessage("A data de pagamento deve ser nula quando status é diferente de PAGO");
    }

    @Test
    void shouldClearPaymentDateWhenChangingFromPaidToUnpaid() {
        Payable payable = Payable.create(
                "Aluguel", new BigDecimal("10.00"), StatusPayableEnum.PAGO,
                null, LocalDate.of(2026, 8, 10), supplier());

        payable.updateStatus(StatusPayableEnum.PENDENTE, null);

        assertThat(payable.getStatus()).isEqualTo(StatusPayableEnum.PENDENTE);
        assertThat(payable.getPaymentDate()).isNull();
    }

    @Test
    void shouldKeepExistingPaymentDateWhenPaidStatusIsRepeatedWithoutDate() {
        Payable payable = Payable.create(
                "Aluguel", new BigDecimal("10.00"), StatusPayableEnum.PAGO,
                null, LocalDate.of(2026, 8, 10), supplier());

        payable.updateStatus(StatusPayableEnum.PAGO, null);

        assertThat(payable.getPaymentDate()).isEqualTo(LocalDate.of(2026, 8, 10));
    }

    private Supplier supplier() {
        return new Supplier(1L, "Fornecedor");
    }
}
