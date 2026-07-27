package com.totvs.payablemanagementapi.adapter.output.storage;

import com.totvs.payablemanagementapi.core.exception.FileStorageException;
import com.totvs.payablemanagementapi.core.port.output.FileStoragePort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

@Component
public class LocalFileStorageAdapter implements FileStoragePort {

    private final Path basePath;

    public LocalFileStorageAdapter(
            @Value("${app.file-storage.base-path:./data/imports}") String basePath
    ) {
        this.basePath = Path.of(basePath).toAbsolutePath().normalize();
    }

    @Override
    public String saveCsvFile(InputStream content) {
        if (content == null) {
            throw new FileStorageException("O conteúdo do arquivo é obrigatório");
        }

        String fileName = UUID.randomUUID() + ".csv";
        Path targetPath = basePath.resolve(fileName);

        try {
            Files.createDirectories(basePath);
            Files.copy(content, targetPath);
            return basePath.relativize(targetPath).toString();
        } catch (IOException exception) {
            deleteQuietly(targetPath);
            throw new FileStorageException("Não foi possível salvar o arquivo", exception);
        }
    }

    @Override
    public InputStream getFile(String path) {
        Path filePath = resolveStoredPath(path);

        try {
            return Files.newInputStream(filePath);
        } catch (IOException exception) {
            throw new FileStorageException("Não foi possível ler o arquivo: " + path, exception);
        }
    }

    @Override
    public void deleteFile(String path) {
        Path filePath = resolveStoredPath(path);

        try {
            Files.deleteIfExists(filePath);
        } catch (IOException exception) {
            throw new FileStorageException("Não foi possível remover o arquivo: " + path, exception);
        }
    }

    private Path resolveStoredPath(String path) {
        if (path == null || path.isBlank()) {
            throw new FileStorageException("O caminho do arquivo é obrigatório");
        }

        Path resolvedPath = basePath.resolve(path).normalize();
        if (!resolvedPath.startsWith(basePath)) {
            throw new FileStorageException("O caminho do arquivo é inválido");
        }

        return resolvedPath;
    }

    private void deleteQuietly(Path filePath) {
        try {
            Files.deleteIfExists(filePath);
        } catch (IOException ignored) {
        }
    }
}
