package com.totvs.payablemanagementapi.adapter.input.controller;

import com.totvs.payablemanagementapi.adapter.input.security.SecurityConfiguration;
import com.totvs.payablemanagementapi.core.port.input.PayableImportationServiceUseCase;
import com.totvs.payablemanagementapi.core.exception.PayableImportationNotFoundException;
import com.totvs.payablemanagementapi.domain.PayableImportation;
import com.totvs.payablemanagementapi.domain.PayableImportationItem;
import com.totvs.payablemanagementapi.domain.enums.StatusPayableImportationEnum;
import com.totvs.payablemanagementapi.domain.enums.StatusPayableImportationItemEnum;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.mock.web.MockMultipartFile;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = PayableImportationController.class,
        properties = {
                "app.security.username=test-user",
                "app.security.password=test-password"
        }
)
@Import({ApiExceptionHandler.class, SecurityConfiguration.class})
@WithMockUser
class PayableImportationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PayableImportationServiceUseCase payableImportationServiceUseCase;

    @Test
    void shouldListPayableImportations() throws Exception {
        when(payableImportationServiceUseCase.list()).thenReturn(List.of(importation(1L)));

        mockMvc.perform(get("/import/payable"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    void shouldFindPayableImportationById() throws Exception {
        when(payableImportationServiceUseCase.findById(1L)).thenReturn(importation(1L));

        mockMvc.perform(get("/import/payable/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void shouldListPayableImportationItems() throws Exception {
        when(payableImportationServiceUseCase.listPayableImportationItem(1L))
                .thenReturn(List.of(item(2L, 1L)));

        mockMvc.perform(get("/import/payable/1/items"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(2))
                .andExpect(jsonPath("$[0].payableImportationId").value(1));
    }

    @Test
    void shouldReturnNotFoundWhenPayableImportationDoesNotExist() throws Exception {
        when(payableImportationServiceUseCase.findById(99L))
                .thenThrow(new PayableImportationNotFoundException(99L));

        mockMvc.perform(get("/import/payable/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail")
                        .value("Importação de contas a pagar com id 99 não encontrada"));
    }

    @Test
    void shouldCreatePayableImportationFromCsvFile() throws Exception {
        when(payableImportationServiceUseCase.create(any(InputStream.class))).thenReturn(importation(1L));

        mockMvc.perform(multipart("/import/payable")
                        .file(new MockMultipartFile(
                                "file",
                                "payables.csv",
                                MediaType.TEXT_PLAIN_VALUE,
                                "description,amount".getBytes()
                        )))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));

        verify(payableImportationServiceUseCase).create(any(InputStream.class));
    }

    @Test
    void shouldRejectRequestWithoutFile() throws Exception {
        mockMvc.perform(multipart("/import/payable"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("É necessário enviar o arquivo para a importação"));
    }

    @Test
    void shouldRejectEmptyCsvFileWithDetails() throws Exception {
        mockMvc.perform(multipart("/import/payable")
                        .file(new MockMultipartFile(
                                "file",
                                "payables.csv",
                                MediaType.TEXT_PLAIN_VALUE,
                                new byte[0]
                        )))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("O arquivo enviado está vazio"));
    }

    @Test
    void shouldRejectNonCsvFile() throws Exception {
        mockMvc.perform(multipart("/import/payable")
                        .file(new MockMultipartFile(
                                "file",
                                "payables.txt",
                                MediaType.TEXT_PLAIN_VALUE,
                                "description,amount".getBytes()
                        )))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldRejectUnsupportedMediaType() throws Exception {
        mockMvc.perform(post("/import/payable")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnsupportedMediaType())
                .andExpect(jsonPath("$.detail").value(
                        "O tipo de mídia enviado não é suportado para a importação"
                ));
    }

    private PayableImportation importation(Long id) {
        LocalDateTime now = LocalDateTime.now();
        return new PayableImportation(id, now, now, StatusPayableImportationEnum.PENDING, null);
    }

    private PayableImportationItem item(Long id, Long importationId) {
        return PayableImportationItem.builder()
                .id(id)
                .payableImportationId(importationId)
                .payableId(3L)
                .status(StatusPayableImportationItemEnum.SUCCESS)
                .build();
    }
}
