package org.example.mollyapi.order.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.mollyapi.address.dto.AddressResponseDto;
import org.example.mollyapi.address.entity.Address;
import org.example.mollyapi.address.repository.AddressRepository;
import org.example.mollyapi.cart.entity.Cart;
import org.example.mollyapi.cart.repository.CartRepository;
import org.example.mollyapi.common.exception.CustomException;
import org.example.mollyapi.common.exception.error.impl.OrderError;
import org.example.mollyapi.delivery.dto.DeliveryReqDto;
import org.example.mollyapi.delivery.entity.Delivery;
import org.example.mollyapi.delivery.repository.DeliveryRepository;
import org.example.mollyapi.delivery.type.DeliveryStatus;
import org.example.mollyapi.order.dto.*;
import org.example.mollyapi.order.entity.*;
import org.example.mollyapi.order.repository.OrderDetailRepository;
import org.example.mollyapi.order.repository.OrderRepository;
import org.example.mollyapi.order.type.CancelStatus;
import org.example.mollyapi.order.type.OrderStatus;
import org.example.mollyapi.payment.dto.request.PaymentConfirmReqDto;
import org.example.mollyapi.payment.dto.response.PaymentInfoResDto;
import org.example.mollyapi.payment.dto.response.PaymentResDto;
import org.example.mollyapi.payment.entity.Payment;
import org.example.mollyapi.payment.repository.PaymentRepository;
import org.example.mollyapi.payment.service.PaymentService;
import org.example.mollyapi.payment.type.PaymentStatus;
import org.example.mollyapi.payment.util.AESUtil;
import org.example.mollyapi.product.entity.ProductItem;
import org.example.mollyapi.product.repository.ProductItemRepository;
import org.example.mollyapi.review.repository.ReviewRepository;
import org.example.mollyapi.user.entity.User;
import org.example.mollyapi.user.repository.UserRepository;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import static org.example.mollyapi.common.exception.error.impl.OrderError.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
//@Transactional
public class OrderServiceImpl implements OrderService{
    private final OrderRepository orderRepository;
    private final OrderDetailRepository orderDetailRepository;
    private final ProductItemRepository productItemRepository;
    private final UserRepository userRepository;
    private final PaymentRepository paymentRepository;
    private final DeliveryRepository deliveryRepository;
    private final AddressRepository addressRepository;
    private final ReviewRepository reviewRepository;
    private final CartRepository cartRepository;
    private final PaymentService paymentService;
    private final OrderStockService validationService;


    /**
     * 사용자의 주문 내역 조회 (GET /orders/{userId})
     */
    public OrderHistoryResponseDto getUserOrders(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다. userId=" + userId));

        List<Order> orders = orderRepository.findOrdersByUserAndStatusIn(
                user, List.of(OrderStatus.SUCCEEDED, OrderStatus.WITHDRAW)
        );

        return new OrderHistoryResponseDto(userId, orders, paymentRepository, reviewRepository);
    }


    /**
     * 주문 상세 조회 (GET /orders/{orderId})
     */
    public OrderResponseDto getOrderDetails(Long orderId) {
        // 주문 조회
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("해당 주문을 찾을 수 없습니다. orderId=" + orderId));

        // 기본 배송지 조회
        AddressResponseDto defaultAddress = addressRepository.findByUserAndDefaultAddr(order.getUser(), true)
                .map(AddressResponseDto::from)
                .orElse(null);

        // 주문 상세 응답 반환
        return OrderResponseDto.from(order, order.getOrderDetails(), order.getUser().getPoint(), defaultAddress);
    }

    //--------------------------------------------------------------------//

    /**
     * 주문 생성
     */
    @Transactional
    public OrderResponseDto createOrder(Long userId, List<OrderRequestDto> orderRequests) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다. userId=" + userId));

        // 결제용 주문 ID 생성
        String tossOrderId = generateTossOrderId();

        // 새로운 주문 생성 (초기 상태는 PENDING)
//        Order order = Order.builder()
//                .user(user)
//                .tossOrderId(tossOrderId)
//                .totalAmount(0L)
//                .status(OrderStatus.PENDING)
//                .cancelStatus(CancelStatus.NONE)
//                .expirationTime(LocalDateTime.now().plusMinutes(10))
//                .build();
        Order order = new Order(user, tossOrderId);

        List<OrderDetail> orderDetails = orderRequests.stream()
                .map(req -> createOrderDetail(order, req))
                .collect(Collectors.toList());

        // 주문 상세(OrderDetail) 저장
        orderDetailRepository.saveAll(orderDetails);
        order.updateTotalAmount(calculateTotalAmount(orderDetails));
        orderRepository.save(order);

        // 비교 #1
        AddressResponseDto defaultAddress = addressRepository.findByUserAndDefaultAddr(user, true)
                .map(AddressResponseDto::from)
                .orElse(null);

        // 비교 #2
        Optional<Address> byUserAndDefaultAddr = addressRepository.findByUserAndDefaultAddr(user, true);
        Address address = byUserAndDefaultAddr.get();
        AddressResponseDto from = AddressResponseDto.from(address);

        return OrderResponseDto.from(order, orderDetails, user.getPoint(), defaultAddress);
    }

    private String generateTossOrderId() {
        return "ORD-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) + "-" + new Random().nextInt(9000);
    }

    /**
     * 주문 상세 생성 - 장바구니 주문, 바로 주문 구분
     */
    private OrderDetail createOrderDetail(Order order, OrderRequestDto req) {
        Cart cart = (req.cartId() != null) ?
                cartRepository.findById(req.cartId())
                        .orElseThrow(() -> new IllegalArgumentException("장바구니 항목을 찾을 수 없습니다. cartId=" + req.cartId()))
                : null;

        Long itemId = (cart != null) ? cart.getProductItem().getId() : req.itemId();
        Long quantity = (cart != null) ? cart.getQuantity() : req.quantity();

        ProductItem productItem = productItemRepository.findProductItemById(itemId);

        // 재고 조회(차감 X)
        if (productItem.getQuantity() < quantity) {
            throw new IllegalArgumentException("재고가 부족하여 주문할 수 없습니다. itemId=" + itemId);
        }

//        return OrderDetail.builder()
//                .order(order)
//                .productItem(productItem)
//                .size(productItem.getSize())
//                .price(productItem.getProduct().getPrice())
//                .quantity(quantity)
//                .brandName(productItem.getProduct().getBrandName())
//                .productName(productItem.getProduct().getProductName())
//                .cartId(req.getCartId())
//                .build();
        return new OrderDetail( // create method
                order,
                productItem,
                productItem.getSize(),
                productItem.getProduct().getPrice(),
                quantity,
                productItem.getProduct().getBrandName(),
                productItem.getProduct().getProductName(),
                req.cartId()
        );
    }

    /**
     * 주문 취소: 결제 요청 전 주문을 취소하는 경우(API)
     */
    public String cancelOrder(Long orderId) {
        // 주문 조회
        Order order = orderRepository.findOrderById(orderId);

        // 주문 상태가 PENDING이 아닐 경우 취소 불가
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new IllegalStateException("결제 요청이 진행된 주문은 취소할 수 없습니다.");
        }

        // 주문 및 주문 상세 삭제 (Cascade로 OrderDetail도 삭제됨)
        orderRepository.delete(order);

        // 클라이언트에 응답 메시지 반환
        return "주문이 취소되었습니다.";
    }


    /**
     * 주문 시간 초과로 자동 취소 처리 -> 배치 작업 예정
     */
    @Transactional
    public void expireOrder(Long orderId) {
        // 주문 조회
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("해당 주문을 찾을 수 없습니다. orderId=" + orderId));

        // 주문 상태가 PENDING이 아닐 경우 만료 처리 불가
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new IllegalStateException("이미 결제 요청이 진행된 주문은 만료될 수 없습니다.");
        }

        log.info("주문 시간이 초과되어 자동 취소로 주문 삭제를 진행합니다. orderId={}, 사용자ID={}", orderId, order.getUser().getUserId());

        // 주문 삭제 (Cascade로 OrderDetail도 같이 삭제됨)
        orderRepository.delete(order);
    }


    /**
     * 결제 요청
     */
    @Transactional
    public PaymentResDto processPayment(Long userId, String paymentKey, String tossOrderId, Long amount, String point, String paymentType, DeliveryReqDto deliveryInfo) {
        System.out.println("----------------------------------ProcessPayment 트랜잭션 시작----------------------------------");

        /// 1. 사용자 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다. userId=" + userId));

        /// 2. 주문 조회
        Order order = orderRepository.findByTossOrderIdWithDetails(tossOrderId)
                .orElseThrow(() -> new IllegalArgumentException("해당 주문을 찾을 수 없습니다. tossOrderId=" + tossOrderId));

        // 3. 결제 금액 검증
        validateAmount(order.getTotalAmount(), amount);

        // 4. 포인트 정보 복호화 및 검증, 차감 (tx1)
        Integer pointUsage = Integer.parseInt(AESUtil.decryptWithSalt(point));
        validateUserPoint(user, pointUsage);
        user.updatePoint(-pointUsage);
        userRepository.save(user);

        // 5. 배송 정보 생성 후 주문에 연결 (tx1)
        Delivery delivery = createDelivery(deliveryInfo);
        order.setDelivery(delivery);
        deliveryRepository.save(delivery); // * 서비스로

        // 6. 해당 주문에 이미 결제가      있는지 확인 ( 주문에 결제는 하나밖에 없음 )
        Optional<PaymentInfoResDto> paymentInfoResDto = paymentService.findLatestPayment(order.getId());

        // 7. 재고 검증 및 차감 (tx2)
        paymentInfoResDto.ifPresentOrElse(
                payment -> log.info("최근 결제 내역 존재: {}", payment),
                () -> validationService.validateBeforePayment(order.getId())
        );
//        validationService.validateBeforePayment(user, order, point, deliveryInfo);

        // 8. 장바구니에서 주문한 상품 삭제
        for (OrderDetail orderDetail : order.getOrderDetails()) {
            cartRepository.deleteById(orderDetail.getCartId()); // +) 예외 추가 - cart가 존재하지 않을 경우
        }
        // 9. 주문 정보 저장
        orderRepository.save(order);

        // 10. PaymentRequestDto 생성 후 PaymentService 호출
        PaymentConfirmReqDto paymentConfirmReqDto = new PaymentConfirmReqDto(
                order.getId(),
                order.getTossOrderId(),
                order.getPaymentId(),
                order.getTotalAmount(),
                order.getPaymentType(),
                order.getPointUsage()
        );

        // 11. 결제 진행
        /* internal server error일 경우 -> 자동 재시도
            다른 에러일 경우 수동 재시도 로직 (주문 저장, 결제만 재시도)
        */
        Payment payment = paymentService.processPayment(userId, paymentConfirmReqDto);


        // 12. 결제 성공/실패에 따라 나머지 로직 처리
        switch (payment.getStatus()) {
            case APPROVED -> {
                order.addPayment(payment);  // 새로운 결제 추가
                order.updatePaymentInfo(); // 최신 결제 정보 업데이트
                order.updateStatus(OrderStatus.SUCCEEDED);
                orderRepository.save(order);
            }
            case PENDING -> handlePaymentFailure(payment, tossOrderId, "결제 실패");
            case FAILED -> throw new CustomException(PAYMENT_RETRY_REQUIRED);
        }
        System.out.println("----------------------------------ProcessPayment 트랜잭션 종료----------------------------------");
        return PaymentResDto.from(payment);
    }


    private Delivery createDelivery(DeliveryReqDto deliveryInfo) {
        return Delivery.from(deliveryInfo);
    }

    /**
     * 결제 실패 - 결제 자동 재시도
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
//    @Transactional
    public void handlePaymentFailure(Payment payment, String tossOrderId, String failureReason) {
        System.out.println("----------------------------------재시도 트랜잭션 시작----------------------------------");
        log.error("결제 실패 - 주문 트랜잭션 유지, 결제만 롤백 진행: tossOrderId={}, failureReason={}", tossOrderId, failureReason);

        // 주문 조회
        Order order = orderRepository.findByTossOrderId(tossOrderId)
                .orElseThrow(() -> new IllegalArgumentException("해당 주문을 찾을 수 없습니다. tossOrderId=" + tossOrderId));

        // 결제 자동 재시도 3회 실행
        // @retryable or 쓰레드 슬립
        for (int i = 1; i <= 3; i++) {
            Payment retriedPayment = paymentService.retryPayment(payment.getUser().getUserId(), tossOrderId, payment.getPaymentKey());

            if (retriedPayment.getStatus() == PaymentStatus.APPROVED) {
                log.info("결제 재시도 성공: tossOrderId={}", tossOrderId);

                // 결제 성공 시 주문 업데이트
                order.addPayment(retriedPayment);
//                order.updatePaymentInfo();
                order.updateStatus(OrderStatus.SUCCEEDED);
                orderRepository.save(order);
                System.out.println("----------------------------------재시도 트랜잭션 종료----------------------------------");
                return;
            }
            log.warn("결제 재시도 실패 {}/3: tossOrderId={}", i, tossOrderId);
        }

        // 자동 재시도 3회 실패 시 주문 상태 PENDING 유지. 사용자가 수동 재시도 가능
        log.error("결제 재시도 3회 실패 - 주문을 기존 상태로 유지: tossOrderId={}", tossOrderId);

        // 사용자에게 재시도 여부를 물음
        throw new CustomException(OrderError.PAYMENT_RETRY_REQUIRED); // "결제가 실패했습니다. 다시 시도하시겠습니까? (API: /orders/{orderId}/retry-payment)"
    }

    /**
     * 결제 재시도 (사용자 API를 받아 수동 재시도)
     */
    @Transactional
    public void retryPayment(Long userId, Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("해당 주문을 찾을 수 없습니다. orderId=" + orderId));

        // 주문 상태 확인
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new IllegalStateException("결제 재시도는 PENDING 상태에서만 가능합니다.");
        }

        // 주문 만료 시간 확인 (시간 초과 시 주문 실패 처리)
        if (order.getExpirationTime().isBefore(LocalDateTime.now())) {
            failOrder(order.getTossOrderId()); // 재시도 횟수 초과 시 주문 취소
            throw new IllegalStateException("결제 가능 시간이 초과되었습니다. 주문을 다시 생성해주세요.");
        }

        // 기존 결제 정보 확인 (paymentService 코드 사용으로 리팩토링)
        Payment latestPayment = paymentRepository.findLatestPaymentByOrderId(orderId, PageRequest.of(0, 1))
                .stream().findFirst()
                .orElseThrow(() -> new IllegalArgumentException("결제 정보가 없습니다. orderId=" + orderId));

        // 결제 재시도 진행
        Payment retriedPayment = paymentService.retryPayment(userId, order.getTossOrderId(), latestPayment.getPaymentKey());

        if (retriedPayment.getStatus() == PaymentStatus.APPROVED) {
            order.addPayment(retriedPayment);
//            order.updatePaymentInfo();
            order.updateStatus(OrderStatus.SUCCEEDED);
            orderRepository.save(order);
        } else {
            throw new IllegalStateException("결제 재시도에 실패했습니다.");
        }
    }

    /**
     * 주문 실패 처리 - 주문 상태 변경, 사용포인트 & 재고 & 장바구니 복구, 배송 삭제, 주문 데이터 삭제
     */
    @Transactional
    public void failOrder(String tossOrderId) {
        log.error("주문 실패 처리 시작: tossOrderId={}", tossOrderId);
        // 1. 주문 조회
        Order order = orderRepository.findByTossOrderId(tossOrderId)
                .orElseThrow(() -> new IllegalArgumentException("해당 주문을 찾을 수 없습니다. tossOrderId=" + tossOrderId));

        // 2. 주문 상태 변경 (실패)
        order.updateStatus(OrderStatus.FAILED);

        // 3. 사용 포인트 복구
        refundUserPoints(order);

        // 4. 재고 복구
        restoreStock(order.getOrderDetails());

        // 5. 배송 정보 삭제
        if (order.getDelivery() != null) {
            deliveryRepository.delete(order.getDelivery());
            log.info("배송 정보 삭제 완료: tossOrderId={}", tossOrderId);
        }

        // 6. 장바구니 복구 (주문 상세에서 cartId가 있는 항목을 다시 장바구니로 추가)
        restoreCart(order.getOrderDetails());

        // 7. 주문 삭제 (Cascade로 OrderDetail도 같이 삭제됨)
        orderRepository.delete(order);
        log.info("주문 실패 처리 완료: tossOrderId={}", tossOrderId);
    }

    //--------------------------------------------------------------------//

    /**
     * 주문 철회 요청 (철회 요청 -> 즉시 환불 실행)
     */
    @Transactional
    public void withdrawOrder(Long orderId) {
        Order order = orderRepository.findOrderById(orderId);
        validateOrderWithdrawal(order);

        Delivery delivery = deliveryRepository.findByOrderId(orderId)
                .orElseThrow(() -> new IllegalArgumentException("배송 정보를 찾을 수 없습니다. orderId=" + orderId));

        // 배송준비중 → 즉시 환불 요청
        if (delivery.getStatus() == DeliveryStatus.READY) {
            delivery.setStatus(DeliveryStatus.CANCEL_REQUESTED);
            order.updateCancelStatus(CancelStatus.REQUESTED);
        }
        // 배송완료 → 반품 요청 (환불은 반품 완료 후 진행)
        else if (delivery.getStatus() == DeliveryStatus.ARRIVED) {
            delivery.setStatus(DeliveryStatus.RETURN_REQUESTED);
            order.updateCancelStatus(CancelStatus.REQUESTED);
            log.info("반품 요청 완료 - orderId={}", order.getId());
            return; // 여기서 주문 철회 프로세스를 멈춤 (반품 도착 API가 호출되면 이어서 진행)
        }
        else {
            throw new IllegalStateException("현재 상태에서 주문 철회가 불가능합니다.");
        }

        orderRepository.save(order);
        deliveryRepository.save(delivery);

        // 배송준비중 → 환불 즉시 실행
        processRefund(order);
    }

    /**
     * (배송완료 →)반품 도착 후 자동 환불 진행
     */
    @Transactional
    public void handleReturnArrived(Long orderId) {
        Order order = orderRepository.findOrderById(orderId);

        // 주문이 반품 요청된 상태가 맞는지 확인
        if (order.getCancelStatus() != CancelStatus.REQUESTED) {
            throw new IllegalStateException("현재 상태에서 반품 처리가 불가능합니다.");
        }
//        // 주문이 이미 환불 요청이 됐고 && 이미 환불에 실패한 상태인지 확인 - 다시 반품 프로세스를 진행하지 않도록 방지
//        if (order.getCancelStatus() == CancelStatus.FAILED) {
//            throw new IllegalStateException("이 주문은 이미 환불 실패 상태입니다. 관리자에게 문의하세요.");
//        }

        log.info("반품 도착 확인 - 자동 환불 진행: orderId={}", orderId);

        // 환불 진행
        processRefund(order);
    }

    /**
     * 환불 프로세스 (실패하면 자동 재시도)
     */
    @Transactional
    public void processRefund(Order order) {
        boolean refundSuccess = refundUserPoints(order);

        if (!refundSuccess) {
            log.warn("환불 실패 - 자동 재시도 진행: orderId={}", order.getId());
            retryRefund(order.getId(), 3);  // 최대 3회 자동 재시도
        } else {
            finalizeOrderWithdrawal(order);
        }
    }

    /**
     * 자동 환불 재시도
     */
    @Transactional
    public void retryRefund(Long orderId, int retryCount) {
        for (int i = 1; i <= retryCount; i++) {
            boolean refundSuccess = refundUserPoints(orderRepository.findOrderById(orderId));
            if (refundSuccess) {
                finalizeOrderWithdrawal(orderRepository.findOrderById(orderId));
                return;
            }
            log.warn("환불 재시도 실패 {}/{}: orderId={}", i, retryCount, orderId);
        }

        // 최대 재시도 횟수 초과 시 철회 실패 처리
        Order order = orderRepository.findOrderById(orderId);
        order.updateCancelStatus(CancelStatus.FAILED);
        orderRepository.save(order);
        log.error("환불 실패 - 수동 처리 필요: orderId={}", orderId);
    }

    /**
     * 철회 완료 처리 (환불 성공 후 실행)
     */
    private void finalizeOrderWithdrawal(Order order) {
        Delivery delivery = deliveryRepository.findByOrderId(order.getId())
                .orElseThrow(() -> new IllegalArgumentException("배송 정보를 찾을 수 없습니다. orderId=" + order.getId()));

        // 업데이트 쿼리로 변경, 리포지토리에서 쿼리 날리는걸로 수정
        order.updateCancelStatus(CancelStatus.COMPLETED);
        order.updateStatus(OrderStatus.WITHDRAW);
        orderRepository.save(order);

        // 업데이트 쿼리로 변경
        delivery.setStatus(DeliveryStatus.RETURNED);
        deliveryRepository.save(delivery);

        // 재고 복구
        restoreStock(order.getOrderDetails());
        log.info("주문 철회 성공 - orderId={}", order.getId());
    }

    //-----------------------------유틸--------------------------------//

    /**
     * 주문 철회 가능 여부 검증
     */
    private void validateOrderWithdrawal(Order order) {
        // 1. 주문 상태 검증: 주문이 성공 상태여야 철회 가능
        if (order.getStatus() != OrderStatus.SUCCEEDED) {
            throw new IllegalStateException("철회 요청이 불가능한 주문 상태입니다. (orderId=" + order.getId() + ")");
        }

        // 2. 기존 철회 요청 여부 확인
        if (order.getCancelStatus() != CancelStatus.NONE) {
            throw new IllegalStateException("이미 철회 요청이 진행된 주문입니다. (orderId=" + order.getId() + ")");
        }

        // 3. 배송 상태 검증: READY(배송 준비중) 또는 ARRIVED(배송 완료) 상태만 철회 가능
        Delivery delivery = deliveryRepository.findByOrderId(order.getId())
                .orElseThrow(() -> new IllegalArgumentException("배송 정보를 찾을 수 없습니다. orderId=" + order.getId()));

        if (delivery.getStatus() != DeliveryStatus.READY && delivery.getStatus() != DeliveryStatus.ARRIVED) {
            throw new IllegalStateException("현재 배송 상태에서 철회 요청이 불가능합니다. (orderId=" + order.getId() + ", deliveryStatus=" + delivery.getStatus() + ")");
        }

        log.info("주문 철회 요청 검증 완료: orderId={}, deliveryStatus={}", order.getId(), delivery.getStatus());
    }

    /**
     * 포인트 환불 (실패 시 false 반환)
     */
    private boolean refundUserPoints(Order order) {
        try {
            User user = order.getUser();
            int refundPoints = (order.getPointUsage() != null ? order.getPointUsage() : 0) -
                    (order.getPointSave() != null ? order.getPointSave() : 0);
            user.updatePoint(refundPoints);
            userRepository.save(user);
            log.info("포인트 환불 완료 - 사용자 ID={}, 환불 포인트={}", user.getUserId(), refundPoints);
            return true;
        } catch (DataAccessException e) {
            log.error("포인트 환불 실패 - DB 오류 발생: orderId={}, error={}", order.getId(), e.getMessage());
            throw new CustomException(ORDER_WITHDRAW_REFUND_FAIL);
        } catch (Exception e) {
            log.error("포인트 환불 실패: orderId={}, error={}", order.getId(), e.getMessage());
            return false;
        }
    }

    private void validateAmount(Long orderAmount, Long amount) {
        if (!Objects.equals(orderAmount, amount)) {
            throw new IllegalArgumentException("결제 금액이 일치하지 않습니다.");
        }
    }

    private void validateUserPoint(User user, Integer requiredPoint) {
        if (user.getPoint() < requiredPoint) {
            throw new IllegalArgumentException("사용자 포인트가 부족합니다.");
        }
    }

    /**
     * 재고 복구
     */
    private void restoreStock(List<OrderDetail> orderDetails) {
        for (OrderDetail detail : orderDetails) {
            ProductItem productItem = detail.getProductItem();
            if (productItem != null) {
                log.info("[Before] 재고 복구 전 - 상품 ID: {}, 기존 재고: {}, 주문 수량={}",
                        productItem.getId(), productItem.getQuantity(), detail.getQuantity());

                productItem.restoreStock(detail.getQuantity());
                productItemRepository.save(productItem);
                log.info("[After] 재고 복구 완료 - 상품 ID: {}, 실행 후 재고={}",
                        productItem.getId(), productItem.getQuantity());
            } else {
                log.warn("ProductItem이 null입니다. OrderDetail ID={}", detail.getId());
            }
        }
    }

    /**
     * 장바구니 복구
     */
    private void restoreCart(List<OrderDetail> orderDetails) {
        for (OrderDetail detail : orderDetails) {
            if (detail.getCartId() != null) {
                Cart cart = Cart.builder()
                        .user(detail.getOrder().getUser())
                        .productItem(detail.getProductItem())
                        .quantity(detail.getQuantity())
                        .build();
                cartRepository.save(cart);
                log.info("장바구니 복구 완료 - productId={}, quantity={}", detail.getProductItem().getId(), detail.getQuantity());
            }
        }
    }

    private long calculateTotalAmount(List<OrderDetail> orderDetails) {
        return orderDetails.stream()
                .mapToLong(d -> d.getPrice() * d.getQuantity())
                .sum();
    }
}