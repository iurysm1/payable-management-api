package com.totvs.payablemanagementapi.adapter.output.repository.converter;

import com.totvs.payablemanagementapi.domain.enums.StatusPayableImportationItemEnum;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class StatusPayableImportationItemJpaConverterTest {

    private final StatusPayableImportationItemJpaConverter converter = new StatusPayableImportationItemJpaConverter();

    @Test
    void shouldConvertStatusToDatabaseCode() {
        assertThat(converter.convertToDatabaseColumn(StatusPayableImportationItemEnum.SUCCESS)).isEqualTo((short) 0);
        assertThat(converter.convertToDatabaseColumn(StatusPayableImportationItemEnum.ERROR)).isEqualTo((short) 1);
        assertThat(converter.convertToDatabaseColumn(null)).isNull();
    }

    @Test
    void shouldConvertDatabaseCodeToStatus() {
        assertThat(converter.convertToEntityAttribute((short) 0)).isEqualTo(StatusPayableImportationItemEnum.SUCCESS);
        assertThat(converter.convertToEntityAttribute((short) 1)).isEqualTo(StatusPayableImportationItemEnum.ERROR);
        assertThat(converter.convertToEntityAttribute(null)).isNull();
    }

    @Test
    void shouldRejectUnknownDatabaseCode() {
        assertThatThrownBy(() -> converter.convertToEntityAttribute((short) 9))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Código de status de item de importação inválido: 9");
    }
}
