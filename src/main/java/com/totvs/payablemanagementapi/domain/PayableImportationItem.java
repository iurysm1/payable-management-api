package com.totvs.payablemanagementapi.domain;

import com.totvs.payablemanagementapi.domain.enums.StatusPayableImportationItemEnum;
import com.totvs.payablemanagementapi.domain.exception.InvalidPayableImportationItemException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "payable_importation_item")
public class PayableImportationItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "payable_id")
    private Long payableId;

    @Column(name = "payable_importation_id", nullable = false, updatable = false)
    private Long payableImportationId;

    @Column(nullable = false)
    private StatusPayableImportationItemEnum status;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    public static PayableImportationItem create(Long payableImportationId) {
        if (payableImportationId == null) {
            throw new InvalidPayableImportationItemException("A importação do item é obrigatória");
        }

        return new PayableImportationItem(
                null,
                null,
                payableImportationId,
                StatusPayableImportationItemEnum.PENDING,
                null
        );
    }

    public void updateStatus(
            StatusPayableImportationItemEnum status,
            Long payableId,
            String errorMessage
    ) {
        validateStatusPayableAndErrorMessage(status, payableId, errorMessage);
        this.status = status;
        this.payableId = payableId;
        this.errorMessage = errorMessage;
    }

    private void validateStatusPayableAndErrorMessage(
            StatusPayableImportationItemEnum status,
            Long payableId,
            String errorMessage
    ) {
        if (status == null) {
            throw new InvalidPayableImportationItemException("O status do item de importação é obrigatório");
        }

        if (status == StatusPayableImportationItemEnum.COMPLETED && payableId == null) {
            throw new InvalidPayableImportationItemException(
                    "A conta a pagar é obrigatória quando o item é concluído"
            );
        }

        if (status != StatusPayableImportationItemEnum.COMPLETED && payableId != null) {
            throw new InvalidPayableImportationItemException(
                    "A conta a pagar deve ser nula quando o item não é concluído"
            );
        }

        if (status == StatusPayableImportationItemEnum.ERROR && isBlank(errorMessage)) {
            throw new InvalidPayableImportationItemException(
                    "A mensagem de erro é obrigatória quando o item possui erro"
            );
        }

        if (status != StatusPayableImportationItemEnum.ERROR && errorMessage != null) {
            throw new InvalidPayableImportationItemException(
                    "A mensagem de erro deve ser nula quando o item não possui erro"
            );
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
