package com.totvs.payablemanagementapi.domain;

import com.totvs.payablemanagementapi.domain.exception.InvalidSupplierException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SupplierTest {

    @Test
    void shouldRejectNullName() {
        assertThatThrownBy(() -> Supplier.create(null))
                .isInstanceOf(InvalidSupplierException.class)
                .hasMessage("O nome do fornecedor é obrigatório");
    }

    @Test
    void shouldRejectEmptyName() {
        assertThatThrownBy(() -> Supplier.create(""))
                .isInstanceOf(InvalidSupplierException.class)
                .hasMessage("O nome do fornecedor é obrigatório");
    }

    @Test
    void shouldRejectBlankName() {
        assertThatThrownBy(() -> Supplier.create("   "))
                .isInstanceOf(InvalidSupplierException.class)
                .hasMessage("O nome do fornecedor é obrigatório");
    }

    @Test
    void shouldUpdateSupplierDetails() {
        Supplier supplier = Supplier.create("TOTVS");

        supplier.updateDetails("Nome atualizado");

        assertThat(supplier.getName()).isEqualTo("Nome atualizado");
    }

    @Test
    void shouldRejectBlankNameWhenUpdatingSupplierDetails() {
        Supplier supplier = Supplier.create("TOTVS");

        assertThatThrownBy(() -> supplier.updateDetails("   "))
                .isInstanceOf(InvalidSupplierException.class)
                .hasMessage("O nome do fornecedor é obrigatório");
    }
}
