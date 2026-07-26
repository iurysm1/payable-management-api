package com.totvs.payablemanagementapi.core.exception;

public class TotalPaidReportNotFoundException extends RuntimeException {

    public TotalPaidReportNotFoundException() {
        super("Nenhuma conta paga encontrada para o período informado");
    }
}
