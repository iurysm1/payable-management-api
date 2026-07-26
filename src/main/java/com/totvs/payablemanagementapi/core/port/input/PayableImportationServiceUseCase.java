package com.totvs.payablemanagementapi.core.port.input;

import com.totvs.payablemanagementapi.core.port.input.dto.PayableImportationDto;
import com.totvs.payablemanagementapi.domain.PayableImportation;
import com.totvs.payablemanagementapi.domain.PayableImportationItem;

import java.util.List;

public interface PayableImportationServiceUseCase {
    PayableImportation create();

    PayableImportation findById(Long id);

    List<PayableImportation> list();

    List<PayableImportationItem> listPayableImportationItem(long id);
}
