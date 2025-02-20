package org.example.mollyapi.delivery.type;

public enum DeliveryStatus {
    READY, // 배송 준비 중(default)
    SHIPPING, // 배송 중
    CANCEL_REQUESTED, // 배송 전 주문 철회 요청됨(배송 진행 못하게 잠금)
    ARRIVED, // 배송 완료
    CANCELED, // 주문 철회로 배송 취소됨
    RETURN_REQUESTED, // 반품 요청됨
    RETURN_ARRIVED, // 반품 도착
    RETURNED // 반품 완료
}