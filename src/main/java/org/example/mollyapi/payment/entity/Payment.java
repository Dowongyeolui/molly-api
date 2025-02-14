package org.example.mollyapi.payment.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.mollyapi.common.entity.Base;
import org.example.mollyapi.common.exception.CustomException;
import org.example.mollyapi.common.exception.error.impl.PaymentError;
import org.example.mollyapi.order.entity.Order;
import org.example.mollyapi.payment.type.PaymentStatus;
import org.example.mollyapi.user.entity.User;

import java.time.LocalDateTime;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Entity
public class Payment extends Base {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String paymentType;

    @Column
    private Long amount;

    @Column
    private String paymentKey;

    @Column
    private String tossOrderId;

    @Column
    private LocalDateTime paymentDate;

    @Column
    private String failureReason;

    @Column
    private Integer point;

    @Column
    @Enumerated(EnumType.STRING)
    private PaymentStatus paymentStatus;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;


    // 생성자 팩토리 메서드
    public static Payment from(User user, Order order, String tossOrderId, String paymentKey, String paymentType, Long amount, String paymentStatus) {
        return Payment.builder()
                .user(user)
                .order(order)
                .tossOrderId(tossOrderId)
                .paymentKey(paymentKey)
                .paymentType(paymentType)
                .amount(amount)
                .paymentStatus(PaymentStatus.from(paymentStatus))
                .build();
    }

//    public static Payment ready(Long userId,String tossOrderId, Integer amount) {
//        return Payment.builder()
//                .userId(userId)
//                .tossOrderId(tossOrderId)
//                .amount(amount)
//                .paymentStatus(PaymentStatus.PENDING)
//                .build();
//    }





    // 결제 성공 시 필드 추가 도메인 로직
    public void successPayment(Integer point) {
        if (this.paymentStatus != PaymentStatus.PENDING) {
            throw new CustomException(PaymentError.PAYMENT_ALREADY_PROCESSED);
        }
        this.point = point;
        this.paymentStatus = PaymentStatus.APPROVED;
        this.paymentDate = LocalDateTime.now();
    }

//
    public void failPayment(String failureReason) {
        if (this.paymentStatus != PaymentStatus.PENDING) {
            throw new CustomException(PaymentError.PAYMENT_ALREADY_PROCESSED);
        }
        this.paymentStatus = PaymentStatus.FAILED;
        this.failureReason = failureReason;
    }

    public void cancelPayment() {
        if (this.paymentStatus != PaymentStatus.APPROVED) {
            throw new CustomException(PaymentError.PAYMENT_ALREADY_CANCELED);
        }
        this.paymentStatus = PaymentStatus.CANCELED;
    }





}
