package com.totvs.payablemanagementapi.adapter.input.controller;

import com.totvs.payablemanagementapi.core.port.input.SupplierUseCase;
import com.totvs.payablemanagementapi.domain.Supplier;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/suppliers")
@RequiredArgsConstructor
public class SuppliersController {

    private final SupplierUseCase supplierUseCase;

    @GetMapping
    public ResponseEntity<Page<Supplier>> list(Pageable pageable) {
        return ResponseEntity.ok(supplierUseCase.list(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Supplier> findById(@PathVariable Long id) {
        return ResponseEntity.ok(supplierUseCase.findById(id));
    }

    @PostMapping
    public ResponseEntity<Supplier> save(@Valid @RequestBody Supplier supplier) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(supplierUseCase.save(supplier));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Supplier> update(@PathVariable Long id, @Valid @RequestBody Supplier supplier) {
        supplier.setId(id);
        return ResponseEntity.ok(supplierUseCase.update(supplier));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        supplierUseCase.delete(id);
        return ResponseEntity.noContent().build();
    }
}
