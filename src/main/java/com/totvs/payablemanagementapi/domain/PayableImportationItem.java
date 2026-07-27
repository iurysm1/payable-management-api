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
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "payable_importation_item")
@Builder
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

    public static PayableImportationItem createSuccess(
            Long payableImportationId,
            Long payableId
    ) {
        PayableImportationItem payableImportationItem = PayableImportationItem.builder()
                .payableImportationId(payableImportationId)
                .payableId(payableId)
                .status(StatusPayableImportationItemEnum.SUCCESS)
                .build();

        payableImportationItem.validate();

        return payableImportationItem;
    }

    public static PayableImportationItem createError(
            Long payableImportationId,
            String errorMessage
    ) {
        PayableImportationItem payableImportationItem = PayableImportationItem.builder()
                .payableImportationId(payableImportationId)
                .status(StatusPayableImportationItemEnum.ERROR)
                .errorMessage(errorMessage)
                .build();

        payableImportationItem.validate();

        return payableImportationItem;
    }

    public void validate() {
        if (payableImportationId == null) {
            throw new InvalidPayableImportationItemException("A importação do item é obrigatória");
        }

        if (status == null) {
            throw new InvalidPayableImportationItemException("O status do item de importação é obrigatório");
        }

        if (status == StatusPayableImportationItemEnum.SUCCESS && payableId == null) {
            throw new InvalidPayableImportationItemException(
                    "A conta a pagar é obrigatória quando o item possui sucesso"
            );
        }

        if (status != StatusPayableImportationItemEnum.SUCCESS && payableId != null) {
            throw new InvalidPayableImportationItemException(
                    "A conta a pagar deve ser nula quando o item não possui sucesso"
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
