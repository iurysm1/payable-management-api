package com.totvs.payablemanagementapi.core.exception;

public class PayableImportationNotFoundException extends RuntimeException {

    public PayableImportationNotFoundException(Long id) {
        super("Importação de contas a pagar com id %d não encontrada".formatted(id));
    }
}
