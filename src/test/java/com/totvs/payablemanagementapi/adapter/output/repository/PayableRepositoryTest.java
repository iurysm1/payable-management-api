package com.totvs.payablemanagementapi.adapter.output.repository;

import com.totvs.payablemanagementapi.domain.Payable;
import com.totvs.payablemanagementapi.domain.Supplier;
import com.totvs.payablemanagementapi.domain.enums.StatusPayableEnum;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
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
class PayableRepositoryTest {

    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:17-alpine");

    private static final PageRequest PAGEABLE = PageRequest.of(0, 20);

    @Autowired
    private PayableRepository payableRepository;

    @Autowired
    private SupplierRepository supplierRepository;

    @DynamicPropertySource
    static void configureDataSource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
    }

    @BeforeEach
    void setUp() {
        Supplier supplier = supplierRepository.save(new Supplier(null, "Fornecedor de teste"));

        payableRepository.saveAll(List.of(
                payable("Aluguel escritório", LocalDate.of(2026, 8, 10), supplier),
                payable("ALUGUEL depósito", LocalDate.of(2026, 8, 20), supplier),
                payable("Internet", LocalDate.of(2026, 8, 15), supplier),
                payable("Conta sem vencimento", null, supplier)
        ));
    }

    @Test
    void shouldReturnAllPayablesWhenDescriptionIsNull() {
        var result = payableRepository.findAllByFilters(null, null, null, PAGEABLE);

        assertThat(result.getContent()).hasSize(4);
    }

    @Test
    void shouldFilterByPartialDescriptionIgnoringCase() {
        var result = payableRepository.findAllByFilters("aluguEL", null, null, PAGEABLE);

        assertThat(result.getContent())
                .extracting(Payable::getDescription)
                .containsExactlyInAnyOrder("Aluguel escritório", "ALUGUEL depósito");
    }

    @Test
    void shouldFilterByExpirationDatePeriodIncludingBoundaries() {
        var result = payableRepository.findAllByFilters(
                null,
                LocalDate.of(2026, 8, 10),
                LocalDate.of(2026, 8, 20),
                PAGEABLE
        );

        assertThat(result.getContent())
                .extracting(Payable::getDescription)
                .containsExactlyInAnyOrder("Aluguel escritório", "ALUGUEL depósito", "Internet");
    }

    @Test
    void shouldReturnPayablesWithAndWithoutExpirationDateWhenPeriodIsAbsent() {
        var result = payableRepository.findAllByFilters(null, null, null, PAGEABLE);

        assertThat(result.getContent())
                .extracting(Payable::getDescription)
                .contains("Conta sem vencimento");
    }

    @Test
    void shouldCombineDescriptionAndExpirationDatePeriod() {
        var result = payableRepository.findAllByFilters(
                "alugu",
                LocalDate.of(2026, 8, 1),
                LocalDate.of(2026, 8, 15),
                PAGEABLE
        );

        assertThat(result.getContent())
                .extracting(Payable::getDescription)
                .containsExactly("Aluguel escritório");
    }

    private Payable payable(String description, LocalDate expirationDate, Supplier supplier) {
        return Payable.builder()
                .description(description)
                .amount(new BigDecimal("100.00"))
                .status(StatusPayableEnum.PENDENTE)
                .expirationDate(expirationDate)
                .supplier(supplier)
                .build();
    }
}
