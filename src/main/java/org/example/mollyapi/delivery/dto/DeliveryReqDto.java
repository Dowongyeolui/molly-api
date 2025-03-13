package org.example.mollyapi.delivery.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.example.mollyapi.delivery.entity.Delivery;

import java.util.Objects;

public record DeliveryReqDto(
        @Schema(description = "착신자명", example = "momo")
        String receiver_name,
        @Schema(description = "착신자 번호", example = "010-5134-1111")
        String receiver_phone,
        @Schema(description = "도로명주소", example = "판교판교")
        String road_address,
        @Schema(description = "지번", example = "12345")
        String number_address,
        @Schema(description = "배송 세부사항", example = "배송 조심히 해주세요")
        String addr_detail
) {
        // Delivery 엔티티를 기반으로 DeliveryReqDto를 생성하는 정적 메서드 추가
        public static DeliveryReqDto from(Delivery delivery) {
                return new DeliveryReqDto(
                        delivery.getReceiverName(),
                        delivery.getReceiverPhone(),
                        delivery.getRoadAddress(),
                        delivery.getNumberAddress(),
                        delivery.getAddrDetail()
                );
        }

        // 배송 정보 검증
        public void validate() {
                if (Objects.isNull(receiver_name) || receiver_name.trim().isEmpty() ||
                        Objects.isNull(receiver_phone) || receiver_phone.trim().isEmpty() ||
                        Objects.isNull(road_address) || road_address.trim().isEmpty() ||
                        Objects.isNull(number_address) || number_address.trim().isEmpty() ||
                        Objects.isNull(addr_detail) || addr_detail.trim().isEmpty()) {
                        throw new IllegalArgumentException("배송 정보가 누락되었습니다.");
                }
        }
}