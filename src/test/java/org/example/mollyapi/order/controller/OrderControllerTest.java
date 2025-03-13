//package org.example.mollyapi.order.controller;
//
//import org.example.mollyapi.order.dto.OrderRequestDto;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//
//import java.util.List;
//
//import static org.assertj.core.api.Assertions.assertThatThrownBy;
//import static org.junit.jupiter.api.Assertions.*;
//
//class OrderControllerTest {
//
//    @Test
//    @DisplayName("유효하지 않은 사용자로 주문을 요청하면 예외가 발생한다")
//    void createOrderWithInvalidUser_ShouldThrowException() {
//        /// given
//        Long invalidUserId = -1L;
//        OrderRequestDto orderReq = OrderRequestDto.builder()
//                .cartId(null)
//                .itemId(savedProductItem.getId())
//                .quantity(2L)
//                .build();
//
//        /// when & then
//        assertThatThrownBy(() -> orderService.createOrder(invalidUserId, List.of(orderReq)))
//                .isInstanceOf(IllegalArgumentException.class)
//                .hasMessageContaining("사용자를 찾을 수 없습니다. userId=" + invalidUserId);
//    }
//
//    @Test
//    @DisplayName("userId 없이 주문을 요청하면 예외가 발생한다")
//    void createOrderWithoutUserId_ShouldThrowException() {
//        /// given
//        OrderRequestDto orderReq = OrderRequestDto.builder()
//                .cartId(null)
//                .itemId(savedProductItem.getId())
//                .quantity(2L)
//                .build();
//
//        /// when & then
//        assertThatThrownBy(() -> orderService.createOrder(null, List.of(orderReq)))
//                .isInstanceOf(IllegalArgumentException.class)
//                .hasMessageContaining("사용자 정보가 없습니다.");
//    }
//
//    @Test
//    @DisplayName("유효하지 않은 상품 ID로 주문 요청 시 예외 발생")
//    void createOrderWithInvalidProductId_ShouldThrowException() {
//        /// given
//        Long invalidProductId = -1L;
//        OrderRequestDto orderReq = OrderRequestDto.builder()
//                .cartId(null)
//                .itemId(invalidProductId)
//                .quantity(2L)
//                .build();
//
//        /// when & then
//        assertThatThrownBy(() -> orderService.createOrder(savedUser.getUserId(), List.of(orderReq)))
//                .isInstanceOf(IllegalArgumentException.class)
//                .hasMessageContaining("상품을 찾을 수 없습니다.");
//    }
//
//    @Test
//    @DisplayName("기본 배송지가 없을 때 예외 발생")
//    void createOrderWithoutDefaultAddress_ShouldThrowException() {
//        /// given
//        savedUser.setDefaultAddress(null);
//        userRepository.save(savedUser);
//
//        OrderRequestDto orderReq = OrderRequestDto.builder()
//                .cartId(null)
//                .itemId(savedProductItem.getId())
//                .quantity(2L)
//                .build();
//
//        /// when & then
//        assertThatThrownBy(() -> orderService.createOrder(savedUser.getUserId(), List.of(orderReq)))
//                .isInstanceOf(IllegalArgumentException.class)
//                .hasMessageContaining("기본 배송지 검증 필요");
//    }
//
//}