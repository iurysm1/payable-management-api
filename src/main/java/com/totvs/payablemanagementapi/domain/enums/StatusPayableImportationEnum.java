package com.totvs.payablemanagementapi.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@Getter
@RequiredArgsConstructor
public enum StatusPayableImportationEnum {

    PENDING(0, "Importação pendente"),
    PROCESSING(1, "Importação em processamento"),
    COMPLETED(2, "Importação concluída"),
    COMPLETED_WITH_ERRORS(3, "Importação concluída com erros"),
    FAILED(4, "Importação falhou");

    private final int code;
    private final String description;

    public static StatusPayableImportationEnum fromCode(int code) {
        return Arrays.stream(values())
                .filter(status -> status.code == code)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "Código de status de importação inválido: %d".formatted(code)));
    }

    public static StatusPayableImportationEnum fromName(String name) {
        return Arrays.stream(values())
                .filter(status -> status.name().equalsIgnoreCase(name))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "Nome de status de importação inválido: %s".formatted(name)));
    }
}
