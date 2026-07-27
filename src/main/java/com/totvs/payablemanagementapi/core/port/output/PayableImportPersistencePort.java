package com.totvs.payablemanagementapi.core.port.output;

import com.totvs.payablemanagementapi.domain.PayableImportation;
import com.totvs.payablemanagementapi.domain.PayableImportationItem;

import java.util.List;

public interface PayableImportPersistencePort {
    PayableImportation save(PayableImportation payableImportation);
    PayableImportation findById(long id);
    void deleteById(long id);

    List<PayableImportation> findAll();
    List<PayableImportationItem> findByPayableImportationId(long id);
}
