package org.example.mollyapi.product.service;


import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

public interface ProductBulkService {

    List<Map<String, String>> saveChunkOfBulkProducts(MultipartFile file, Long userId);
}
