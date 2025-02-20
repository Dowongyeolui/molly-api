package org.example.mollyapi.product.file;

import lombok.RequiredArgsConstructor;
import org.example.mollyapi.product.client.ImageClient;
import org.example.mollyapi.product.dto.UploadFile;
import org.example.mollyapi.product.enums.ImageType;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriComponentsBuilder;

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
