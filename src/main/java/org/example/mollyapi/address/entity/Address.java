package org.example.mollyapi.address.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.mollyapi.user.entity.User;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Table(name = "address")
public class Address {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "address_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "recipient", nullable = false)
    private String recipient;

    @Column(name = "recipient_cell_phone", nullable = false)
    private String recipientCellPhone;

    @Column(name = "road_address", nullable = false)
    private String roadAddress;

    @Column(name = "number_address")
    private String numberAddress;

    @Column(name = "addr_detail", nullable = false)
    private String addrDetail;

    @Column(name = "default_addr", nullable = false)
    private Boolean defaultAddr;

    // 기본 주소 변경
    public void updateDefaultAddr(boolean isDefault) {
        this.defaultAddr = isDefault;
    }

    // 주소 수정
    public void updateAddress(String recipient, String recipientCellPhone, String roadAddress, String numberAddress, String addrDetail) {
        this.recipient = recipient;
        this.recipientCellPhone = recipientCellPhone;
        this.roadAddress = roadAddress;
        this.numberAddress = numberAddress;
        this.addrDetail = addrDetail;
    }
}