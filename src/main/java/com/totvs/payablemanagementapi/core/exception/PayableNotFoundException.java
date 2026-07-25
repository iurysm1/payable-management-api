package com.totvs.payablemanagementapi.core.exception;

public class PayableNotFoundException extends RuntimeException {
    public PayableNotFoundException(Long id) {
        super("Conta a pagar com id %d não encontrada".formatted(id));
    }
}