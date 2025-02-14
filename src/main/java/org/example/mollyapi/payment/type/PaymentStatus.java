package org.example.mollyapi.payment.type;

import java.util.Arrays;

public enum PaymentStatus {
    PENDING("결제대기"),      // 결제 대기 중
    APPROVED("결제승인"),     // 결제 승인됨
    FAILED("결제실패"),       // 결제 실패
    CANCELED("결제취소"),     // 결제 취소됨
    REFUNDED("환불완료")      // 환불 완료
    ;
    private final String status;

    PaymentStatus(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }

    public static PaymentStatus from(String status) {
        return Arrays.stream(values())
                .filter(paymentStatus -> paymentStatus.getStatus().equals(status))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown status " + status));

    }

    public boolean isPending() {
        return this == PENDING;
    }

    public boolean hasApproved() {
        return this == APPROVED;
    }

    public boolean hasFailed() {
        return this == FAILED;
    }

    public boolean hasCanceled() {
        return this == CANCELED;
    }

    public boolean isRefunded() {
        return this == REFUNDED;
    }
}
