package com.totvs.payablemanagementapi.adapter.input.documentation;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public final class ApiDocumentationSchemas {

    private ApiDocumentationSchemas() {
    }

    @Schema(name = "SupplierRequest", description = "Dados para criação ou atualização de um fornecedor.")
    public record SupplierRequest(
            @Schema(description = "Nome do fornecedor.", example = "TOTVS", requiredMode = Schema.RequiredMode.REQUIRED)
            String name
    ) {
    }

    @Schema(name = "SupplierResponse", description = "Fornecedor cadastrado.")
    public record SupplierResponse(
            @Schema(description = "Identificador do fornecedor.", example = "1")
            Long id,
            @Schema(description = "Nome do fornecedor.", example = "TOTVS")
            String name
    ) {
    }

    @Schema(name = "PayableRequest", description = "Dados para criação ou atualização de uma conta a pagar.")
    public record PayableRequest(
            @Schema(
                    description = "Descrição da conta a pagar.",
                    example = "Pagamento de aluguel",
                    requiredMode = Schema.RequiredMode.REQUIRED
            )
            String description,
            @Schema(
                    description = "Valor monetário não negativo, com até duas casas decimais.",
                    example = "1500.00",
                    minimum = "0",
                    requiredMode = Schema.RequiredMode.REQUIRED
            )
            BigDecimal amount,
            @Schema(
                    description = """
                            Status da conta. Quando omitido, assume PENDENTE. PAGO exige paymentDate;
                            os demais status não admitem data de pagamento.
                            """,
                    example = "PENDENTE",
                    allowableValues = {"PENDENTE", "PAGO", "CANCELADO"}
            )
            String status,
            @Schema(description = "Data de vencimento no formato ISO-8601.", example = "2026-08-10")
            LocalDate expirationDate,
            @Schema(
                    description = "Data do pagamento, obrigatória somente para status PAGO.",
                    example = "2026-08-05"
            )
            LocalDate paymentDate,
            @Schema(
                    description = "Identificador de um fornecedor existente.",
                    example = "1",
                    requiredMode = Schema.RequiredMode.REQUIRED
            )
            Long supplierId
    ) {
    }

    @Schema(name = "UpdatePayableStatusRequest", description = "Alteração pontual do status de uma conta.")
    public record UpdatePayableStatusRequest(
            @Schema(
                    description = "Código do status: 0=PENDENTE, 1=PAGO, 2=CANCELADO.",
                    example = "1",
                    minimum = "0",
                    maximum = "2",
                    requiredMode = Schema.RequiredMode.REQUIRED
            )
            Integer status,
            @Schema(
                    description = """
                            Data de pagamento. Ao mudar para PAGO, a data atual é usada quando omitida.
                            Para outro status, a data armazenada é removida.
                            """,
                    example = "2026-08-10"
            )
            LocalDate paymentDate
    ) {
    }

    @Schema(name = "PayableResponse", description = "Conta a pagar persistida.")
    public record PayableResponse(
            @Schema(description = "Identificador da conta.", example = "1")
            Long id,
            @Schema(description = "Descrição da conta.", example = "Pagamento de aluguel")
            String description,
            @Schema(description = "Valor da conta.", example = "1500.00")
            BigDecimal amount,
            @Schema(
                    description = "Status atual da conta.",
                    example = "PENDENTE",
                    allowableValues = {"PENDENTE", "PAGO", "CANCELADO"}
            )
            String status,
            @Schema(description = "Data de vencimento.", example = "2026-08-10")
            LocalDate expirationDate,
            @Schema(description = "Data do pagamento, quando a conta estiver paga.", example = "2026-08-05")
            LocalDate paymentDate,
            @Schema(description = "Fornecedor vinculado.")
            SupplierResponse supplier
    ) {
    }

    @Schema(name = "PayableImportCsvRequest", description = "Arquivo CSV de contas a pagar.")
    public record PayableImportCsvRequest(
            @Schema(
                    description = """
                            CSV UTF-8 com cabeçalho obrigatório:
                            description,amount,status,expirationDate,paymentDate,supplierId
                            """,
                    type = "string",
                    format = "binary",
                    requiredMode = Schema.RequiredMode.REQUIRED
            )
            String file
    ) {
    }

    @Schema(name = "PayableImportationResponse", description = "Importação assíncrona de contas a pagar.")
    public record PayableImportationResponse(
            @Schema(description = "Identificador da importação.", example = "1")
            Long id,
            @Schema(description = "Data e hora de criação.", example = "2026-08-01T10:15:30")
            String createdAt,
            @Schema(description = "Data e hora da última atualização.", example = "2026-08-01T10:15:35")
            String updatedAt,
            @Schema(
                    description = "Estado do processamento assíncrono.",
                    example = "PENDING",
                    allowableValues = {
                            "PENDING", "PROCESSING", "COMPLETED", "COMPLETED_WITH_ERRORS", "FAILED"
                    }
            )
            String status,
            @Schema(description = "Erro global quando a importação estiver em FAILED.")
            String errorMessage
    ) {
    }

    @Schema(name = "PayableImportationItemResponse", description = "Resultado do processamento de uma linha do CSV.")
    public record PayableImportationItemResponse(
            @Schema(description = "Identificador do item.", example = "10")
            Long id,
            @Schema(description = "Conta criada quando o item possui SUCCESS.", example = "25")
            Long payableId,
            @Schema(description = "Importação à qual o item pertence.", example = "1")
            Long payableImportationId,
            @Schema(
                    description = "Resultado da linha.",
                    example = "SUCCESS",
                    allowableValues = {"SUCCESS", "ERROR"}
            )
            String status,
            @Schema(description = "Motivo da rejeição quando o item possui ERROR.")
            String errorMessage
    ) {
    }

    @Schema(name = "PaidPayableItemResponse", description = "Conta paga incluída no relatório.")
    public record PaidPayableItemResponse(
            @Schema(description = "Descrição da conta.", example = "Pagamento de aluguel")
            String description,
            @Schema(description = "Valor pago.", example = "1500.00")
            BigDecimal amount,
            @Schema(description = "Status da conta.", example = "PAGO", allowableValues = {"PAGO"})
            String status,
            @Schema(description = "Data de vencimento.", example = "2026-08-10")
            LocalDate expirationDate,
            @Schema(description = "Data do pagamento.", example = "2026-08-05")
            LocalDate paymentDate,
            @Schema(description = "Nome do fornecedor.", example = "TOTVS")
            String supplierName
    ) {
    }

    @Schema(name = "TotalPaidReportResponse", description = "Consolidação das contas pagas no período.")
    public record TotalPaidReportResponse(
            @Schema(description = "Início do período.", example = "2026-08-01")
            LocalDate startDate,
            @Schema(description = "Fim do período.", example = "2026-08-31")
            LocalDate endDate,
            @Schema(description = "Soma dos valores pagos.", example = "2390.90")
            BigDecimal totalPaid,
            @Schema(description = "Quantidade de contas pagas.", example = "2")
            int paidCount,
            @Schema(description = "Contas consideradas no cálculo.")
            List<PaidPayableItemResponse> paidPayableItems
    ) {
    }

    @Schema(name = "SortResponse", description = "Estado da ordenação da página.")
    public record SortResponse(
            @Schema(description = "Indica se não há ordenação.", example = "true")
            boolean empty,
            @Schema(description = "Indica se existe ordenação.", example = "false")
            boolean sorted,
            @Schema(description = "Indica se a consulta está sem ordenação.", example = "true")
            boolean unsorted
    ) {
    }

    @Schema(name = "PageableResponse", description = "Metadados da solicitação de página.")
    public record PageableResponse(
            @Schema(description = "Número da página, iniciado em zero.", example = "0")
            int pageNumber,
            @Schema(description = "Quantidade solicitada por página.", example = "20")
            int pageSize,
            @Schema(description = "Ordenação solicitada.")
            SortResponse sort,
            @Schema(description = "Deslocamento do primeiro registro.", example = "0")
            long offset,
            @Schema(description = "Indica uma consulta paginada.", example = "true")
            boolean paged,
            @Schema(description = "Indica uma consulta não paginada.", example = "false")
            boolean unpaged
    ) {
    }

    @Schema(name = "SupplierPageResponse", description = "Página de fornecedores.")
    public record SupplierPageResponse(
            List<SupplierResponse> content,
            PageableResponse pageable,
            int totalPages,
            long totalElements,
            boolean last,
            int size,
            int number,
            SortResponse sort,
            int numberOfElements,
            boolean first,
            boolean empty
    ) {
    }

    @Schema(name = "PayablePageResponse", description = "Página de contas a pagar.")
    public record PayablePageResponse(
            List<PayableResponse> content,
            PageableResponse pageable,
            int totalPages,
            long totalElements,
            boolean last,
            int size,
            int number,
            SortResponse sort,
            int numberOfElements,
            boolean first,
            boolean empty
    ) {
    }

    @Schema(name = "ProblemDetail", description = "Erro HTTP no formato RFC 9457.")
    public record ProblemDetailResponse(
            @Schema(description = "Identificador do tipo do problema.", example = "about:blank")
            String type,
            @Schema(description = "Título correspondente ao status HTTP.", example = "Bad Request")
            String title,
            @Schema(description = "Código HTTP.", example = "400")
            int status,
            @Schema(description = "Explicação específica do erro.", example = "Dados inválidos")
            String detail,
            @Schema(description = "URI da requisição que originou o erro.")
            String instance
    ) {
    }
}
