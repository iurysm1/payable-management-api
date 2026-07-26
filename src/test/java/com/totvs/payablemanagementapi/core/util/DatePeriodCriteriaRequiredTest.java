package com.totvs.payablemanagementapi.core.util;

import com.totvs.payablemanagementapi.core.exception.InvalidDatePeriodCriteriaException;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DatePeriodCriteriaRequiredTest {

    @Test
    void shouldAcceptValidPeriod() {
        assertThatCode(() -> new DatePeriodCriteriaRequired(
                LocalDate.of(2026, 8, 1), LocalDate.of(2026, 8, 31)
        )).doesNotThrowAnyException();
    }

    @Test
    void shouldRejectMissingOrInvertedDates() {
        assertThatThrownBy(() -> new DatePeriodCriteriaRequired(null, LocalDate.of(2026, 8, 31)))
                .isInstanceOf(InvalidDatePeriodCriteriaException.class);
        assertThatThrownBy(() -> new DatePeriodCriteriaRequired(LocalDate.of(2026, 8, 31), null))
                .isInstanceOf(InvalidDatePeriodCriteriaException.class);
        assertThatThrownBy(() -> new DatePeriodCriteriaRequired(
                LocalDate.of(2026, 9, 1), LocalDate.of(2026, 8, 31)
        )).isInstanceOf(InvalidDatePeriodCriteriaException.class);
    }
}
