package com.totvs.payablemanagementapi.core.service;

import com.totvs.payablemanagementapi.core.port.input.PayableImportationLineUseCase;
import com.totvs.payablemanagementapi.core.port.input.PayableUseCase;
import com.totvs.payablemanagementapi.core.port.input.dto.PayableDto;
import com.totvs.payablemanagementapi.core.port.output.PayableImportPersistencePort;
import com.totvs.payablemanagementapi.domain.Payable;
import com.totvs.payablemanagementapi.domain.PayableImportationItem;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PayableImportationLineService implements PayableImportationLineUseCase {

    private final PayableUseCase payableUseCase;
    private final PayableImportPersistencePort payableImportPersistencePort;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void process(Long payableImportationId, PayableDto payableDto) {
        Payable payable = payableUseCase.save(payableDto);
        payableImportPersistencePort.saveItem(
                PayableImportationItem.createSuccess(payableImportationId, payable.getId())
        );
    }
}
