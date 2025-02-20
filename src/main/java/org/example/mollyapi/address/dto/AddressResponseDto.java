package org.example.mollyapi.address.dto;

import lombok.Builder;
import org.example.mollyapi.address.entity.Address;

@Builder
public record AddressResponseDto(
        Long addressId,
        String recipient,
        String recipientCellPhone,
        String roadAddress,
        String numberAddress,
        String addrDetail,
        Boolean defaultAddr,
        Long userId,
        String userName
) {
    public static AddressResponseDto from(Address address) {
        return AddressResponseDto.builder()
                .addressId(address.getId())
                .recipient(address.getRecipient())
                .recipientCellPhone(address.getRecipientCellPhone())
                .roadAddress(address.getRoadAddress())
                .numberAddress(address.getNumberAddress())
                .addrDetail(address.getAddrDetail())
                .defaultAddr(address.getDefaultAddr())
                .userId(address.getUser().getUserId())
                .userName(address.getUser().getName())
                .build();
    }
}