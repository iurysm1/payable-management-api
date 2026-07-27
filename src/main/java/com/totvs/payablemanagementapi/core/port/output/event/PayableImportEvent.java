package com.totvs.payablemanagementapi.core.port.output.event;

public record PayableImportEvent(
        Long payableImportationId,
        String payableImportationCsvFilePath
) {
}
