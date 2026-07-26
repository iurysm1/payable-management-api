package com.totvs.payablemanagementapi.core.port.input;

import com.totvs.payablemanagementapi.domain.PayableImportation;
import com.totvs.payablemanagementapi.domain.PayableImportationItem;
import com.totvs.payablemanagementapi.core.port.input.dto.UpdatePayableImportationStatusDto;

import java.io.InputStream;
import java.util.List;

public interface PayableImportationServiceUseCase {
    PayableImportation create(InputStream file);

    PayableImportation findById(Long id);

    List<PayableImportation> list();

    PayableImportation updateStatus(Long id, UpdatePayableImportationStatusDto updateStatusDto);

    List<PayableImportationItem> listPayableImportationItem(Long id);
}
