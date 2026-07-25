package com.totvs.payablemanagementapi.core.port.input;

import com.totvs.payablemanagementapi.core.port.input.dto.PayableDto;
import com.totvs.payablemanagementapi.domain.Payable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PayableUseCase {
    Page<Payable> list(Pageable pageable);

    Payable findById(Long id);

    Payable save(PayableDto payableDto);

    Payable update(PayableDto payableDto);

    void delete(Long payable);
}
