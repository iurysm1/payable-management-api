package com.totvs.payablemanagementapi.adapter.input.controller;

import com.totvs.payablemanagementapi.core.exception.InvalidDatePeriodCriteriaException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;

import static org.assertj.core.api.Assertions.assertThat;

class ApiExceptionHandlerTest {

    @Test
    void shouldReturnBadRequestForInvalidDatePeriodCriteria() {
        ProblemDetail response = new ApiExceptionHandler()
                .handleInvalidDatePeriodCriteria(new InvalidDatePeriodCriteriaException());

        assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(response.getDetail()).isEqualTo(
                "O período de datas deve possuir data inicial e final, e a data inicial não pode ser posterior à data final"
        );
    }
}
