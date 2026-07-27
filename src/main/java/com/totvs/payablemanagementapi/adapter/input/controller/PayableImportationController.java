package com.totvs.payablemanagementapi.adapter.input.controller;

import com.totvs.payablemanagementapi.adapter.input.documentation.ApiDocumentationSchemas.PayableImportationItemResponse;
import com.totvs.payablemanagementapi.adapter.input.documentation.ApiDocumentationSchemas.PayableImportationResponse;
import com.totvs.payablemanagementapi.adapter.input.documentation.ApiDocumentationSchemas.PayableImportCsvRequest;
import com.totvs.payablemanagementapi.adapter.input.documentation.ApiDocumentationSchemas.ProblemDetailResponse;
import com.totvs.payablemanagementapi.adapter.input.documentation.OpenApiConfiguration;
import com.totvs.payablemanagementapi.adapter.input.documentation.OpenApiExamples;
import com.totvs.payablemanagementapi.core.port.input.PayableImportationServiceUseCase;
import com.totvs.payablemanagementapi.domain.PayableImportation;
import com.totvs.payablemanagementapi.domain.PayableImportationItem;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Locale;

@RestController
@RequestMapping("/import/payable")
@RequiredArgsConstructor
@Tag(name = OpenApiConfiguration.TAG_IMPORTATIONS)
public class PayableImportationController {

    private final PayableImportationServiceUseCase payableImportationServiceUseCase;

    @GetMapping
    @Operation(
            operationId = "listPayableImportations",
            summary = "Listar importações",
            description = "Retorna todas as importações de contas a pagar e seus estados atuais."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Importações existentes.",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            array = @ArraySchema(schema = @Schema(implementation = PayableImportationResponse.class))
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
    public ResponseEntity<List<PayableImportation>> list() {
        return ResponseEntity.ok(payableImportationServiceUseCase.list());
    }

    @GetMapping("/{id}")
    @Operation(
            operationId = "findPayableImportationById",
            summary = "Consultar importação",
            description = "Retorna uma importação pelo identificador para acompanhamento assíncrono."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Importação encontrada.",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = PayableImportationResponse.class)
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
                    description = "Importação não encontrada.",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                            schema = @Schema(implementation = ProblemDetailResponse.class),
                            examples = @ExampleObject(value = OpenApiExamples.NOT_FOUND_PROBLEM)
                    )
            )
    })
    public ResponseEntity<PayableImportation> findById(
            @Parameter(description = "Identificador da importação.", example = "1", required = true)
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(payableImportationServiceUseCase.findById(id));
    }

    @GetMapping("/{id}/items")
    @Operation(
            operationId = "listPayableImportationItems",
            summary = "Listar itens da importação",
            description = """
                    Retorna o resultado de cada linha processada. SUCCESS possui payableId;
                    ERROR possui errorMessage e não cria uma conta.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Itens processados da importação.",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            array = @ArraySchema(schema = @Schema(implementation = PayableImportationItemResponse.class))
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
                    description = "Importação não encontrada.",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                            schema = @Schema(implementation = ProblemDetailResponse.class),
                            examples = @ExampleObject(value = OpenApiExamples.NOT_FOUND_PROBLEM)
                    )
            )
    })
    public ResponseEntity<List<PayableImportationItem>> listItems(
            @Parameter(description = "Identificador da importação.", example = "1", required = true)
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(payableImportationServiceUseCase.listPayableImportationItem(id));
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
            operationId = "createPayableImportation",
            summary = "Importar contas por CSV",
            description = """
                    Recebe um CSV UTF-8, salva o arquivo, cria uma importação PENDING e publica um
                    evento no RabbitMQ. O 201 confirma a aceitação, não o término. O consumidor muda
                    o estado para PROCESSING e depois COMPLETED, COMPLETED_WITH_ERRORS ou FAILED.

                    Cabeçalho obrigatório:
                    `description,amount,status,expirationDate,paymentDate,supplierId`.
                    Os status aceitos são PENDENTE, PAGO e CANCELADO; datas usam `yyyy-MM-dd`.
                    Linhas em branco são ignoradas e campos podem usar aspas duplas.
                    """,
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    description = "Arquivo CSV com extensão .csv.",
                    content = @Content(
                            mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                            schema = @Schema(implementation = PayableImportCsvRequest.class)
                    )
            )
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Importação aceita e aguardando processamento.",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = PayableImportationResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Arquivo ausente, vazio ou sem extensão CSV.",
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
                    responseCode = "415",
                    description = "Requisição não usa multipart/form-data.",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                            schema = @Schema(implementation = ProblemDetailResponse.class),
                            examples = @ExampleObject(value = OpenApiExamples.UNSUPPORTED_MEDIA_TYPE_PROBLEM)
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Falha ao armazenar o arquivo ou publicar o evento.",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                            schema = @Schema(implementation = ProblemDetailResponse.class),
                            examples = @ExampleObject(value = OpenApiExamples.INTERNAL_SERVER_ERROR_PROBLEM)
                    )
            )
    })
    public ResponseEntity<PayableImportation> create(
            @Parameter(hidden = true) @RequestParam("file") MultipartFile file
    ) throws IOException {
        validateFile(file);

        try (InputStream content = file.getInputStream()) {
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(payableImportationServiceUseCase.create(content));
        }
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O arquivo é obrigatório");
        }

        String fileName = file.getOriginalFilename();
        if (fileName == null || !fileName.toLowerCase(Locale.ROOT).endsWith(".csv")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O arquivo deve ser CSV");
        }
    }
}
