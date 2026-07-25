package com.totvs.payablemanagementapi.core.util;

import com.totvs.payablemanagementapi.core.exception.InvalidDatePeriodCriteriaException;

import java.time.LocalDate;

public class DatePeriodCriteria {
    private final LocalDate startDate;
    private final LocalDate endDate;

    public DatePeriodCriteria(LocalDate startDate, LocalDate endDate) {

        this.startDate = startDate;
        this.endDate = endDate;
        isValidPeriodCriteria(startDate, endDate);
    }

    public void isValidPeriodCriteria(LocalDate startDate, LocalDate endDate) {

        if(startDate == null || endDate == null || startDate.isAfter(endDate)){
            throw new InvalidDatePeriodCriteriaException();
        }
    }
}
