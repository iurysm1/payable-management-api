package com.totvs.payablemanagementapi.adapter.input.controller.reports;

import com.totvs.payablemanagementapi.adapter.input.documentation.ApiDocumentationSchemas.ProblemDetailResponse;
import com.totvs.payablemanagementapi.adapter.input.documentation.ApiDocumentationSchemas.TotalPaidReportResponse;
import com.totvs.payablemanagementapi.adapter.input.documentation.OpenApiConfiguration;
import com.totvs.payablemanagementapi.adapter.input.documentation.OpenApiExamples;
import com.totvs.payablemanagementapi.core.port.input.dto.reports.TotalPaidReportDto;
import com.totvs.payablemanagementapi.core.port.input.dto.reports.TotalPaidReportFilterDto;
import com.totvs.payablemanagementapi.core.port.input.reports.TotalPaidReportUseCase;
import com.totvs.payablemanagementapi.core.util.DatePeriodCriteriaRequired;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/report")
@RequiredArgsConstructor
@Tag(name = OpenApiConfiguration.TAG_REPORTS)
public class TotalPaidReportController {

    private final TotalPaidReportUseCase totalPaidReportUseCase;

    @GetMapping("/total-paid")
    @Operation(
            operationId = "generateTotalPaidReport",
            summary = "Gerar relatório de pagamentos",
            description = """
                    Consolida as contas com status PAGO cuja paymentDate esteja no período inclusivo.
                    supplierId é opcional; startDate e endDate são obrigatórias e devem formar um
                    período válido.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Relatório consolidado.",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = TotalPaidReportResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Período ausente ou inválido.",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                            schema = @Schema(implementation = ProblemDetailResponse.class),
                            examples = @ExampleObject(value = OpenApiExamples.BAD_REQUEST_PROBLEM)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Credenciais ausentes ou inválidas.",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                            schema = @Schema(implementation = ProblemDetailResponse.class),
                            examples = @ExampleObject(value = OpenApiExamples.UNAUTHORIZED_PROBLEM)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Fornecedor não existe ou não há pagamentos no período.",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                            schema = @Schema(implementation = ProblemDetailResponse.class),
                            examples = @ExampleObject(value = OpenApiExamples.NOT_FOUND_PROBLEM)
                    )
            )
    })
    public ResponseEntity<TotalPaidReportDto> totalPaid(
            @Parameter(description = "Fornecedor opcional para restringir o relatório.", example = "1")
            @RequestParam(required = false) Long supplierId,
            @Parameter(
                    description = "Data inicial inclusiva no formato ISO-8601.",
                    example = "2026-08-01",
                    required = true
            )
            @RequestParam(required = false) LocalDate startDate,
            @Parameter(
                    description = "Data final inclusiva no formato ISO-8601.",
                    example = "2026-08-31",
                    required = true
            )
            @RequestParam(required = false) LocalDate endDate
    ) {
        TotalPaidReportFilterDto filter = new TotalPaidReportFilterDto(
                supplierId,
                new DatePeriodCriteriaRequired(startDate, endDate)
        );

        return ResponseEntity.ok(totalPaidReportUseCase.processReport(filter));
    }
}
