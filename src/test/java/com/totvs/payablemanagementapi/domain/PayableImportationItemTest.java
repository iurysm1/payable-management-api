package com.totvs.payablemanagementapi.domain;

import com.totvs.payablemanagementapi.domain.enums.StatusPayableImportationItemEnum;
import com.totvs.payablemanagementapi.domain.exception.InvalidPayableImportationItemException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PayableImportationItemTest {

    @Test
    void shouldCreatePendingItemWithoutPayableOrError() {
        PayableImportationItem item = PayableImportationItem.create(10L);

        assertThat(item.getPayableImportationId()).isEqualTo(10L);
        assertThat(item.getStatus()).isEqualTo(StatusPayableImportationItemEnum.PENDING);
        assertThat(item.getPayableId()).isNull();
        assertThat(item.getErrorMessage()).isNull();
    }

    @Test
    void shouldCompleteItemWithPayable() {
        PayableImportationItem item = PayableImportationItem.create(10L);

        item.updateStatus(StatusPayableImportationItemEnum.COMPLETED, 20L, null);

        assertThat(item.getStatus()).isEqualTo(StatusPayableImportationItemEnum.COMPLETED);
        assertThat(item.getPayableId()).isEqualTo(20L);
        assertThat(item.getErrorMessage()).isNull();
    }

    @Test
    void shouldStoreErrorWithoutPayable() {
        PayableImportationItem item = PayableImportationItem.create(10L);

        item.updateStatus(StatusPayableImportationItemEnum.ERROR, null, "Valor inválido");

        assertThat(item.getStatus()).isEqualTo(StatusPayableImportationItemEnum.ERROR);
        assertThat(item.getPayableId()).isNull();
        assertThat(item.getErrorMessage()).isEqualTo("Valor inválido");
    }

    @Test
    void shouldRejectCompletedItemWithoutPayable() {
        PayableImportationItem item = PayableImportationItem.create(10L);

        assertThatThrownBy(() -> item.updateStatus(
                StatusPayableImportationItemEnum.COMPLETED,
                null,
                null
        )).isInstanceOf(InvalidPayableImportationItemException.class)
                .hasMessage("A conta a pagar é obrigatória quando o item é concluído");
    }

    @Test
    void shouldRejectPayableForNonCompletedItem() {
        PayableImportationItem item = PayableImportationItem.create(10L);

        assertThatThrownBy(() -> item.updateStatus(
                StatusPayableImportationItemEnum.ERROR,
                20L,
                "Valor inválido"
        )).isInstanceOf(InvalidPayableImportationItemException.class)
                .hasMessage("A conta a pagar deve ser nula quando o item não é concluído");
    }

    @Test
    void shouldRejectErrorWithoutMessage() {
        PayableImportationItem item = PayableImportationItem.create(10L);

        assertThatThrownBy(() -> item.updateStatus(
                StatusPayableImportationItemEnum.ERROR,
                null,
                " "
        )).isInstanceOf(InvalidPayableImportationItemException.class)
                .hasMessage("A mensagem de erro é obrigatória quando o item possui erro");
    }
}
