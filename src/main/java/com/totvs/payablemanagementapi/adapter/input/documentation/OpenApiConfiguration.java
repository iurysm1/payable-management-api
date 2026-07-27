package com.totvs.payablemanagementapi.adapter.input.documentation;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Payable Management API",
                version = "1.0.0",
                description = """
                        API para gestão de fornecedores, contas a pagar, importações assíncronas em CSV
                        e relatórios de pagamentos. Todos os recursos exigem autenticação HTTP Basic.
                        """
        ),
        tags = {
                @Tag(
                        name = OpenApiConfiguration.TAG_SUPPLIERS,
                        description = "Cadastro, consulta, atualização e exclusão de fornecedores."
                ),
                @Tag(
                        name = OpenApiConfiguration.TAG_PAYABLES,
                        description = "Gestão do ciclo de vida das contas a pagar."
                ),
                @Tag(
                        name = OpenApiConfiguration.TAG_IMPORTATIONS,
                        description = """
                                Importação assíncrona de contas a pagar por CSV e acompanhamento do
                                processamento e de seus itens.
                                """
                ),
                @Tag(
                        name = OpenApiConfiguration.TAG_REPORTS,
                        description = "Relatórios consolidados de contas efetivamente pagas."
                )
        },
        security = @SecurityRequirement(name = OpenApiConfiguration.BASIC_AUTH)
)
@SecurityScheme(
        name = OpenApiConfiguration.BASIC_AUTH,
        type = SecuritySchemeType.HTTP,
        scheme = "basic",
        description = "Credenciais configuradas por APP_SECURITY_USERNAME e APP_SECURITY_PASSWORD."
)
public class OpenApiConfiguration {

    public static final String BASIC_AUTH = "basicAuth";
    public static final String TAG_SUPPLIERS = "Fornecedores";
    public static final String TAG_PAYABLES = "Contas a pagar";
    public static final String TAG_IMPORTATIONS = "Importações";
    public static final String TAG_REPORTS = "Relatórios";

    @Bean
    OpenAPI payableManagementOpenApi() {
        return new OpenAPI()
                .servers(List.of(new Server()
                        .url("/management/rest")
                        .description("Servidor da aplicação")));
    }
}
