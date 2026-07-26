package com.totvs.payablemanagementapi.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@Getter
@RequiredArgsConstructor
public enum StatusPayableEnum {

    PENDENTE(0, "Conta pendente"),
    PAGO(1, "Conta paga"),
    CANCELADO(2, "Conta cancelada");


    private final int code;
    private final String description;

    public static StatusPayableEnum fromCode(int code) {
        return Arrays.stream(values())
                .filter(status -> status.code == code)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "Código de status de conta a pagar inválido: %d".formatted(code)));
    }

    public static StatusPayableEnum fromName(String name) {
        return Arrays.stream(values())
                .filter(status -> status.name().equalsIgnoreCase(name))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "Nome de status de conta a pagar inválido: %s".formatted(name)));
    }
}
