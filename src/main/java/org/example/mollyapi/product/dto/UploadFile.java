package org.example.mollyapi.product.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UploadFile {
    String uploadFileName;
    String storedFileName;
}