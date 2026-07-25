package com.totvs.payablemanagementapi.adapter.output.repository.converter;

import com.totvs.payablemanagementapi.domain.enums.StatusPayableEnum;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class StatusPayableJpaConverterTest {

    private final StatusPayableJpaConverter converter = new StatusPayableJpaConverter();

    @Test
    void shouldConvertStatusToDatabaseCode() {
        assertThat(converter.convertToDatabaseColumn(StatusPayableEnum.PENDENTE)).isEqualTo((short) 0);
        assertThat(converter.convertToDatabaseColumn(StatusPayableEnum.PAGO)).isEqualTo((short) 1);
        assertThat(converter.convertToDatabaseColumn(StatusPayableEnum.CANCELADO)).isEqualTo((short) 2);
    }

    @Test
    void shouldConvertDatabaseCodeToStatus() {
        assertThat(converter.convertToEntityAttribute((short) 0)).isEqualTo(StatusPayableEnum.PENDENTE);
        assertThat(converter.convertToEntityAttribute((short) 1)).isEqualTo(StatusPayableEnum.PAGO);
        assertThat(converter.convertToEntityAttribute((short) 2)).isEqualTo(StatusPayableEnum.CANCELADO);
    }

    @Test
    void shouldRejectUnknownDatabaseCode() {
        assertThatThrownBy(() -> converter.convertToEntityAttribute((short) 9))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Código de status de conta a pagar inválido: 9");
    }
}
