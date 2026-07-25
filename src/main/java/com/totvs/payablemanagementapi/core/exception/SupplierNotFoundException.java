package com.totvs.payablemanagementapi.core.exception;

public class SupplierNotFoundException extends RuntimeException {

    public SupplierNotFoundException(Long id) {
        super("Fornecedor com id %d não encontrado".formatted(id));
    }
}
