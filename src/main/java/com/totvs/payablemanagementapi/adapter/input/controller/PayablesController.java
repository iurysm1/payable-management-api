package com.totvs.payablemanagementapi.adapter.input.controller;

import com.totvs.payablemanagementapi.adapter.input.documentation.ApiDocumentationSchemas.PayablePageResponse;
import com.totvs.payablemanagementapi.adapter.input.documentation.ApiDocumentationSchemas.PayableRequest;
import com.totvs.payablemanagementapi.adapter.input.documentation.ApiDocumentationSchemas.PayableResponse;
import com.totvs.payablemanagementapi.adapter.input.documentation.ApiDocumentationSchemas.ProblemDetailResponse;
import com.totvs.payablemanagementapi.adapter.input.documentation.ApiDocumentationSchemas.UpdatePayableStatusRequest;
import com.totvs.payablemanagementapi.adapter.input.documentation.OpenApiConfiguration;
import com.totvs.payablemanagementapi.adapter.input.documentation.OpenApiExamples;
import com.totvs.payablemanagementapi.core.port.input.PayableUseCase;
import com.totvs.payablemanagementapi.core.port.input.dto.PayableDto;
import com.totvs.payablemanagementapi.core.port.input.dto.PayableFilterDto;
import com.totvs.payablemanagementapi.core.port.input.dto.UpdatePayableStatusDto;
import com.totvs.payablemanagementapi.core.util.DatePeriodCriteria;
import com.totvs.payablemanagementapi.domain.Payable;
import com.totvs.payablemanagementapi.domain.enums.StatusPayableEnum;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/payable")
@RequiredArgsConstructor
@Tag(name = OpenApiConfiguration.TAG_PAYABLES)
public class PayablesController {

    private final PayableUseCase payableUseCase;

    @GetMapping
    @Operation(
            operationId = "listPayables",
            summary = "Listar contas a pagar",
            description = """
                    Retorna contas paginadas. As datas filtram o vencimento e devem ser informadas
                    juntas; a data inicial não pode ser posterior à final.
                    """,
            parameters = {
                    @Parameter(
                            name = "page",
                            in = ParameterIn.QUERY,
                            description = "Número da página, iniciado em zero.",
                            schema = @Schema(type = "integer", defaultValue = "0", minimum = "0")
                    ),
                    @Parameter(
                            name = "size",
                            in = ParameterIn.QUERY,
                            description = "Quantidade de registros por página.",
                            schema = @Schema(type = "integer", defaultValue = "20", minimum = "1")
                    ),
                    @Parameter(
                            name = "sort",
                            in = ParameterIn.QUERY,
                            description = "Ordenação no formato propriedade,direção. Pode ser repetido.",
                            example = "expirationDate,asc",
                            schema = @Schema(type = "string")
                    )
            }
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Página de contas a pagar.",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = PayablePageResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Filtro de status ou período inválido.",
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
            )
    })
    public ResponseEntity<Page<Payable>> list(
            @Parameter(description = "Trecho da descrição, sem distinção entre maiúsculas e minúsculas.", example = "aluguel")
            @RequestParam(required = false) String description,
            @Parameter(description = "Início opcional do período de vencimento.", example = "2026-08-01")
            @RequestParam(required = false) LocalDate startDate,
            @Parameter(description = "Fim opcional do período de vencimento.", example = "2026-08-31")
            @RequestParam(required = false) LocalDate endDate,
            @Parameter(
                    description = "Status por nome, sem distinção entre maiúsculas e minúsculas.",
                    example = "PAGO",
                    schema = @Schema(allowableValues = {"PENDENTE", "PAGO", "CANCELADO"})
            )
            @RequestParam(required = false) String status,
            @Parameter(hidden = true) Pageable pageable
    ) {
        DatePeriodCriteria periodCriteria = new DatePeriodCriteria(startDate, endDate);
        StatusPayableEnum statusFilter = status == null ? null : StatusPayableEnum.fromName(status);
        PayableFilterDto filter = new PayableFilterDto(description, periodCriteria, statusFilter);

        return ResponseEntity.ok(payableUseCase.list(pageable, filter));
    }

    @GetMapping("/{id}")
    @Operation(
            operationId = "findPayableById",
            summary = "Consultar conta a pagar",
            description = "Retorna uma conta a pagar e o fornecedor associado."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Conta a pagar encontrada.",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = PayableResponse.class)
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
                    description = "Conta a pagar não encontrada.",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                            schema = @Schema(implementation = ProblemDetailResponse.class),
                            examples = @ExampleObject(value = OpenApiExamples.NOT_FOUND_PROBLEM)
                    )
            )
    })
    public ResponseEntity<Payable> findById(
            @Parameter(description = "Identificador da conta a pagar.", example = "1", required = true)
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(payableUseCase.findById(id));
    }

    @PostMapping
    @Operation(
            operationId = "createPayable",
            summary = "Criar conta a pagar",
            description = """
                    Cadastra uma conta para um fornecedor existente. O status padrão é PENDENTE.
                    Uma conta PAGO exige paymentDate e outros status exigem essa data nula.
                    """,
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    description = "Dados da nova conta a pagar.",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = PayableRequest.class),
                            examples = @ExampleObject(value = OpenApiExamples.PAYABLE_REQUEST)
                    )
            )
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Conta a pagar criada.",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = PayableResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Dados violam uma regra da conta a pagar.",
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
                    description = "Fornecedor informado não existe.",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                            schema = @Schema(implementation = ProblemDetailResponse.class),
                            examples = @ExampleObject(value = OpenApiExamples.NOT_FOUND_PROBLEM)
                    )
            )
    })
    public ResponseEntity<Payable> save(@Valid @RequestBody PayableDto payableDto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(payableUseCase.save(payableDto));
    }

    @PutMapping("/{id}")
    @Operation(
            operationId = "updatePayable",
            summary = "Atualizar conta a pagar",
            description = "Substitui os dados da conta. O identificador da rota prevalece sobre eventual id do corpo.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    description = "Novos dados da conta a pagar.",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = PayableRequest.class),
                            examples = @ExampleObject(value = OpenApiExamples.PAYABLE_REQUEST)
                    )
            )
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Conta a pagar atualizada.",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = PayableResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Dados violam uma regra da conta a pagar.",
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
                    description = "Conta ou fornecedor não encontrado.",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                            schema = @Schema(implementation = ProblemDetailResponse.class),
                            examples = @ExampleObject(value = OpenApiExamples.NOT_FOUND_PROBLEM)
                    )
            )
    })
    public ResponseEntity<Payable> update(
            @Parameter(description = "Identificador da conta a pagar.", example = "1", required = true)
            @PathVariable Long id,
            @Valid @RequestBody PayableDto payableDto
    ) {
        PayableDto payableToUpdate = new PayableDto(
                id,
                payableDto.description(),
                payableDto.amount(),
                payableDto.status(),
                payableDto.expirationDate(),
                payableDto.paymentDate(),
                payableDto.supplierId()
        );

        return ResponseEntity.ok(payableUseCase.update(payableToUpdate));
    }

    @DeleteMapping("/{id}")
    @Operation(
            operationId = "deletePayable",
            summary = "Excluir conta a pagar",
            description = "Exclui definitivamente uma conta a pagar existente."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Conta a pagar excluída.", content = @Content),
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
                    description = "Conta a pagar não encontrada.",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                            schema = @Schema(implementation = ProblemDetailResponse.class),
                            examples = @ExampleObject(value = OpenApiExamples.NOT_FOUND_PROBLEM)
                    )
            )
    })
    public ResponseEntity<Void> delete(
            @Parameter(description = "Identificador da conta a pagar.", example = "1", required = true)
            @PathVariable Long id
    ) {
        payableUseCase.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/status")
    @Operation(
            operationId = "updatePayableStatus",
            summary = "Alterar status da conta",
            description = """
                    Atualiza somente o status. Ao passar para PAGO sem paymentDate, utiliza a data atual
                    ou preserva a data anterior se a conta já estava paga. Outros status removem a data.
                    """,
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    description = "Código do novo status e data de pagamento opcional.",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = UpdatePayableStatusRequest.class),
                            examples = @ExampleObject(value = OpenApiExamples.UPDATE_PAYABLE_STATUS_REQUEST)
                    )
            )
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Status atualizado ou conta retornada sem alteração.",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = PayableResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Código de status ausente, inexistente ou combinação inválida.",
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
                    description = "Conta a pagar não encontrada.",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                            schema = @Schema(implementation = ProblemDetailResponse.class),
                            examples = @ExampleObject(value = OpenApiExamples.NOT_FOUND_PROBLEM)
                    )
            )
    })
    public ResponseEntity<Payable> updateStatus(
            @Parameter(description = "Identificador da conta a pagar.", example = "1", required = true)
            @PathVariable Long id,
            @Valid @RequestBody UpdatePayableStatusDto updatePayableStatusDto
    ) {
        return ResponseEntity.ok(payableUseCase.updateStatus(id, updatePayableStatusDto));
    }
}
