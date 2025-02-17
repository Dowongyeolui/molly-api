package org.example.mollyapi;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.time.LocalDateTime;

public class JacksonTest {
    public static void main(String[] args) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());

        LocalDateTime now = LocalDateTime.now();
        String json = mapper.writeValueAsString(now);
        System.out.println("✅ LocalDateTime JSON 변환 결과: " + json);
    }
}