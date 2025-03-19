package org.example.mollyapi.common.config;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final long MAX_AGE_SECS = 3600;

    private final MultipartJsonMessageConverter multipartJsonMessageConverter;

    private final LoggingInterceptor loggingInterceptor;


    @Autowired
    public WebConfig(MultipartJsonMessageConverter multipartJsonMessageConverter, LoggingInterceptor loggingInterceptor) {
        this.multipartJsonMessageConverter = multipartJsonMessageConverter;
        this.loggingInterceptor = loggingInterceptor;
    }

    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        converters.add(multipartJsonMessageConverter);
    }


    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(loggingInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns("/actuator/prometheus", "/error");
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("http://localhost:3000", "https://molly-client.vercel.app")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(MAX_AGE_SECS);

        WebMvcConfigurer.super.addCorsMappings(registry);
    }
}
