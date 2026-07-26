package com.totvs.payablemanagementapi.domain.enums;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class StatusPayableEnumTest {

    @Test
    void shouldExposeConfiguredCodesAndDescriptions() {
        assertThat(StatusPayableEnum.PENDENTE.getCode()).isZero();
        assertThat(StatusPayableEnum.PENDENTE.getDescription()).isEqualTo("Conta pendente");
        assertThat(StatusPayableEnum.PAGO.getCode()).isEqualTo(1);
        assertThat(StatusPayableEnum.PAGO.getDescription()).isEqualTo("Conta paga");
        assertThat(StatusPayableEnum.CANCELADO.getCode()).isEqualTo(2);
        assertThat(StatusPayableEnum.CANCELADO.getDescription()).isEqualTo("Conta cancelada");
    }

    @Test
    void shouldFindStatusByCode() {
        assertThat(StatusPayableEnum.fromCode(0)).isEqualTo(StatusPayableEnum.PENDENTE);
        assertThat(StatusPayableEnum.fromCode(1)).isEqualTo(StatusPayableEnum.PAGO);
        assertThat(StatusPayableEnum.fromCode(2)).isEqualTo(StatusPayableEnum.CANCELADO);
    }

    @Test
    void shouldFindStatusByNameIgnoringCase() {
        assertThat(StatusPayableEnum.fromName("PAGO")).isEqualTo(StatusPayableEnum.PAGO);
        assertThat(StatusPayableEnum.fromName("pago")).isEqualTo(StatusPayableEnum.PAGO);
        assertThat(StatusPayableEnum.fromName("pEnDeNtE")).isEqualTo(StatusPayableEnum.PENDENTE);
    }

    @Test
    void shouldRejectInvalidStatusName() {
        assertThatThrownBy(() -> StatusPayableEnum.fromName("EM_ANALISE"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Nome de status de conta a pagar inválido: EM_ANALISE");
    }
}
