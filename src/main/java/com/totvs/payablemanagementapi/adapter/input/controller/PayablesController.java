package com.totvs.payablemanagementapi.adapter.input.controller;

import com.totvs.payablemanagementapi.core.port.input.PayableUseCase;
import com.totvs.payablemanagementapi.domain.Payable;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("payables")
@RequiredArgsConstructor
public class PayablesController {

    private final PayableUseCase payableUseCase;

    @GetMapping
    public ResponseEntity<Page<Payable>> list(Pageable pageable){
        return ResponseEntity.ok(payableUseCase.list(pageable));
    }
}
