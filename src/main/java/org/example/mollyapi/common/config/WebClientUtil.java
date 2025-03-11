package org.example.mollyapi.common.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebClientUtil {

    private final WebClientConfig webClientConfig;

    public <T,V> ResponseEntity<T> post(String url, V requestDto, Class<T> responseDtoClass, Map<String, String> headers) {
        return webClientConfig.webClient().method(HttpMethod.POST)
                .uri(url)
                .headers(httpHeaders -> headers.forEach(httpHeaders::set))
                .bodyValue(requestDto)
                .retrieve()
                .toEntity(responseDtoClass)
                .block(); //동기식
    }

    public <T, V> ResponseEntity<T> get(String url, V requestDto, Class<T> responseDtoClass, Map<String, String> headers) {
        return webClientConfig.webClient().method(HttpMethod.GET)
                .uri(url)
                .headers(httpHeaders -> headers.forEach(httpHeaders::set))
                .retrieve()
                .toEntity(responseDtoClass)
                .block();
    }
}


