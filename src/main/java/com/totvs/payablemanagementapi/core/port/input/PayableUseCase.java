package com.totvs.payablemanagementapi.core.port.input;

import com.totvs.payablemanagementapi.core.port.input.dto.PayableDto;
import com.totvs.payablemanagementapi.core.port.input.dto.PayableFilterDto;
import com.totvs.payablemanagementapi.domain.Payable;
import com.totvs.payablemanagementapi.domain.enums.StatusPayableEnum;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PayableUseCase {
    Page<Payable> list(Pageable pageable, PayableFilterDto filter);

    Payable findById(Long id);

    Payable save(PayableDto payableDto);

    Payable update(PayableDto payableDto);

    void delete(Long payable);

    Payable updateStatus(Long id, StatusPayableEnum status);

}
