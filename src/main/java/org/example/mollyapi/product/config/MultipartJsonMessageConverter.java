package org.example.mollyapi.product.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.AbstractJackson2HttpMessageConverter;
import org.springframework.stereotype.Component;

@Component
public class MultipartJsonMessageConverter extends AbstractJackson2HttpMessageConverter {

    // multipart/form-data에 포함되는 application/octet-stream 처리
    public MultipartJsonMessageConverter() {
        super(new ObjectMapper(), MediaType.APPLICATION_OCTET_STREAM);
    }
}
