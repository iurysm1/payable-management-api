package com.totvs.payablemanagementapi.domain;

import com.totvs.payablemanagementapi.domain.enums.StatusPayableImportationItemEnum;
import com.totvs.payablemanagementapi.domain.exception.InvalidPayableImportationItemException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PayableImportationItemTest {

    @Test
    void shouldCreateSuccessfulItemWithPayable() {
        PayableImportationItem item = PayableImportationItem.createSuccess(10L, 20L);

        assertThat(item.getPayableImportationId()).isEqualTo(10L);
        assertThat(item.getStatus()).isEqualTo(StatusPayableImportationItemEnum.SUCCESS);
        assertThat(item.getPayableId()).isEqualTo(20L);
        assertThat(item.getErrorMessage()).isNull();
    }

    @Test
    void shouldStoreErrorWithoutPayable() {
        PayableImportationItem item = PayableImportationItem.createError(10L, "Valor inválido");

        assertThat(item.getStatus()).isEqualTo(StatusPayableImportationItemEnum.ERROR);
        assertThat(item.getPayableId()).isNull();
        assertThat(item.getErrorMessage()).isEqualTo("Valor inválido");
    }

    @Test
    void shouldRejectSuccessfulItemWithoutPayable() {
        assertThatThrownBy(() -> PayableImportationItem.createSuccess(10L, null))
                .isInstanceOf(InvalidPayableImportationItemException.class)
                .hasMessage("A conta a pagar é obrigatória quando o item possui sucesso");
    }

    @Test
    void shouldRejectPayableForErrorItem() {
        PayableImportationItem item = PayableImportationItem.builder()
                .payableImportationId(10L)
                .payableId(20L)
                .status(StatusPayableImportationItemEnum.ERROR)
                .errorMessage("Valor inválido")
                .build();

        assertThatThrownBy(item::validate)
                .isInstanceOf(InvalidPayableImportationItemException.class)
                .hasMessage("A conta a pagar deve ser nula quando o item não possui sucesso");
    }

    @Test
    void shouldRejectErrorWithoutMessage() {
        assertThatThrownBy(() -> PayableImportationItem.createError(10L, " "))
                .isInstanceOf(InvalidPayableImportationItemException.class)
                .hasMessage("A mensagem de erro é obrigatória quando o item possui erro");
    }
}
