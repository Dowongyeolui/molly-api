package org.example.mollyapi.product.file;

import org.example.mollyapi.product.dto.UploadFile;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Component
public class FileStore {

    public List<UploadFile> storeFiles(List<MultipartFile> files) {
        List<UploadFile> uploadFiles = new ArrayList<>();
        for (MultipartFile file : files) {
            uploadFiles.add(new UploadFile("uploadFileName", "storedFileName"));
        }
        return uploadFiles;
    }

    public UploadFile storeFile(MultipartFile file) {

        return new UploadFile("uploadFileName", "storedFileName");
    }
}
