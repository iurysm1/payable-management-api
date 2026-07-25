package com.totvs.payablemanagementapi.core.exception;

public class SupplierInUseException extends RuntimeException {

    public SupplierInUseException(Long id) {
        super("Fornecedor com id %d possui contas a pagar vinculadas".formatted(id));
    }
}
