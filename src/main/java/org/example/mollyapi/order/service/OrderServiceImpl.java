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
import org.example.mollyapi.common.exception.error.impl.PaymentError;
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
import org.example.mollyapi.payment.dto.request.PaymentRequestDto;
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
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import static java.lang.Integer.parseInt;
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
        log.info("create Order 실행");
        if (userId == null) {
            throw new IllegalArgumentException("주문을 생성하려면 유효한 사용자 ID가 필요합니다.");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다. userId=" + userId));

//        // 중복 주문 방지 - 같은 사용자에 진행 중인 주문이 있는지 확인
//        if (orderRepository.existsByUserIdAndStatus(userId, OrderStatus.PENDING)) {
//            throw new IllegalArgumentException("현재 진행 중인 주문이 있습니다.");
//        }

        // 주문 요청 데이터 검증
        for (OrderRequestDto req : orderRequests) {
            if (req.cartId() == null && req.itemId() == null && req.quantity() == null) {
                throw new IllegalArgumentException("cartId, itemId, quantity 중 하나는 반드시 포함되어야 합니다.");
            }
        }

        // 결제용 tossOrderId 생성
        String tossOrderId;
        do {
            tossOrderId = generateTossOrderId();
        } while (orderRepository.existsByTossOrderId(tossOrderId));

        // 새로운 주문 생성 (초기 상태는 PENDING)
        Order order = new Order(user, tossOrderId);
        List<OrderDetail> orderDetails = orderRequests.stream()
                .map(req -> createOrderDetail(order, req))
                .collect(Collectors.toList());

        // 주문 상세(OrderDetail) 저장
        orderRepository.save(order); // ❓순서 바뀌어도 되는거임????
        orderDetailRepository.saveAll(orderDetails);
        order.updateTotalAmount(calculateTotalAmount(orderDetails));

        // ❓비교 #1
        AddressResponseDto defaultAddress = addressRepository.findByUserAndDefaultAddr(user, true)
                .map(AddressResponseDto::from)
                .orElse(null);

        // ❓비교 #2
//        Optional<Address> byUserAndDefaultAddr = addressRepository.findByUserAndDefaultAddr(user, true);
//        Address address = byUserAndDefaultAddr.get();
//        AddressResponseDto from = AddressResponseDto.from(address);

        return OrderResponseDto.from(order, orderDetails, user.getPoint(), defaultAddress);
    }

    private String generateTossOrderId() {
        return "ORD-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) + "-" + new Random().nextInt(9000);
    }


    /**
     * 주문 상세 생성 - 장바구니 주문, 바로 주문 구분
     */
    public OrderDetail createOrderDetail(Order order, OrderRequestDto req) {
        Cart cart = null;
        if (req.cartId() != null) {
            cart = cartRepository.findById(req.cartId())
                    .orElseThrow(() -> new IllegalArgumentException("장바구니 항목을 찾을 수 없습니다. cartId=" + req.cartId()));
        }

        Long itemId = (cart != null) ? cart.getProductItem().getId() : req.itemId();
        Long quantity = (cart != null) ? cart.getQuantity() : req.quantity();

        // itemId 및 quantity 검증
        if (itemId == null) {
            throw new IllegalArgumentException("상품 ID가 존재하지 않습니다.");
        }
        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException("주문 수량은 1개 이상이어야 합니다.");
        }

        ProductItem productItem = productItemRepository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("상품이 존재하지 않습니다. itemId=" + itemId));

//        // 중복 주문 방지: 같은 상품을 이미 주문했는지 체크
//        long existingOrderCount = orderRepository.countByUserAndProductItem(order.getUser(), productItem.getId());
//        if (existingOrderCount > 0) {
//            throw new IllegalArgumentException("이미 동일 상품에 대한 주문이 진행 중입니다.");
//        }

        // 재고 조회(차감 X)
        if (productItem.getQuantity() < quantity) {
            throw new IllegalArgumentException("재고가 부족하여 주문할 수 없습니다. itemId=" + itemId);
        }

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
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("해당 주문을 찾을 수 없습니다. orderId=" + orderId));

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

        /// 2-1. 주문 조회
        final String tossOrderIdFinal = tossOrderId; // effectively final 보장
        Order order = orderRepository.findByTossOrderIdWithDetails(tossOrderIdFinal)
                .orElseThrow(() -> new IllegalArgumentException("해당 주문을 찾을 수 없습니다. tossOrderId=" + tossOrderIdFinal));

        /// 2-2. 주문 상태 확인 (재시도 시 필요)
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new IllegalStateException("결제 재시도는 PENDING 상태에서만 가능합니다.");
        }

        /// 2-3. 주문 만료 시간 확인 (시간 초과 시 주문 실패 처리)
        if (order.getExpirationTime().isBefore(LocalDateTime.now())) {
            failOrder(tossOrderId);
            throw new IllegalStateException("결제 가능 시간이 초과되었습니다. 주문을 다시 생성해주세요.");
        }

        /// 3-1. 기존 결제 정보 확인 (주문에 결제는 하나밖에 없음)
        Optional<PaymentInfoResDto> paymentInfoResDto = paymentService.findLatestPayment(order.getId());
        boolean isRetry = paymentInfoResDto.isPresent(); // 기존 결제 내역이 있으면 결제 재시도로 판단
        if (paymentInfoResDto.isPresent() && paymentInfoResDto.get().paymentStatus() == PaymentStatus.APPROVED) {
            throw new IllegalArgumentException("이미 결제된 주문입니다.");
        }

        Delivery delivery = Delivery.from(deliveryInfo, order.getId());
        deliveryRepository.save(delivery);

        order.setDelivery(delivery);
        orderRepository.save(order);

        /// 3-2. 최초 요청이 아닌 경우 (재시도 시) 결제 정보 등 파라미터 재입력 받아 업데이트 가능
        if (isRetry) {
            PaymentInfoResDto latestPaymentInfo = paymentService.findLatestPayment(order.getId())
                    .orElseThrow(() -> new IllegalArgumentException("결제 정보가 없습니다."));

            paymentKey = latestPaymentInfo.paymentKey();
            tossOrderId = latestPaymentInfo.tossOrderId();
            amount = order.getTotalAmount();
            point = String.valueOf(order.getPointUsage());
            paymentType = order.getPaymentType();
            deliveryInfo = order.getDelivery().toDto();
        }

        /// 3-3. 결제 정보 검증 추가
        if (paymentKey == null || paymentKey.trim().isEmpty()) {
            throw new IllegalArgumentException("결제 정보가 누락되었습니다.");
        }

        /// 3-4. 배송 정보 검증 추가
        deliveryInfo.validate();

        /// 3-5. 결제 금액 검증
        validateAmount(order.getTotalAmount(), amount);

        /// 4. 포인트 정보 복호화 및 검증, 차감 (tx1)
        Integer pointUsage = Optional.ofNullable(AESUtil.decryptWithSalt(point))
                .map(Integer::parseInt)
                .orElse(0); // 기본값 0 설정. NumberFormatException 방지
        validateUserPoint(user, pointUsage);
        user.updatePoint(-pointUsage);
        userRepository.save(user);

        /// 5. 배송 정보 생성 후 주문에 연결 (tx1)
//        Delivery delivery = createDelivery(deliveryInfo);
//        delivery.setOrder(order);
//        order.setDelivery(delivery);
        deliveryRepository.save(delivery); // * 서비스로
        orderRepository.save(order);
//        Delivery delivery = Delivery.from(deliveryInfo, order);  // ✅ Order 설정 추가
//        deliveryRepository.save(delivery);

        /// 6. 재고 검증 및 차감, 장바구니 삭제 : 첫 결제 요청일 때만 실핼 (tx2)
        if (!isRetry) {
            validationService.validateBeforePayment(order.getId());
        }

        /// 7. 주문 정보 저장
//        orderRepository.save(order);

        /// 10. PaymentRequestDto 생성 후 PaymentService 호출
        PaymentConfirmReqDto paymentConfirmReqDto = new PaymentConfirmReqDto(
                order.getId(),
                order.getTossOrderId(),
                order.getPaymentId(),
                order.getTotalAmount(),
                order.getPaymentType(),
                order.getPointUsage()
        );

        /// 11. 결제 진행
        Payment payment = paymentService.processPayment(userId, paymentConfirmReqDto);
        log.info("payment = {}", payment);
        // 12. 결제 성공/실패에 따라 나머지 로직 처리
        if (Objects.requireNonNull(payment.getStatus()) == PaymentStatus.APPROVED) {
            log.info("APPROVE 실행");
            order.addPayment(payment);  // 결제 추가
            order.updateStatus(OrderStatus.SUCCEEDED);
            orderRepository.save(order);
        }
        System.out.println("----------------------------------ProcessPayment 트랜잭션 종료----------------------------------");
        return PaymentResDto.from(payment);
    }

    @Transactional
    public PaymentResDto processPaymentTest(Long userId, String paymentKey, String tossOrderId, Long amount, String point, String paymentType, DeliveryReqDto deliveryInfo, String status) {
        System.out.println("----------------------------------ProcessPayment 트랜잭션 시작----------------------------------");

        /// 1. 사용자 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다. userId=" + userId));

        /// 2-1. 주문 조회
        final String tossOrderIdFinal = tossOrderId; // effectively final 보장
        Order order = orderRepository.findByTossOrderIdWithDetails(tossOrderIdFinal)
                .orElseThrow(() -> new IllegalArgumentException("해당 주문을 찾을 수 없습니다. tossOrderId=" + tossOrderIdFinal));

        log.info("status = {}", order.getStatus());
        /// 2-2. 주문 상태 확인 (재시도 시 필요)
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new IllegalStateException("결제 재시도는 PENDING 상태에서만 가능합니다.");
        }

        /// 2-3. 주문 만료 시간 확인 (시간 초과 시 주문 실패 처리)
        if (order.getExpirationTime().isBefore(LocalDateTime.now())) {
            failOrder(tossOrderId);
            throw new IllegalStateException("결제 가능 시간이 초과되었습니다. 주문을 다시 생성해주세요.");
        }

        /// 3. `Delivery` 생성 및 `Order`와 연결
        Delivery delivery = Delivery.from(deliveryInfo, order.getId()); // 🚀 orderId 설정
        deliveryRepository.save(delivery); // orderId 포함한 채 저장됨

        /// 4. `Order`에 `delivery_id` 설정 후 저장
        order.setDelivery(delivery);
        orderRepository.save(order); // delivery_id 포함된 상태로 저장됨

        if (delivery == null) {
            throw new IllegalArgumentException("배송 정보를 생성할 수 없습니다.");
        }

        /// 3-1. 기존 결제 정보 확인 (주문에 결제는 하나밖에 없음)
        Optional<PaymentInfoResDto> paymentInfoResDto = paymentService.findLatestPayment(order.getId());
        boolean isRetry = paymentInfoResDto.isPresent(); // 기존 결제 내역이 있으면 결제 재시도로 판단
//        if (paymentInfoResDto.isPresent() && paymentInfoResDto.get().paymentStatus() == PaymentStatus.APPROVED) {
//            throw new IllegalArgumentException("이미 결제된 주문입니다.");
//        }


        /// 3-2. 최초 요청이 아닌 경우 (재시도 시) 결제 정보 등 파라미터 재입력 받아 업데이트 가능
        if (isRetry) {
            PaymentInfoResDto latestPaymentInfo = paymentService.findLatestPayment(order.getId())
                    .orElseThrow(() -> new IllegalArgumentException("결제 정보가 없습니다."));

            paymentKey = latestPaymentInfo.paymentKey();
            tossOrderId = latestPaymentInfo.tossOrderId();
            amount = order.getTotalAmount();
            point = String.valueOf(order.getPointUsage());
            paymentType = order.getPaymentType();
            deliveryInfo = order.getDelivery().toDto();
        }

        /// 3-3. 결제 정보 검증 추가
        if (paymentKey == null || paymentKey.trim().isEmpty()) {
            throw new IllegalArgumentException("결제 정보가 누락되었습니다.");
        }

        /// 3-4. 배송 정보 검증 추가
        deliveryInfo.validate();

        /// 3-5. 결제 금액 검증
        validateAmount(order.getTotalAmount(), amount);

        /// 4. 포인트 정보 복호화 및 검증, 차감 (tx1)
//        Integer pointUsage = Optional.ofNullable(AESUtil.decryptWithSalt(point))
//                .map(Integer::parseInt)
//                .orElse(0); // 기본값 0 설정. NumberFormatException 방지
        Integer pointUsage = Integer.parseInt(point);
        validateUserPoint(user, pointUsage);
        user.updatePoint(-pointUsage);
        userRepository.save(user);

//        /// 5. 배송 정보 생성 후 주문에 연결 (tx1)
////        Delivery delivery = createDelivery(deliveryInfo);
////        delivery.setOrder(order);
////        order.setDelivery(delivery);
//        orderRepository.save(order);
//        deliveryRepository.save(delivery); // * 서비스로
////        Delivery delivery = Delivery.from(deliveryInfo, order);  // ✅ Order 설정 추가
////        deliveryRepository.save(delivery);

        /// 6. 재고 검증 및 차감, 장바구니 삭제 : 첫 결제 요청일 때만 실핼 (tx2)
        if (!isRetry) {
            validationService.validateBeforePayment(order.getId());
        }

        /// 7. 주문 정보 저장
//        orderRepository.save(order);

        /// 10. PaymentRequestDto 생성 후 PaymentService 호출
        PaymentConfirmReqDto paymentConfirmReqDto = new PaymentConfirmReqDto(
                order.getId(),
                order.getTossOrderId(),
                order.getPaymentId(),
                order.getTotalAmount(),
                order.getPaymentType(),
                order.getPointUsage()
        );

        /// 11. 결제 진행
        Payment payment = paymentService.processPaymentTest(userId, paymentConfirmReqDto, status);
        log.info("payment = {}", payment);
        // 12. 결제 성공/실패에 따라 나머지 로직 처리
        if (Objects.requireNonNull(payment.getStatus()) == PaymentStatus.APPROVED) {
            log.info("APPROVE 실행");
            order.addPayment(payment);  // 결제 추가
            order.updateStatus(OrderStatus.SUCCEEDED);
            orderRepository.save(order);
        }
        log.info("payment paymentKey = {}", payment.getPaymentKey());
        System.out.println("----------------------------------ProcessPayment 트랜잭션 종료----------------------------------");
        return PaymentResDto.from(payment);
    }


    private Delivery createDelivery(DeliveryReqDto deliveryInfo, Long orderId) {
        return Delivery.from(deliveryInfo, orderId);
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
        throw new CustomException(OrderError.PAYMENT_RETRY_REQUIRED); // "결제가 실패했습니다. 다시 시도하시겠습니까? (API: /orders/{orderId}/fail-payment)"
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

        Delivery delivery = order.getDelivery();

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
        Delivery delivery = order.getDelivery();
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
        Delivery delivery = order.getDelivery();
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