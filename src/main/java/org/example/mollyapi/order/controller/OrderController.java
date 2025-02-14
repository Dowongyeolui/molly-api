package org.example.mollyapi.order.controller;

import lombok.RequiredArgsConstructor;
import org.example.mollyapi.order.dto.OrderCreateRequestDto;
import org.example.mollyapi.order.dto.OrderResponseDto;
import org.example.mollyapi.order.service.OrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;

    @PostMapping(produces = "application/json")
    public ResponseEntity<OrderResponseDto> createOrder(@RequestBody OrderCreateRequestDto request) {
        OrderResponseDto response = orderService.createOrder(request.getUserId(), request.getOrderRequests());
        return ResponseEntity.ok(response);
    }
}