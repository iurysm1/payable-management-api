package com.totvs.payablemanagementapi.core.port.input;

import com.totvs.payablemanagementapi.domain.Payable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PayableUseCase {
    Page<Payable> list(Pageable pageable);

    Payable findById(Long id);

    Payable save(Payable payable);

    Payable update(Payable payable);

    void delete(Long payable);
}
