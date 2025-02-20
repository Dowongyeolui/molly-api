package org.example.mollyapi.product.dto;

import lombok.Builder;
import lombok.Data;

@Data
public class UploadFile {
    String uploadFileName;
    String storedFileName;

    @Builder
    UploadFile(String uploadFileName, String storedFileName) {
        this.uploadFileName = uploadFileName;
        this.storedFileName = storedFileName;
    }
}