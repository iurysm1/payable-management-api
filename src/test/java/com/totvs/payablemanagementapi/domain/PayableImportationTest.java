package com.totvs.payablemanagementapi.domain;

import com.totvs.payablemanagementapi.domain.enums.StatusPayableImportationEnum;
import com.totvs.payablemanagementapi.domain.exception.InvalidPayableImportationException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PayableImportationTest {

    @Test
    void shouldCreatePendingImportationWithAuditDates() {
        PayableImportation importation = PayableImportation.create();

        assertThat(importation.getStatus()).isEqualTo(StatusPayableImportationEnum.PENDING);
        assertThat(importation.getCreatedAt()).isNotNull();
        assertThat(importation.getUpdatedAt()).isEqualTo(importation.getCreatedAt());
        assertThat(importation.getErrorMessage()).isNull();
    }

    @Test
    void shouldAcceptErrorMessageWhenImportationFails() {
        PayableImportation importation = PayableImportation.create();

        importation.updateStatus(StatusPayableImportationEnum.FAILED, "Arquivo inválido");

        assertThat(importation.getStatus()).isEqualTo(StatusPayableImportationEnum.FAILED);
        assertThat(importation.getErrorMessage()).isEqualTo("Arquivo inválido");
        assertThat(importation.getUpdatedAt()).isAfterOrEqualTo(importation.getCreatedAt());
    }

    @Test
    void shouldRejectFailedImportationWithoutErrorMessage() {
        PayableImportation importation = PayableImportation.create();

        assertThatThrownBy(() -> importation.updateStatus(StatusPayableImportationEnum.FAILED, "  "))
                .isInstanceOf(InvalidPayableImportationException.class)
                .hasMessage("A mensagem de erro é obrigatória quando a importação falha");
    }

    @Test
    void shouldRejectErrorMessageWhenImportationDoesNotFail() {
        PayableImportation importation = PayableImportation.create();

        assertThatThrownBy(() -> importation.updateStatus(
                StatusPayableImportationEnum.COMPLETED_WITH_ERRORS,
                "Um item falhou"
        )).isInstanceOf(InvalidPayableImportationException.class)
                .hasMessage("A mensagem de erro deve ser nula quando a importação não falha");
    }
}
