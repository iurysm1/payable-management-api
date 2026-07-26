package com.totvs.payablemanagementapi.domain;

import com.totvs.payablemanagementapi.domain.enums.StatusPayableImportationEnum;
import com.totvs.payablemanagementapi.domain.exception.InvalidPayableImportationException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "payable_importation")
@Builder
public class PayableImportation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(nullable = false)
    private StatusPayableImportationEnum status;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    public static PayableImportation create() {
        LocalDateTime now = LocalDateTime.now();

        PayableImportation payableImportation = PayableImportation.builder()
                .createdAt(now)
                .updatedAt(now)
                .status(StatusPayableImportationEnum.PENDING)
                .build();

        payableImportation.validate();

        return payableImportation;
    }

    public void updateStatus(StatusPayableImportationEnum status, String errorMessage) {
        this.status = status;
        this.errorMessage = errorMessage;
        this.updatedAt = LocalDateTime.now();

        validate();
    }

    public void validate() {
        if (status == null) {
            throw new InvalidPayableImportationException("O status da importação é obrigatório");
        }

        if (status == StatusPayableImportationEnum.FAILED && isBlank(errorMessage)) {
            throw new InvalidPayableImportationException(
                    "A mensagem de erro é obrigatória quando a importação falha"
            );
        }

        if (status != StatusPayableImportationEnum.FAILED && errorMessage != null) {
            throw new InvalidPayableImportationException(
                    "A mensagem de erro deve ser nula quando a importação não falha"
            );
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
