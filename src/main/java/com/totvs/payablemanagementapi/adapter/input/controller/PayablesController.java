package com.totvs.payablemanagementapi.adapter.input.controller;

import com.totvs.payablemanagementapi.core.port.input.PayableUseCase;
import com.totvs.payablemanagementapi.core.port.input.dto.PayableDto;
import com.totvs.payablemanagementapi.core.port.input.dto.PayableFilterDto;
import com.totvs.payablemanagementapi.core.port.input.dto.UpdatePayableStatusDto;
import com.totvs.payablemanagementapi.core.util.DatePeriodCriteria;
import com.totvs.payablemanagementapi.domain.Payable;
import com.totvs.payablemanagementapi.domain.enums.StatusPayableEnum;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/payable")
@RequiredArgsConstructor
public class PayablesController {

    private final PayableUseCase payableUseCase;

    @GetMapping
    public ResponseEntity<Page<Payable>> list(
            @RequestParam(required = false) String description,
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate,
            @RequestParam(required = false) String status,
            Pageable pageable
    ) {
        DatePeriodCriteria periodCriteria = new DatePeriodCriteria(startDate, endDate);
        StatusPayableEnum statusFilter = status == null ? null : StatusPayableEnum.fromName(status);
        PayableFilterDto filter = new PayableFilterDto(description, periodCriteria, statusFilter);

        return ResponseEntity.ok(payableUseCase.list(pageable, filter));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Payable> findById(@PathVariable Long id) {
        return ResponseEntity.ok(payableUseCase.findById(id));
    }

    @PostMapping
    public ResponseEntity<Payable> save(@Valid @RequestBody PayableDto payableDto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(payableUseCase.save(payableDto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Payable> update(@PathVariable Long id, @Valid @RequestBody PayableDto payableDto) {
        PayableDto payableToUpdate = new PayableDto(
                id,
                payableDto.description(),
                payableDto.amount(),
                payableDto.status(),
                payableDto.expirationDate(),
                payableDto.paymentDate(),
                payableDto.supplierId()
        );

        return ResponseEntity.ok(payableUseCase.update(payableToUpdate));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        payableUseCase.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<Payable> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdatePayableStatusDto updatePayableStatusDto
    ) {
        return ResponseEntity.ok(payableUseCase.updateStatus(id, updatePayableStatusDto));
    }
}
