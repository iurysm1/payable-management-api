package com.totvs.payablemanagementapi.adapter.input.controller;

import com.totvs.payablemanagementapi.core.exception.InvalidDatePeriodCriteriaException;
import com.totvs.payablemanagementapi.core.exception.PayableImportationNotFoundException;
import com.totvs.payablemanagementapi.domain.exception.InvalidPayableImportationException;
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

    @Test
    void shouldReturnNotFoundForMissingPayableImportation() {
        ProblemDetail response = new ApiExceptionHandler()
                .handleNotFound(new PayableImportationNotFoundException(1L));

        assertThat(response.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
        assertThat(response.getDetail()).isEqualTo("Importação de contas a pagar com id 1 não encontrada");
    }

    @Test
    void shouldReturnBadRequestForInvalidPayableImportation() {
        ProblemDetail response = new ApiExceptionHandler()
                .handleInvalidDomainData(new InvalidPayableImportationException("O id da importação é obrigatório"));

        assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(response.getDetail()).isEqualTo("O id da importação é obrigatório");
    }
}
