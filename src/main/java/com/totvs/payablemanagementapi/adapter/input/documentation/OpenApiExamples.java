package com.totvs.payablemanagementapi.adapter.input.documentation;

public final class OpenApiExamples {

    public static final String SUPPLIER_REQUEST = """
            {
              "name": "TOTVS"
            }
            """;

    public static final String PAYABLE_REQUEST = """
            {
              "description": "Pagamento de aluguel",
              "amount": 1500.00,
              "status": "PENDENTE",
              "expirationDate": "2026-08-10",
              "supplierId": 1
            }
            """;

    public static final String UPDATE_PAYABLE_STATUS_REQUEST = """
            {
              "status": 1,
              "paymentDate": "2026-08-10"
            }
            """;

    public static final String BAD_REQUEST_PROBLEM = """
            {
              "type": "about:blank",
              "title": "Bad Request",
              "status": 400,
              "detail": "Dados inválidos"
            }
            """;

    public static final String UNAUTHORIZED_PROBLEM = """
            {
              "type": "about:blank",
              "title": "Unauthorized",
              "status": 401,
              "detail": "Credenciais de autenticação ausentes ou inválidas"
            }
            """;

    public static final String NOT_FOUND_PROBLEM = """
            {
              "type": "about:blank",
              "title": "Not Found",
              "status": 404,
              "detail": "Recurso não encontrado"
            }
            """;

    public static final String CONFLICT_PROBLEM = """
            {
              "type": "about:blank",
              "title": "Conflict",
              "status": 409,
              "detail": "O recurso possui vínculos e não pode ser excluído"
            }
            """;

    public static final String UNSUPPORTED_MEDIA_TYPE_PROBLEM = """
            {
              "type": "about:blank",
              "title": "Unsupported Media Type",
              "status": 415,
              "detail": "O tipo de mídia enviado não é suportado para a importação"
            }
            """;

    public static final String INTERNAL_SERVER_ERROR_PROBLEM = """
            {
              "type": "about:blank",
              "title": "Internal Server Error",
              "status": 500,
              "detail": "Não foi possível publicar o evento de importação"
            }
            """;

    private OpenApiExamples() {
    }
}
