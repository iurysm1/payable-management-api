package com.totvs.payablemanagementapi.core.port.input;

import com.totvs.payablemanagementapi.core.port.output.event.PayableImportEvent;

public interface PayableImportationProcessingUseCase {

    void process(PayableImportEvent event);
}
