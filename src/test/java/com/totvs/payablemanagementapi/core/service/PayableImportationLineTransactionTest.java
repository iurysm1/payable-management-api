package com.totvs.payablemanagementapi.core.service;

import com.totvs.payablemanagementapi.adapter.output.repository.PayableImportationItemRepository;
import com.totvs.payablemanagementapi.adapter.output.repository.PayableImportationJpaAdapter;
import com.totvs.payablemanagementapi.adapter.output.repository.PayableImportationRepository;
import com.totvs.payablemanagementapi.adapter.output.repository.PayableJpaAdapter;
import com.totvs.payablemanagementapi.adapter.output.repository.PayableRepository;
import com.totvs.payablemanagementapi.adapter.output.repository.SupplierJpaAdapter;
import com.totvs.payablemanagementapi.adapter.output.repository.SupplierRepository;
import com.totvs.payablemanagementapi.core.port.input.PayableImportationLineUseCase;
import com.totvs.payablemanagementapi.core.port.input.dto.PayableDto;
import com.totvs.payablemanagementapi.domain.Payable;
import com.totvs.payablemanagementapi.domain.PayableImportation;
import com.totvs.payablemanagementapi.domain.PayableImportationItem;
import com.totvs.payablemanagementapi.domain.Supplier;
import com.totvs.payablemanagementapi.domain.enums.StatusPayableEnum;
import com.totvs.payablemanagementapi.domain.enums.StatusPayableImportationItemEnum;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({
        PayableImportationLineService.class,
        PayableService.class,
        SupplierService.class,
        PayableJpaAdapter.class,
        PayableImportationJpaAdapter.class,
        SupplierJpaAdapter.class
})
@Transactional(propagation = Propagation.NOT_SUPPORTED)
class PayableImportationLineTransactionTest {

    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:17-alpine");

    @Autowired
    private PayableImportationLineUseCase payableImportationLineUseCase;

    @Autowired
    private SupplierRepository supplierRepository;

    @Autowired
    private PayableRepository payableRepository;

    @Autowired
    private PayableImportationRepository payableImportationRepository;

    @Autowired
    private PayableImportationItemRepository payableImportationItemRepository;

    @DynamicPropertySource
    static void configureDataSource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
    }

    @BeforeEach
    void setUp() {
        payableImportationItemRepository.deleteAll();
        payableRepository.deleteAll();
        payableImportationRepository.deleteAll();
        supplierRepository.deleteAll();
    }

    @Test
    void shouldCommitPayableAndSuccessfulItemTogether() {
        Supplier supplier = supplierRepository.save(new Supplier(null, "Fornecedor"));
        PayableImportation importation = payableImportationRepository.save(PayableImportation.create());

        payableImportationLineUseCase.process(importation.getId(), payableDto(supplier.getId()));

        List<Payable> payables = payableRepository.findAll();
        List<PayableImportationItem> items =
                payableImportationItemRepository.findByPayableImportationId(importation.getId());

        assertThat(payables).hasSize(1);
        assertThat(items).hasSize(1);
        assertThat(items.getFirst().getStatus()).isEqualTo(StatusPayableImportationItemEnum.SUCCESS);
        assertThat(items.getFirst().getPayableId()).isEqualTo(payables.getFirst().getId());
    }

    @Test
    void shouldRollbackPayableWhenSuccessfulItemCannotBePersisted() {
        Supplier supplier = supplierRepository.save(new Supplier(null, "Fornecedor"));
        Long nonexistentImportationId = Long.MAX_VALUE;

        assertThatThrownBy(() -> payableImportationLineUseCase.process(
                nonexistentImportationId,
                payableDto(supplier.getId())
        )).isInstanceOf(RuntimeException.class);

        assertThat(payableRepository.findAll()).isEmpty();
        assertThat(payableImportationItemRepository.findAll()).isEmpty();
    }

    private PayableDto payableDto(Long supplierId) {
        return new PayableDto(
                null,
                "Aluguel",
                new BigDecimal("100.00"),
                StatusPayableEnum.PENDENTE,
                null,
                null,
                supplierId
        );
    }
}
