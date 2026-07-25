package com.totvs.payablemanagementapi.core.exception;

public class InvalidDatePeriodCriteriaException extends RuntimeException {

    public InvalidDatePeriodCriteriaException() {
        super("O período de datas deve possuir data inicial e final, e a data inicial não pode ser posterior à data final");
    }
}
