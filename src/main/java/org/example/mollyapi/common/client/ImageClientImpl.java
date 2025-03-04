package org.example.mollyapi.common.client;

import lombok.Data;
import org.example.mollyapi.common.exception.CustomException;
import org.example.mollyapi.product.dto.UploadFile;
import org.example.mollyapi.common.enums.ImageType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.Optional;

import static org.example.mollyapi.common.exception.error.impl.ReviewError.FAIL_DELETE;

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

    @Override
    public void delete(ImageType type, String url) {
        // 쿼리 파라미터 설정: URL을 쿼리 파라미터로 포함
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(storageUrl)
                .queryParam("url", url);

        // URL과 쿼리 파라미터가 잘 설정되었는지 확인
        System.out.println("Final URL: " + uriBuilder.toUriString());

        // 요청 본문을 비워둠 (DELETE 요청이므로 본문은 필요 없음)
        HttpEntity<Void> httpEntity = new HttpEntity<>(null);

        // HTTP DELETE 요청 보내기
        ResponseEntity<Void> response = restTemplate.exchange(uriBuilder.toUriString(), HttpMethod.DELETE, httpEntity, Void.class);

        // 응답 상태가 OK일 경우 true 반환
        if(!response.getStatusCode().equals(HttpStatus.OK))
            throw new CustomException(FAIL_DELETE);
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
