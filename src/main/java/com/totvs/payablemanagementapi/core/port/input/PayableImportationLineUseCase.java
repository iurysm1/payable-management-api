package com.totvs.payablemanagementapi.core.port.input;

import com.totvs.payablemanagementapi.core.port.input.dto.PayableDto;

public interface PayableImportationLineUseCase {

    void process(Long payableImportationId, PayableDto payableDto);
}
