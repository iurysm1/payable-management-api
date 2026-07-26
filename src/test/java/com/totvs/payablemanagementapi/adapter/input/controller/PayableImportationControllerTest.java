package com.totvs.payablemanagementapi.adapter.input.controller;

import com.totvs.payablemanagementapi.core.port.input.PayableImportationServiceUseCase;
import com.totvs.payablemanagementapi.domain.PayableImportation;
import com.totvs.payablemanagementapi.domain.enums.StatusPayableImportationEnum;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
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

@WebMvcTest(PayableImportationController.class)
@Import(ApiExceptionHandler.class)
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
}
