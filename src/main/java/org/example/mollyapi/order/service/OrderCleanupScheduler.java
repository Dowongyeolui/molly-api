package org.example.mollyapi.order.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;


@Slf4j
@Component
@RequiredArgsConstructor
public class OrderCleanupScheduler {

    private final OrderSchedulerService orderSchedulerService;

    // 매일 오후 2시 28분에 ExpiredOrders 삭제
    @Scheduled(cron = "0 28 14 * * *")
    public void cleanUpExpiredOrders() {
        log.info("[Scheduler] 시작: 만료된 주문 삭제 시작");
        int deletedCount = orderSchedulerService.deleteExpiredOrders();
        log.info("[Scheduler] 완료: {}개의 만료된 주문이 삭제됨", deletedCount);
    }
}
