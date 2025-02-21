package org.example.mollyapi.common.client;

import org.example.mollyapi.product.dto.UploadFile;
import org.example.mollyapi.common.enums.ImageType;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

public interface ImageClient {

    Optional<UploadFile> upload(ImageType type, MultipartFile file);
    List<UploadFile> upload(ImageType type, List<MultipartFile> files);
}
