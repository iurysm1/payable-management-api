package com.totvs.payablemanagementapi.core.exception;

public class PayableImportEventPublishingException extends RuntimeException {

    public PayableImportEventPublishingException(String message) {
        super(message);
    }

    public PayableImportEventPublishingException(String message, Throwable cause) {
        super(message, cause);
    }
}
