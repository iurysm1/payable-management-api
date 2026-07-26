package com.totvs.payablemanagementapi.adapter.output.repository.reports;

import com.totvs.payablemanagementapi.adapter.output.repository.SupplierRepository;
import com.totvs.payablemanagementapi.core.port.input.dto.reports.PaidPayableItemDto;
import com.totvs.payablemanagementapi.domain.Payable;
import com.totvs.payablemanagementapi.domain.Supplier;
import com.totvs.payablemanagementapi.domain.enums.StatusPayableEnum;
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
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class TotalPaidRepositoryTest {

    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:17-alpine");

    @Autowired
    private TotalPaidRepository totalPaidRepository;

    @Autowired
    private SupplierRepository supplierRepository;

    @DynamicPropertySource
    static void configureDataSource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
    }

    @Test
    void shouldFilterPaidPayablesByPaymentPeriodAndOptionalSupplier() {
        Supplier firstSupplier = supplierRepository.save(new Supplier(null, "Fornecedor 1"));
        Supplier secondSupplier = supplierRepository.save(new Supplier(null, "Fornecedor 2"));
        LocalDate startDate = LocalDate.of(2026, 8, 1);
        LocalDate endDate = LocalDate.of(2026, 8, 31);

        totalPaidRepository.saveAll(List.of(
                payable("Pago primeiro fornecedor", StatusPayableEnum.PAGO, LocalDate.of(2026, 8, 1), firstSupplier),
                payable("Pago segundo fornecedor", StatusPayableEnum.PAGO, LocalDate.of(2026, 8, 31), secondSupplier),
                payable("Pendente", StatusPayableEnum.PENDENTE, null, firstSupplier),
                payable("Pago fora do período", StatusPayableEnum.PAGO, LocalDate.of(2026, 9, 1), firstSupplier)
        ));

        List<PaidPayableItemDto> allSuppliers = totalPaidRepository.findPaidByPaymentDatePeriod(
                startDate, endDate, StatusPayableEnum.PAGO, null
        );
        List<PaidPayableItemDto> firstSupplierOnly = totalPaidRepository.findPaidByPaymentDatePeriod(
                startDate, endDate, StatusPayableEnum.PAGO, firstSupplier.getId()
        );

        assertThat(allSuppliers).extracting(PaidPayableItemDto::description)
                .containsExactlyInAnyOrder("Pago primeiro fornecedor", "Pago segundo fornecedor");
        assertThat(firstSupplierOnly).extracting(PaidPayableItemDto::description)
                .containsExactly("Pago primeiro fornecedor");
    }

    private Payable payable(
            String description,
            StatusPayableEnum status,
            LocalDate paymentDate,
            Supplier supplier
    ) {
        return Payable.builder()
                .description(description)
                .amount(new BigDecimal("100.00"))
                .status(status)
                .paymentDate(paymentDate)
                .supplier(supplier)
                .build();
    }
}
