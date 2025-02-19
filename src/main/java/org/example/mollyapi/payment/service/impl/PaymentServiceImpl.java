package org.example.mollyapi.payment.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.Response;
import org.example.mollyapi.address.dto.AddressRequestDto;
import org.example.mollyapi.common.config.WebClientUtil;
import org.example.mollyapi.common.exception.CustomException;
import org.example.mollyapi.common.exception.error.impl.PaymentError;
import org.example.mollyapi.common.exception.error.impl.UserError;
import org.example.mollyapi.delivery.dto.DeliveryReqDto;
import org.example.mollyapi.delivery.entity.Delivery;
import org.example.mollyapi.delivery.repository.DeliveryRepository;
import org.example.mollyapi.order.entity.Order;
import org.example.mollyapi.order.entity.OrderDetail;
import org.example.mollyapi.order.repository.OrderRepository;
import org.example.mollyapi.order.type.OrderStatus;
import org.example.mollyapi.payment.dto.request.PaymentCancelReqDto;
import org.example.mollyapi.payment.dto.request.TossCancelReqDto;
import org.example.mollyapi.payment.dto.response.PaymentInfoResDto;
import org.example.mollyapi.payment.dto.response.PaymentResDto;
import org.example.mollyapi.payment.dto.response.TossCancelResDto;
import org.example.mollyapi.payment.dto.request.TossConfirmReqDto;
import org.example.mollyapi.payment.dto.response.TossConfirmResDto;
import org.example.mollyapi.payment.entity.Payment;
import org.example.mollyapi.payment.repository.PaymentRepository;
import org.example.mollyapi.payment.service.PaymentService;
import org.example.mollyapi.payment.util.MapperUtil;
import org.example.mollyapi.payment.util.PaymentWebClientUtil;
import org.example.mollyapi.product.entity.ProductItem;
import org.example.mollyapi.product.repository.ProductItemRepository;
import org.example.mollyapi.user.dto.GetUserSummaryInfoWithPointResDto;
import org.example.mollyapi.user.entity.User;
import org.example.mollyapi.user.repository.UserRepository;
import org.example.mollyapi.user.service.UserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.lang.Math.toIntExact;

@Service
@Slf4j
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final DeliveryRepository deliveryRepository;
    private final ProductItemRepository productItemRepository;
    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final PaymentWebClientUtil paymentWebClientUtil;
    private final UserService userService;
    private final UserRepository userRepository;


    @Value("${secret.payment-api-key}")
    private String apiKey;

    /*
        결제 로직
     */
    @Transactional
    public Payment processPayment(Long userId, String paymentKey, String tossOrderId, Long amount, Integer point, String paymentType, DeliveryReqDto deliveryInfo) {
        /* 1. find order with tossOrderId
         2. validate amount
         3. success/failure logic
         3-1 if failure -> throw exception
         4. create payment
         5. toss api
         6. success/failure logic
        */

        // user find
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(UserError.NOT_EXISTS_USER));

        // order findByTossOrderId
        Order order = orderRepository.findByTossOrderId(tossOrderId)
                .orElseThrow(() -> new CustomException(PaymentError.ORDER_NOT_FOUND));
        Long orderAmount = order.getTotalAmount();

        // 유저 포인트 검증
        validateUserPoint(userId, point);

        // 결제정보 검증
        validateAmount(orderAmount, amount);

        // payment API
        TossConfirmReqDto tossConfirmReqDto = new TossConfirmReqDto(paymentKey, tossOrderId, amount);
        ResponseEntity<TossConfirmResDto> response = tossPaymentApi(tossConfirmReqDto, apiKey);

        // response 정합성 검사
        boolean res = validateResponse(response);

        // api 응답 tossResDto로 추출
        TossConfirmResDto tossResDto = response.getBody();

        // create pending payment
        Payment payment = Payment.from(user, order, tossOrderId, paymentKey, paymentType, amount, "결제대기");

        // deliveryInfoJson 변형
        String deliveryInfoJson = MapperUtil.convertDtoToJson(deliveryInfo);

        // 결제 성공 및 실패 로직
        if (res) {
            successPayment(payment, tossOrderId, point, deliveryInfoJson);
        } else {
            failPayment(payment, tossOrderId, "실패");
        }
        paymentRepository.save(payment);
        return payment;
    }

    /*
        결제 취소
     */
    public boolean cancelPayment(Long userId, PaymentCancelReqDto paymentCancelReqDto) {

        //Payment 있는지 확인
        Payment payment = findPaymentByPaymentKey(paymentCancelReqDto.paymentKey());

        //Toss request 객체로 변환
        TossCancelReqDto tossCancelReqDto = new TossCancelReqDto(paymentCancelReqDto.cancelReason(), paymentCancelReqDto.cancelAmount());

        //tossApi 호출
        ResponseEntity<TossCancelResDto> response = tossPaymentCancelApi(tossCancelReqDto, paymentCancelReqDto.paymentKey());

        // response 정합성 검사
        boolean res = validateResponse(response);

        // body 추출
        TossCancelResDto tossResDto = response.getBody();

        // 취소 성공 로직 (payment 상태 canceled 로 변경)
        if (res) {
            payment.cancelPayment();
        }

        // 성공 여부 리턴
        return res;
    }

    @Override
    public Payment findPaymentByPaymentKey(String paymentKey) {
        return paymentRepository.findByPaymentKey(paymentKey)
                .orElseThrow(() -> new CustomException(PaymentError.PAYMENT_NOT_FOUND));
    }

    @Override
    public PaymentInfoResDto findLatestPayment(Long orderId) {
        Pageable pageable = PageRequest.of(0, 1); // 첫 번째 결과만 가져옴 (LIMIT 1 효과)
        List<Payment> payments = paymentRepository.findLatestPaymentByOrderId(orderId, pageable);
        return payments.stream()
                .findFirst()
                .map(PaymentInfoResDto::from)
                .orElseThrow(() -> new CustomException(PaymentError.PAYMENT_NOT_FOUND));
    }

    @Override
    public List<PaymentInfoResDto> findAllPayments(Long orderId) {
        List<Payment> payments = paymentRepository.findAllByOrderByCreatedAtDesc()
                .orElseThrow(() -> new CustomException(PaymentError.PAYMENT_NOT_FOUND));
        return payments.stream()
                .map(PaymentInfoResDto::from)
                .collect(Collectors.toList());
    }

    @Override
    public List<PaymentInfoResDto> findUserPayments(Long userId) {
        List<Payment> payments = paymentRepository.findAllByUserId(userId)
                .orElseThrow(() -> new CustomException(PaymentError.PAYMENT_NOT_FOUND));
        return payments.stream()
                .map(PaymentInfoResDto::from)
                .collect(Collectors.toList());
    }


    /*
        결제 성공 - 주문 업데이트 (포인트, 상태), 포인트 차감
     */
//    public void successPayment(Payment payment, String tossOrderId, Integer point) {
//        //payment status change
//        payment.successPayment(point);
//        //order success (field update, point usage)
//        orderService.successOrder(tossOrderId,payment.getPaymentKey(),payment.getPaymentType(),payment.getAmount(),point);
//
//    }
    public void successPayment(Payment payment, String tossOrderId, Integer point, String deliveryInfoJson) {
        //payment status change
        payment.successPayment(point);
        //order success (field update, point usage)
        successOrder(
                tossOrderId,
                payment.getPaymentKey(),
                payment.getPaymentType(),
                payment.getAmount(),
                point,
                deliveryInfoJson // 추가된 deliveryInfoJson 전달
        );
    }

    /*
        결제 실패 - 주문 업데이트
     */
    public void failPayment(Payment payment, String tossOrderId, String failureReason) {
        payment.failPayment(failureReason);
        failOrder(tossOrderId);
        throw new CustomException(PaymentError.PAYMENT_FAILED);
    }


    /*
        confirm tossApi 호출
     */
    private ResponseEntity<TossConfirmResDto> tossPaymentApi(TossConfirmReqDto tossConfirmReqDto, String apiKey) {

        TossConfirmResDto tossConfirmResDto = paymentWebClientUtil.confirmPayment(tossConfirmReqDto, apiKey);
        return ResponseEntity.ok(tossConfirmResDto);
    }

    /*
        cancel tossApi 호출
     */
    private ResponseEntity<TossCancelResDto> tossPaymentCancelApi(TossCancelReqDto tossCancelReqDto, String paymentKey) {
        TossCancelResDto tossCancelResDto = paymentWebClientUtil.cancelPayment(tossCancelReqDto, apiKey, paymentKey);
        return ResponseEntity.ok(tossCancelResDto);
    }

    /*
        payment 생성
     */
    public Payment createPayment(Long userId, Long orderId, String tossOrderId, String paymentKey, String paymentType, Long amount) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(UserError.NOT_EXISTS_USER));
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new CustomException(PaymentError.ORDER_NOT_FOUND));

        Payment payment = Payment.from(user, order, tossOrderId, paymentKey, paymentType, amount, "결제대기");
        paymentRepository.save(payment);

        return payment;
    }

    /*
        HTTP 응답 검증
     */
    private <T> boolean validateResponse(ResponseEntity<T> response) {
        return response.getStatusCode().is2xxSuccessful() && response.getBody() != null;
    }

    /*
        결제 금액 검증
     */
    private void validateAmount(Long orderAmount, Long amount) {
        if (!Objects.equals(amount, orderAmount)) {
            throw new CustomException(PaymentError.PAYMENT_AMOUNT_MISMATCH);
        }
    }

    /*
        포인트 검증
     */
    private void validateUserPoint(Long userId, Integer requiredPoint) {
        GetUserSummaryInfoWithPointResDto userDto = userService.getUserSummaryWithPoint(userId);

        Integer availablePoint = userDto.point();

        if (requiredPoint > availablePoint) {
            throw new CustomException(PaymentError.PAYMENT_POINT_INSUFFICIENT);
        }
    }

    public void successOrder(String tossOrderId, String paymentId, String paymentType, Long paymentAmount, Integer pointUsage, String deliveryInfoJson) {
        // 주문 찾기
        Order order = orderRepository.findByTossOrderId(tossOrderId)
                .orElseThrow(() -> new IllegalArgumentException("해당 주문을 찾을 수 없습니다. tossOrderId=" + tossOrderId));

        // 주문 상태 변경
        order.setStatus(OrderStatus.SUCCEEDED);

        // 사용자의 포인트 차감
        User user = order.getUser();
        if (pointUsage != null && pointUsage > 0) {
            if (user.getPoint() < pointUsage) {
                throw new IllegalArgumentException("사용자 포인트가 부족합니다.");
            }
            user.updatePoint(-pointUsage); // 포인트 차감

            userRepository.save(user);
        }

        // 결제 정보 업데이트
        order.updatePaymentInfo(paymentId, paymentType, paymentAmount, pointUsage);

        // 배송 정보 생성
        createDelivery(order, deliveryInfoJson);

        orderRepository.save(order);
    }

    private void createDelivery(Order order, String deliveryInfoJson) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode deliveryInfo = objectMapper.readTree(deliveryInfoJson);

            String receiverName = deliveryInfo.get("receiver_name").asText();
            String receiverPhone = deliveryInfo.get("receiver_phone").asText();
            String roadAddress = deliveryInfo.get("road_address").asText();
            String numberAddress = deliveryInfo.has("number_address") ? deliveryInfo.get("number_address").asText() : null;
            String addrDetail = deliveryInfo.get("addr_detail").asText();

            // 배송 정보 생성
            Delivery delivery = Delivery.from(order, receiverName, receiverPhone, roadAddress, numberAddress, addrDetail);

            // 배송 정보 저장
            deliveryRepository.save(delivery);

            // Order와 연결
            order.setDelivery(delivery);

            log.info("배송 생성 완료: 주문번호={}, 배송번호={}", order.getId(), delivery.getId());

        } catch (Exception e) {
            log.error("배송 정보 파싱 실패: {}", e.getMessage());
            throw new RuntimeException("배송 정보를 저장할 수 없습니다.");
        }
    }

    public void failOrder(String tossOrderId) {
        Order order = orderRepository.findByTossOrderId(tossOrderId)
                .orElseThrow(() -> new IllegalArgumentException("해당 주문을 찾을 수 없습니다. tossOrderId=" + tossOrderId));

        order.setStatus(OrderStatus.FAILED);

        // 재고 복구
        for (OrderDetail detail : order.getOrderDetails()) {
            ProductItem productItem = detail.getProductItem();
            if (productItem != null) {
                log.info("[Before] 재고 복구 전 - 상품 ID: {}, 기존 재고: {}, 주문 수량: {}",
                        productItem.getId(), productItem.getQuantity(), detail.getQuantity());

                productItem.restoreStock(detail.getQuantity()); // 재고 복구
                productItemRepository.save(productItem);
                productItemRepository.flush();

                log.info("[After] 재고 복구 완료 - 상품 ID: {}, 실행 후 재고: {}",
                        productItem.getId(), productItem.getQuantity());
            } else {
                log.warn("ProductItem이 null입니다. OrderDetail ID: {}", detail.getId());
            }
        }


        // 주문 데이터 삭제 (Cascade로 OrderDetail도 삭제됨)
        orderRepository.delete(order);
    }

}