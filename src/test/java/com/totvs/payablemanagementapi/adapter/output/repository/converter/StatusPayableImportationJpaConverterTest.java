package com.totvs.payablemanagementapi.adapter.output.repository.converter;

import com.totvs.payablemanagementapi.domain.enums.StatusPayableImportationEnum;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class StatusPayableImportationJpaConverterTest {

    private final StatusPayableImportationJpaConverter converter = new StatusPayableImportationJpaConverter();

    @Test
    void shouldConvertStatusToDatabaseCode() {
        assertThat(converter.convertToDatabaseColumn(StatusPayableImportationEnum.PENDING)).isEqualTo((short) 0);
        assertThat(converter.convertToDatabaseColumn(StatusPayableImportationEnum.PROCESSING)).isEqualTo((short) 1);
        assertThat(converter.convertToDatabaseColumn(StatusPayableImportationEnum.COMPLETED)).isEqualTo((short) 2);
        assertThat(converter.convertToDatabaseColumn(StatusPayableImportationEnum.COMPLETED_WITH_ERRORS)).isEqualTo((short) 3);
        assertThat(converter.convertToDatabaseColumn(StatusPayableImportationEnum.FAILED)).isEqualTo((short) 4);
        assertThat(converter.convertToDatabaseColumn(null)).isNull();
    }

    @Test
    void shouldConvertDatabaseCodeToStatus() {
        assertThat(converter.convertToEntityAttribute((short) 0)).isEqualTo(StatusPayableImportationEnum.PENDING);
        assertThat(converter.convertToEntityAttribute((short) 4)).isEqualTo(StatusPayableImportationEnum.FAILED);
        assertThat(converter.convertToEntityAttribute(null)).isNull();
    }

    @Test
    void shouldRejectUnknownDatabaseCode() {
        assertThatThrownBy(() -> converter.convertToEntityAttribute((short) 9))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Código de status de importação inválido: 9");
    }
}
