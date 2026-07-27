package com.totvs.payablemanagementapi.adapter.input.documentation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.totvs.payablemanagementapi.adapter.input.controller.ApiExceptionHandler;
import com.totvs.payablemanagementapi.adapter.input.controller.PayableImportationController;
import com.totvs.payablemanagementapi.adapter.input.controller.PayablesController;
import com.totvs.payablemanagementapi.adapter.input.controller.SuppliersController;
import com.totvs.payablemanagementapi.adapter.input.controller.reports.TotalPaidReportController;
import com.totvs.payablemanagementapi.adapter.input.security.SecurityConfiguration;
import com.totvs.payablemanagementapi.core.port.input.PayableImportationServiceUseCase;
import com.totvs.payablemanagementapi.core.port.input.PayableUseCase;
import com.totvs.payablemanagementapi.core.port.input.SupplierUseCase;
import com.totvs.payablemanagementapi.core.port.input.reports.TotalPaidReportUseCase;
import org.junit.jupiter.api.Test;
import org.springdoc.core.configuration.SpringDocConfiguration;
import org.springdoc.core.properties.SpringDocConfigProperties;
import org.springdoc.core.properties.SwaggerUiConfigProperties;
import org.springdoc.core.properties.SwaggerUiOAuthProperties;
import org.springdoc.webmvc.core.configuration.SpringDocWebMvcConfiguration;
import org.springdoc.webmvc.ui.SwaggerConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = {
                SuppliersController.class,
                PayablesController.class,
                PayableImportationController.class,
                TotalPaidReportController.class
        },
        properties = {
                "app.security.username=test-user",
                "app.security.password=test-password",
                "springdoc.api-docs.version=OPENAPI_3_1",
                "springdoc.writer-with-default-pretty-printer=true",
                "springdoc.packages-to-scan=com.totvs.payablemanagementapi.adapter.input.controller",
                "springdoc.paths-to-match=/supplier/**,/payable/**,/import/payable/**,/report/**",
                "springdoc.swagger-ui.path=/swagger-ui.html",
                "springdoc.swagger-ui.disable-swagger-default-url=true"
        }
)
@Import({
        ApiExceptionHandler.class,
        SecurityConfiguration.class,
        OpenApiConfiguration.class
})
@ImportAutoConfiguration({
        SpringDocConfiguration.class,
        SpringDocConfigProperties.class,
        SpringDocWebMvcConfiguration.class,
        SwaggerConfig.class,
        SwaggerUiConfigProperties.class,
        SwaggerUiOAuthProperties.class
})
@WithMockUser
class OpenApiContractTest {

    private static final Set<String> HTTP_METHODS = Set.of("get", "post", "put", "patch", "delete");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private SupplierUseCase supplierUseCase;

    @MockitoBean
    private PayableUseCase payableUseCase;

    @MockitoBean
    private PayableImportationServiceUseCase payableImportationServiceUseCase;

    @MockitoBean
    private TotalPaidReportUseCase totalPaidReportUseCase;

    @Test
    void shouldKeepGeneratedOpenApiSynchronizedWithVersionedSnapshot() throws Exception {
        String generatedContent = mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode generated = objectMapper.readTree(generatedContent);
        JsonNode snapshot = objectMapper.readTree(Files.readString(Path.of("docs/openapi.json")));

        assertThat(generated).isEqualTo(snapshot);
    }

    @Test
    void shouldExposeCompleteDocumentedContract() throws Exception {
        String generatedContent = mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode openApi = objectMapper.readTree(generatedContent);

        assertThat(openApi.path("openapi").asText()).isEqualTo("3.1.0");
        assertThat(openApi.path("components").path("securitySchemes").has("basicAuth")).isTrue();
        assertThat(openApi.path("security").get(0).has("basicAuth")).isTrue();
        assertThat(openApi.path("tags")).hasSize(4);
        assertThat(openApi.path("paths")).hasSize(9);
        assertThat(countOperations(openApi.path("paths"))).isEqualTo(16);
        assertThat(openApi.at("/paths/~1import~1payable/post/responses").has("415")).isTrue();
        assertThat(openApi.at("/paths/~1supplier~1{id}/delete/responses").has("409")).isTrue();
        assertThat(openApi.at("/paths/~1payable~1{id}~1status/patch/requestBody").isMissingNode()).isFalse();
    }

    @Test
    @WithAnonymousUser
    void shouldProtectDocumentationEndpoints() throws Exception {
        for (String endpoint : Set.of("/v3/api-docs", "/swagger-ui.html", "/redoc.html")) {
            mockMvc.perform(get(endpoint))
                    .andExpect(status().isUnauthorized())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                    .andExpect(header().string(
                            HttpHeaders.WWW_AUTHENTICATE,
                            "Basic realm=\"payable-management-api\""
                    ));
        }
    }

    @Test
    void shouldServeSwaggerAndRedocToAuthenticatedUsers() throws Exception {
        mockMvc.perform(get("/swagger-ui.html"))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/swagger-ui/index.html"));

        mockMvc.perform(get("/redoc.html"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_HTML));
    }

    private int countOperations(JsonNode paths) {
        int count = 0;
        Iterator<Map.Entry<String, JsonNode>> pathEntries = paths.fields();

        while (pathEntries.hasNext()) {
            Iterator<String> methodNames = pathEntries.next().getValue().fieldNames();
            while (methodNames.hasNext()) {
                if (HTTP_METHODS.contains(methodNames.next())) {
                    count++;
                }
            }
        }

        return count;
    }
}
