package com.totvs.payablemanagementapi.core.util;

import com.totvs.payablemanagementapi.core.exception.InvalidDatePeriodCriteriaException;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DatePeriodCriteriaTest {

    private static final String INVALID_PERIOD_MESSAGE =
            "O período de datas deve possuir data inicial e final, e a data inicial não pode ser posterior à data final";

    @Test
    void shouldAcceptValidPeriod() {
        assertThatCode(() -> new DatePeriodCriteria(
                LocalDate.of(2026, 8, 1),
                LocalDate.of(2026, 8, 10)
        )).doesNotThrowAnyException();
    }

    @Test
    void shouldAcceptPeriodWithEqualDates() {
        LocalDate date = LocalDate.of(2026, 8, 10);

        assertThatCode(() -> new DatePeriodCriteria(date, date)).doesNotThrowAnyException();
    }

    @Test
    void shouldAcceptPeriodWithoutDates() {
        assertThatCode(() -> new DatePeriodCriteria(null, null)).doesNotThrowAnyException();
    }

    @Test
    void shouldRejectPeriodWithStartDateAfterEndDate() {
        assertInvalidPeriod(LocalDate.of(2026, 8, 11), LocalDate.of(2026, 8, 10));
    }

    @Test
    void shouldRejectPeriodWithoutStartDate() {
        assertInvalidPeriod(null, LocalDate.of(2026, 8, 10));
    }

    @Test
    void shouldRejectPeriodWithoutEndDate() {
        assertInvalidPeriod(LocalDate.of(2026, 8, 10), null);
    }

    private void assertInvalidPeriod(LocalDate startDate, LocalDate endDate) {
        assertThatThrownBy(() -> new DatePeriodCriteria(startDate, endDate))
                .isInstanceOf(InvalidDatePeriodCriteriaException.class)
                .hasMessage(INVALID_PERIOD_MESSAGE);
    }
}
