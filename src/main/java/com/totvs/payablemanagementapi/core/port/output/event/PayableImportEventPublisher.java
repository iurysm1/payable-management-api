package com.totvs.payablemanagementapi.core.port.output.event;

public interface PayableImportEventPublisher {
    void publish(PayableImportEvent event);
}
