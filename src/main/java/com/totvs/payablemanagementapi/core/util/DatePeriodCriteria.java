package com.totvs.payablemanagementapi.core.util;

import com.totvs.payablemanagementapi.core.exception.InvalidDatePeriodCriteriaException;

import java.time.LocalDate;

public record DatePeriodCriteria(LocalDate startDate, LocalDate endDate) {

    public DatePeriodCriteria {
        boolean onlyOneDateWasInformed = (startDate == null) != (endDate == null);

        if (onlyOneDateWasInformed
                || (startDate != null && startDate.isAfter(endDate))) {
            throw new InvalidDatePeriodCriteriaException();
        }
    }
}
