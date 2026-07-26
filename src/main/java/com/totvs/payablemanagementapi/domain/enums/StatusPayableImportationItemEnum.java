package com.totvs.payablemanagementapi.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@Getter
@RequiredArgsConstructor
public enum StatusPayableImportationItemEnum {

    PENDING(0, "Item pendente"),
    PROCESSING(1, "Item em processamento"),
    COMPLETED(2, "Item concluído"),
    ERROR(3, "Item com erro");

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
