package com.totvs.payablemanagementapi.adapter.input.controller;

import com.totvs.payablemanagementapi.core.port.input.PayableImportationServiceUseCase;
import com.totvs.payablemanagementapi.domain.PayableImportation;
import com.totvs.payablemanagementapi.domain.PayableImportationItem;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Locale;

@RestController
@RequestMapping("/import/payable")
@RequiredArgsConstructor
public class PayableImportationController {

    private final PayableImportationServiceUseCase payableImportationServiceUseCase;

    @GetMapping
    public ResponseEntity<List<PayableImportation>> list() {
        return ResponseEntity.ok(payableImportationServiceUseCase.list());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PayableImportation> findById(@PathVariable Long id) {
        return ResponseEntity.ok(payableImportationServiceUseCase.findById(id));
    }

    @GetMapping("/{id}/items")
    public ResponseEntity<List<PayableImportationItem>> listItems(@PathVariable Long id) {
        return ResponseEntity.ok(payableImportationServiceUseCase.listPayableImportationItem(id));
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<PayableImportation> create(@RequestParam("file") MultipartFile file) throws IOException {
        validateFile(file);

        try (InputStream content = file.getInputStream()) {
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(payableImportationServiceUseCase.create(content));
        }
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O arquivo é obrigatório");
        }

        String fileName = file.getOriginalFilename();
        if (fileName == null || !fileName.toLowerCase(Locale.ROOT).endsWith(".csv")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O arquivo deve ser CSV");
        }
    }
}
