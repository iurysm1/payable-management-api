package com.totvs.payablemanagementapi.core.port.output;

import java.io.InputStream;

public interface FileStoragePort {

    String saveCsvFile(InputStream content);

    InputStream getFile(String path);
}
