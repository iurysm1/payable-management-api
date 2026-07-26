package com.totvs.payablemanagementapi.adapter.output.storage;

import com.totvs.payablemanagementapi.core.exception.FileStorageException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LocalFileStorageAdapterTest {

    @TempDir
    Path temporaryDirectory;

    @Test
    void shouldSaveAndReadFile() throws Exception {
        LocalFileStorageAdapter storage = new LocalFileStorageAdapter(
                temporaryDirectory.resolve("imports").toString()
        );

        String storedPath = storage.saveCsvFile(
                new ByteArrayInputStream("description,amount".getBytes(StandardCharsets.UTF_8))
        );

        assertThat(storedPath).endsWith(".csv");
        assertThat(temporaryDirectory.resolve("imports").resolve(storedPath)).exists();

        try (InputStream file = storage.getFile(storedPath)) {
            assertThat(new String(file.readAllBytes(), StandardCharsets.UTF_8))
                    .isEqualTo("description,amount");
        }
    }

    @Test
    void shouldRejectPathOutsideStorageDirectory() {
        LocalFileStorageAdapter storage = new LocalFileStorageAdapter(temporaryDirectory.toString());

        assertThatThrownBy(() -> storage.getFile("../outside.csv"))
                .isInstanceOf(FileStorageException.class)
                .hasMessage("O caminho do arquivo é inválido");
    }

    @Test
    void shouldThrowWhenFileDoesNotExist() {
        LocalFileStorageAdapter storage = new LocalFileStorageAdapter(temporaryDirectory.toString());

        assertThatThrownBy(() -> storage.getFile("missing.csv"))
                .isInstanceOf(FileStorageException.class)
                .hasMessage("Não foi possível ler o arquivo: missing.csv");
    }

}
