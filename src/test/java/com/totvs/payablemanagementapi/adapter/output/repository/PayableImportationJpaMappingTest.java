package com.totvs.payablemanagementapi.adapter.output.repository;

import com.totvs.payablemanagementapi.domain.Payable;
import com.totvs.payablemanagementapi.domain.PayableImportation;
import com.totvs.payablemanagementapi.domain.PayableImportationItem;
import com.totvs.payablemanagementapi.domain.Supplier;
import com.totvs.payablemanagementapi.domain.enums.StatusPayableEnum;
import com.totvs.payablemanagementapi.domain.enums.StatusPayableImportationEnum;
import com.totvs.payablemanagementapi.domain.enums.StatusPayableImportationItemEnum;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class PayableImportationJpaMappingTest {

    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:17-alpine");

    @Autowired
    private EntityManager entityManager;

    @DynamicPropertySource
    static void configureDataSource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
    }

    @Test
    void shouldPersistImportationItemsUsingSnakeCaseColumnsAndForeignKeys() {
        Supplier supplier = new Supplier(null, "Fornecedor de teste");
        entityManager.persist(supplier);

        Payable payable = Payable.create(
                "Aluguel",
                new BigDecimal("100.00"),
                StatusPayableEnum.PENDENTE,
                null,
                null,
                supplier
        );
        entityManager.persist(payable);

        PayableImportation importation = PayableImportation.create();
        importation.updateStatus(StatusPayableImportationEnum.COMPLETED_WITH_ERRORS, null);
        entityManager.persist(importation);
        entityManager.flush();

        PayableImportationItem completedItem = PayableImportationItem.create(importation.getId());
        completedItem.updateStatus(StatusPayableImportationItemEnum.COMPLETED, payable.getId(), null);
        entityManager.persist(completedItem);

        PayableImportationItem errorItem = PayableImportationItem.create(importation.getId());
        errorItem.updateStatus(StatusPayableImportationItemEnum.ERROR, null, "Linha inválida");
        entityManager.persist(errorItem);
        entityManager.flush();
        entityManager.clear();

        PayableImportation persistedImportation = entityManager.find(
                PayableImportation.class,
                importation.getId()
        );
        PayableImportationItem persistedCompletedItem = entityManager.find(
                PayableImportationItem.class,
                completedItem.getId()
        );
        PayableImportationItem persistedErrorItem = entityManager.find(
                PayableImportationItem.class,
                errorItem.getId()
        );

        assertThat(persistedImportation.getStatus())
                .isEqualTo(StatusPayableImportationEnum.COMPLETED_WITH_ERRORS);
        assertThat(persistedImportation.getErrorMessage()).isNull();
        assertThat(persistedCompletedItem.getPayableId()).isEqualTo(payable.getId());
        assertThat(persistedCompletedItem.getStatus()).isEqualTo(StatusPayableImportationItemEnum.COMPLETED);
        assertThat(persistedErrorItem.getPayableId()).isNull();
        assertThat(persistedErrorItem.getStatus()).isEqualTo(StatusPayableImportationItemEnum.ERROR);
        assertThat(persistedErrorItem.getErrorMessage()).isEqualTo("Linha inválida");
    }
}
