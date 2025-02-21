package org.example.mollyapi.product.file;

import lombok.RequiredArgsConstructor;
import org.example.mollyapi.common.client.ImageClient;
import org.example.mollyapi.product.dto.UploadFile;
import org.example.mollyapi.common.enums.ImageType;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

@Component
@RequiredArgsConstructor
public class FileStore {

    private final ImageClient imageClient;

    public List<UploadFile> storeFiles(List<MultipartFile> files) {
        List<UploadFile> uploadFiles = new ArrayList<>();
        for (MultipartFile file : files) {
            uploadFiles.add(UploadFile.builder()
                    .storedFileName("storedFileName")
                    .uploadFileName("uploadFileName")
                    .build());
        }
        return uploadFiles;
    }

    public UploadFile storeFile(MultipartFile file) {

        Optional<UploadFile> upload = imageClient.upload(ImageType.PRODUCT, file);

        return upload.orElse(null);
    }
}
