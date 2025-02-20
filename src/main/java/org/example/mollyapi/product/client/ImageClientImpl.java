package org.example.mollyapi.product.client;

import lombok.Data;
import org.example.mollyapi.product.dto.UploadFile;
import org.example.mollyapi.product.enums.ImageType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.Optional;

@Component
public class ImageClientImpl implements ImageClient {

    @Value("${file.storage}")
    private String storageUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public Optional<UploadFile> upload(ImageType type, MultipartFile file) {
        // 쿼리 파라미터 설정
        String uri = buildUriComponent(type);
        // ContentType 설정
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "multipart/form-data");
        // body 설정
        LinkedMultiValueMap<Object, Object> requestBody = new LinkedMultiValueMap<>();
        requestBody.add("image", file.getResource());

        HttpEntity<LinkedMultiValueMap<Object, Object>> httpEntity = new HttpEntity<>(requestBody, headers);

        UrlDto responseBody = restTemplate.exchange(uri, HttpMethod.POST, httpEntity, UrlDto.class).getBody();

        if (responseBody != null) {
            return Optional.of(UploadFile.builder()
                    .storedFileName(responseBody.url)
                    .uploadFileName(file.getOriginalFilename())
                    .build()
            );
        }
        return Optional.empty();
    }

    public List<UploadFile> upload(ImageType type, List<MultipartFile> files) {
        return files.stream().map(file -> upload(type, file).orElseThrow()).toList();
    }


    @Data
    private static class UrlDto {
        private String url;
    }

    private String buildUriComponent(ImageType type) {
        boolean isProduct = (type == ImageType.PRODUCT);

        return UriComponentsBuilder.fromHttpUrl(storageUrl)
                .queryParam("isProduct", String.valueOf(isProduct))
                .build().toString();
    }
}
