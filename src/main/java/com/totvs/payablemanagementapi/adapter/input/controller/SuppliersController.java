package com.totvs.payablemanagementapi.adapter.input.controller;

import com.totvs.payablemanagementapi.adapter.input.documentation.ApiDocumentationSchemas.ProblemDetailResponse;
import com.totvs.payablemanagementapi.adapter.input.documentation.ApiDocumentationSchemas.SupplierPageResponse;
import com.totvs.payablemanagementapi.adapter.input.documentation.ApiDocumentationSchemas.SupplierRequest;
import com.totvs.payablemanagementapi.adapter.input.documentation.ApiDocumentationSchemas.SupplierResponse;
import com.totvs.payablemanagementapi.adapter.input.documentation.OpenApiConfiguration;
import com.totvs.payablemanagementapi.adapter.input.documentation.OpenApiExamples;
import com.totvs.payablemanagementapi.core.port.input.SupplierUseCase;
import com.totvs.payablemanagementapi.core.port.input.dto.SupplierDto;
import com.totvs.payablemanagementapi.domain.Supplier;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/supplier")
@RequiredArgsConstructor
@Tag(name = OpenApiConfiguration.TAG_SUPPLIERS)
public class SuppliersController {

    private final SupplierUseCase supplierUseCase;

    @GetMapping
    @Operation(
            operationId = "listSuppliers",
            summary = "Listar fornecedores",
            description = "Retorna os fornecedores em uma página com ordenação opcional.",
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
                            example = "name,asc",
                            schema = @Schema(type = "string")
                    )
            }
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Página de fornecedores.",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = SupplierPageResponse.class)
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
    public ResponseEntity<Page<Supplier>> list(@Parameter(hidden = true) Pageable pageable) {
        return ResponseEntity.ok(supplierUseCase.list(pageable));
    }

    @GetMapping("/{id}")
    @Operation(
            operationId = "findSupplierById",
            summary = "Consultar fornecedor",
            description = "Retorna um fornecedor pelo identificador."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Fornecedor encontrado.",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = SupplierResponse.class)
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
                    description = "Fornecedor não encontrado.",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                            schema = @Schema(implementation = ProblemDetailResponse.class),
                            examples = @ExampleObject(value = OpenApiExamples.NOT_FOUND_PROBLEM)
                    )
            )
    })
    public ResponseEntity<Supplier> findById(
            @Parameter(description = "Identificador do fornecedor.", example = "1", required = true)
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(supplierUseCase.findById(id));
    }

    @PostMapping
    @Operation(
            operationId = "createSupplier",
            summary = "Criar fornecedor",
            description = "Cadastra um fornecedor com nome obrigatório.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    description = "Dados do novo fornecedor.",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = SupplierRequest.class),
                            examples = @ExampleObject(value = OpenApiExamples.SUPPLIER_REQUEST)
                    )
            )
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Fornecedor criado.",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = SupplierResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Nome ausente ou inválido.",
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
    public ResponseEntity<Supplier> save(@Valid @RequestBody SupplierDto supplierDto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(supplierUseCase.save(supplierDto));
    }

    @PutMapping("/{id}")
    @Operation(
            operationId = "updateSupplier",
            summary = "Atualizar fornecedor",
            description = "Atualiza o nome do fornecedor. O identificador da rota prevalece sobre eventual id do corpo.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    description = "Novos dados do fornecedor.",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = SupplierRequest.class),
                            examples = @ExampleObject(value = OpenApiExamples.SUPPLIER_REQUEST)
                    )
            )
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Fornecedor atualizado.",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = SupplierResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Nome ausente ou inválido.",
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
                    description = "Fornecedor não encontrado.",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                            schema = @Schema(implementation = ProblemDetailResponse.class),
                            examples = @ExampleObject(value = OpenApiExamples.NOT_FOUND_PROBLEM)
                    )
            )
    })
    public ResponseEntity<Supplier> update(
            @Parameter(description = "Identificador do fornecedor.", example = "1", required = true)
            @PathVariable Long id,
            @Valid @RequestBody SupplierDto supplierDto
    ) {
        SupplierDto supplierToUpdate = new SupplierDto(id, supplierDto.name());
        return ResponseEntity.ok(supplierUseCase.update(supplierToUpdate));
    }

    @DeleteMapping("/{id}")
    @Operation(
            operationId = "deleteSupplier",
            summary = "Excluir fornecedor",
            description = "Exclui um fornecedor que não possua contas a pagar vinculadas."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Fornecedor excluído.", content = @Content),
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
                    description = "Fornecedor não encontrado.",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                            schema = @Schema(implementation = ProblemDetailResponse.class),
                            examples = @ExampleObject(value = OpenApiExamples.NOT_FOUND_PROBLEM)
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Fornecedor possui contas vinculadas.",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                            schema = @Schema(implementation = ProblemDetailResponse.class),
                            examples = @ExampleObject(value = OpenApiExamples.CONFLICT_PROBLEM)
                    )
            )
    })
    public ResponseEntity<Void> delete(
            @Parameter(description = "Identificador do fornecedor.", example = "1", required = true)
            @PathVariable Long id
    ) {
        supplierUseCase.delete(id);
        return ResponseEntity.noContent().build();
    }
}
