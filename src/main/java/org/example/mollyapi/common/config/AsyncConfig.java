package org.example.mollyapi.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

@Configuration
@EnableAsync
public class AsyncConfig {
    @Bean(name = "preProcessOrderExecutor")
    public Executor preProcessOrderExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10);
        executor.setMaxPoolSize(100);
        executor.setQueueCapacity(150);
        executor.setThreadNamePrefix("preProcessOrderExecutor Async-");
        // 설정 적용
        executor.initialize();
        return executor;
    }

    @Bean(name = "processOrderExecutor")
    public Executor processOrderExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10);
        executor.setMaxPoolSize(200);
        executor.setQueueCapacity(250);
        executor.setThreadNamePrefix("processOrderExecutor Async-");
        // 설정 적용
        executor.initialize();
        return executor;
    }

    @Bean(name = "paymentExecutor")
    public Executor paymentExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10);
        executor.setMaxPoolSize(100);
        executor.setQueueCapacity(150);
        executor.setThreadNamePrefix("paymentExecutor Async-");
        // 설정 적용
        executor.initialize();
        return executor;
    }

    @Bean(name = "reviewLikeExecutor")
    public Executor reviewLikeExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10);    // 기본 스레드 개수
        executor.setMaxPoolSize(50);     // 최대 스레드 개수
        executor.setQueueCapacity(100);  // 대기열 크기
        executor.setThreadNamePrefix("ReviewLike Async-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
    }
}

