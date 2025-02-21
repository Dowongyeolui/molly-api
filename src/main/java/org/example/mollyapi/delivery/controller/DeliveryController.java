package org.example.mollyapi.delivery.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.mollyapi.delivery.service.DeliveryService;
import org.example.mollyapi.delivery.type.DeliveryStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/delivery")
@Slf4j
public class DeliveryController {

    private final DeliveryService deliveryService;

    // 출고 처리 API (READY → SHIPPING)
    @PostMapping("/{orderId}/ship")
    public ResponseEntity<String> shipOrder(@PathVariable Long orderId) {
        deliveryService.updateDeliveryStatus(orderId, DeliveryStatus.SHIPPING);
        return ResponseEntity.ok("출고 처리 완료 (READY → SHIPPING)");
    }

    // 배송 완료 API (SHIPPING → ARRIVED)
    @PostMapping("/{orderId}/arrive")
    public ResponseEntity<String> arriveOrder(@PathVariable Long orderId) {
        deliveryService.updateDeliveryStatus(orderId, DeliveryStatus.ARRIVED);
        return ResponseEntity.ok("배송 완료 (SHIPPING → ARRIVED)");
    }

    // 반품 도착 API (RETURN_REQUESTED → RETURN_ARRIVED)
    @PostMapping("/{orderId}/return-arrive")
    public ResponseEntity<String> returnArrive(@PathVariable Long orderId) {
        deliveryService.updateDeliveryStatus(orderId, DeliveryStatus.RETURN_ARRIVED);
        return ResponseEntity.ok("🔄 반품 도착 처리 완료 (RETURN_REQUESTED → RETURN_ARRIVED)");
    }
}