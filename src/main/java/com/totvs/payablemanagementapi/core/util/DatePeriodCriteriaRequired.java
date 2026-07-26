package com.totvs.payablemanagementapi.core.util;

import com.totvs.payablemanagementapi.core.exception.InvalidDatePeriodCriteriaException;

import java.time.LocalDate;

public record DatePeriodCriteriaRequired(LocalDate startDate, LocalDate endDate) {

    public DatePeriodCriteriaRequired {
        if (startDate == null || endDate == null || startDate.isAfter(endDate)) {
            throw new InvalidDatePeriodCriteriaException();
        }
    }
}
