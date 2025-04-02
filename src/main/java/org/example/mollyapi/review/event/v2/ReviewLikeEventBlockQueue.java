package org.example.mollyapi.review.event.v2;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.mollyapi.review.event.v2.event.LikeCountEvent;
import org.example.mollyapi.review.event.v2.handler.LikeCountEventListener;
import org.springframework.stereotype.Component;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReviewLikeEventBlockQueue {
    private final BlockingQueue<LikeCountEvent> eventQueue = new LinkedBlockingQueue<>();
    private Thread likecountThread;
    private volatile boolean running = true;
    private final LikeCountEventListener likeCountEventListener;

    @PostConstruct
    public void startLikeCount() {
        likecountThread = new Thread(() -> {
            while (running) {
                try {
                    log.info("LikeCount thread started={}", Thread.currentThread().getName());
                    LikeCountEvent event = eventQueue.take(); // 큐에서 이벤트를 순서대로 가져옴
                    likeCountEventListener.handleReviewIncreaseLikeCountEvent(event);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });
        likecountThread.start();
    }

    @PreDestroy
    public void stopLikeCount() {
        running = false;
        likecountThread.interrupt();
    }

    public void publishEvent(Object event) {
        eventQueue.offer((LikeCountEvent) event);
    }
}
