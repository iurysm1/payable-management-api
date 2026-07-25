package com.totvs.payablemanagementapi.domain;


import com.totvs.payablemanagementapi.domain.exception.InvalidSupplierException;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Supplier {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String name;

    public static Supplier create(String name) {
        Supplier supplier = new Supplier(null, name);
        supplier.validate();
        return supplier;
    }

    public void updateDetails(String name) {
        this.name = name;
        validate();
    }

    public void validate() {
        if (name == null || name.isBlank()) {
            throw new InvalidSupplierException("O nome do fornecedor é obrigatório");
        }
    }
}
