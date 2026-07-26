package com.totvs.payablemanagementapi.domain;


import com.totvs.payablemanagementapi.domain.enums.StatusPayableEnum;
import com.totvs.payablemanagementapi.domain.exception.InvalidPayableException;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Builder
public class Payable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String description;

    @NotNull
    private BigDecimal amount;

    @Column(nullable = false)
    @Builder.Default
    private StatusPayableEnum status = StatusPayableEnum.PENDENTE;

    @Column(name = "expiration_date")
    private LocalDate expirationDate;

    @Column(name = "payment_date")
    private LocalDate paymentDate;

    @ManyToOne(optional = false)
    @JoinColumn(name = "supplier_id", nullable = false)
    private Supplier supplier;


    public void validate() {
        if (description == null || description.isBlank()) {
            throw new InvalidPayableException("A descrição da conta a pagar é obrigatória");
        }

        if (amount == null) {
            throw new InvalidPayableException("O valor da conta a pagar é obrigatório");
        }

        if (amount.signum() < 0) {
            throw new InvalidPayableException("O valor da conta a pagar não pode ser negativo");
        }

        if (supplier == null) {
            throw new InvalidPayableException("O fornecedor da conta a pagar é obrigatório");
        }

        isValidStatusAndPaymentDate(status, paymentDate);
    }

    public void updateDetails(
            String description,
            BigDecimal amount,
            StatusPayableEnum status,
            LocalDate expirationDate,
            LocalDate paymentDate,
            Supplier supplier
    ) {
        this.description = description;
        this.amount = amount;
        this.status = status == null ? StatusPayableEnum.PENDENTE : status;
        this.expirationDate = expirationDate;
        this.paymentDate = paymentDate;
        this.supplier = supplier;

        validate();
    }

    public static Payable create(
            String description,
            BigDecimal amount,
            StatusPayableEnum status,
            LocalDate expirationDate,
            LocalDate paymentDate,
            Supplier supplier
    ) {

        StatusPayableEnum effectiveStatus =
                status == null ? StatusPayableEnum.PENDENTE : status;

        Payable payable = Payable.builder()
                .description(description)
                .amount(amount)
                .status(effectiveStatus)
                .expirationDate(expirationDate)
                .paymentDate(paymentDate)
                .supplier(supplier)
                .build();

        payable.validate();

        return payable;
    }

    public void updateStatus(StatusPayableEnum status, LocalDate paymentDate) {
        if (status == null) {
            throw new InvalidPayableException(
                    "O status da conta a pagar é obrigatório"
            );
        }

        if(this.status.equals(StatusPayableEnum.PAGO) && status != this.status) {
            this.paymentDate = null;
        }

        if(status.equals(StatusPayableEnum.PAGO) && paymentDate == null) {
            this.paymentDate = LocalDate.now();
        }else if(status.equals(StatusPayableEnum.PAGO)){
            this.paymentDate = paymentDate;
        }

        this.status = status;

        validate();
    }

    public void isValidStatusAndPaymentDate(StatusPayableEnum status, LocalDate paymentDate){
        isPaidAndPaymentDateIsNull(status, paymentDate);
        isNotPaidAndPaymentDateIsNotNull(status, paymentDate);
    }

    public void isPaidAndPaymentDateIsNull(StatusPayableEnum status, LocalDate paymentDate){
        if (status.equals(StatusPayableEnum.PAGO) && paymentDate == null) {
            throw new InvalidPayableException(
                    "A data de pagamento é obrigatório quando uma conta está com status PAGO"
            );
        }
    }

    public void isNotPaidAndPaymentDateIsNotNull(StatusPayableEnum status, LocalDate paymentDate){
        if (!status.equals(StatusPayableEnum.PAGO) && paymentDate != null) {
            throw new InvalidPayableException(
                    "A data de pagamento deve ser nula quando status é diferente de PAGO"
            );
        }
    }




}
