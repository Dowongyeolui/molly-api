package org.example.mollyapi.delivery.dto;

import io.swagger.v3.oas.annotations.media.Schema;

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
}
