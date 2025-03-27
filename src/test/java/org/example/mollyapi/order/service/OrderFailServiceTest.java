package org.example.mollyapi.order.service;

public class OrderFailServiceTest {

    //    @Test
//    @DisplayName("최종 결제 실패 시 주문 실패 처리")
//    void processPayment_Failed_ShouldFailOrder() {
//        // given
//        testOrder.updateStatus(OrderStatus.PENDING);
//        orderRepository.save(testOrder);
//
//        Payment failedPayment = new Payment("test-key", PaymentStatus.FAILED);
//        paymentRepository.save(failedPayment);
//
//        // when
//        orderService.failOrder(testOrder.getTossOrderId());
//
//        // then
//        Order failedOrder = orderRepository.findByTossOrderId(testOrder.getTossOrderId())
//                .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다."));
//
//        assertThat(failedOrder.getStatus()).isEqualTo(OrderStatus.FAILED);
//    }
}
