package org.example.mollyapi.address.dto;

import lombok.Builder;
import org.example.mollyapi.address.entity.Address;

@Builder
public record AddressRequestDto(
        String recipient,
        String recipientCellPhone,
        String roadAddress,
        String numberAddress,
        String addrDetail,
        Boolean defaultAddr
) {
    public static AddressRequestDto from(Address address) {
        return AddressRequestDto.builder()
                .recipient(address.getRecipient())
                .recipientCellPhone(address.getRecipientCellPhone())
                .roadAddress(address.getRoadAddress())
                .numberAddress(address.getNumberAddress())
                .addrDetail(address.getAddrDetail())
                .defaultAddr(address.getDefaultAddr())
                .build();
    }
}