package com.totvs.payablemanagementapi.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@Getter
@RequiredArgsConstructor
public enum StatusPayableImportationItemEnum {

    SUCCESS(0, "Item importado com sucesso"),
    ERROR(1, "Item com erro");

    private final int code;
    private final String description;

    public static StatusPayableImportationItemEnum fromCode(int code) {
        return Arrays.stream(values())
                .filter(status -> status.code == code)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "Código de status de item de importação inválido: %d".formatted(code)));
    }

    public static StatusPayableImportationItemEnum fromName(String name) {
        return Arrays.stream(values())
                .filter(status -> status.name().equalsIgnoreCase(name))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "Nome de status de item de importação inválido: %s".formatted(name)));
    }
}
