package org.example.mollyapi.address;

import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.example.mollyapi.address.dto.AddressResponseDto;
import org.example.mollyapi.address.entity.Address;
import org.example.mollyapi.address.repository.AddressRepository;
import org.example.mollyapi.address.service.AddressService;
import org.example.mollyapi.common.exception.CustomException;
import org.example.mollyapi.user.entity.User;
import org.example.mollyapi.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@Slf4j
class AddressServiceTest {

    @Mock
    private AddressRepository addressRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AddressService addressService;

    private User testUser;
    private Address testAddress;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .userId(1L)
                .name("테스트 유저")
                .build();

        testAddress = Address.builder()
                .id(1L)
                .user(testUser)
                .recipient("김김이")
                .recipientCellPhone("010-1234-5678")
                .roadAddress("서울시 강남구")
                .addrDetail("101호")
                .defaultAddr(true)
                .build();

        log.info("== 테스트 데이터 초기화 완료 ==");
        log.info("유저 ID: {}, 유저 이름: {}", testUser.getUserId(), testUser.getName());
        log.info("주소 ID: {}, 받는 사람: {}", testAddress.getId(), testAddress.getRecipient());
    }

    @Test
    void 사용자의_주소목록_조회() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(addressRepository.findByUserOrderByDefaultAddrDesc(testUser)).thenReturn(List.of(testAddress));

        List<Address> addresses = addressService.getUserAddresses(1L);

        assertThat(addresses).isNotEmpty();
        assertThat(addresses.get(0).getRecipient()).isEqualTo("김김이");

        log.info("==주소 목록 조회 테스트 성공==");
    }

    @Test
    void 주소_추가() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(addressRepository.save(any(Address.class))).thenReturn(testAddress);

        AddressResponseDto response = addressService.addAddress(
                1L, "김김이2", "010-1234-5678", "서울시 강남구", "101호", "상세주소", false
        );

        assertThat(response.recipient()).isEqualTo("김김이2");
        log.info("==주소 추가 테스트 성공. 주소 ID: {}==", response.addressId());
    }

    @Test
    void 기본_주소_변경() {
        Address newAddress = Address.builder()
                .id(2L)
                .user(testUser)
                .recipient("고라니")
                .recipientCellPhone("010-9999-8888")
                .roadAddress("서울시 서초구")
                .addrDetail("202호")
                .defaultAddr(false)
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(addressRepository.findById(2L)).thenReturn(Optional.of(newAddress));
        when(addressRepository.findByUserAndDefaultAddr(testUser, true)).thenReturn(Optional.of(testAddress));

        AddressResponseDto response = addressService.changeDefaultAddress(1L, 2L);

        assertThat(response.defaultAddr()).isTrue();
        verify(addressRepository, times(1)).findByUserAndDefaultAddr(testUser, true);
        verify(addressRepository, times(1)).findById(2L);

        log.info("==기본 주소 변경 테스트 성공. 기존 주소 ID: {}, 새로운 기본 주소 ID: {}==", testAddress.getId(), response.addressId());
    }

    @Test
    void 주소_수정() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(addressRepository.findById(1L)).thenReturn(Optional.of(testAddress));

        AddressResponseDto response = addressService.updateAddress(
                1L, 1L, "김김이2", "010-9876-5432", "서울시 서초구", "202호", "변경된 주소", false
        );

        assertThat(response.recipient()).isEqualTo("김김이2");
        assertThat(response.roadAddress()).isEqualTo("서울시 서초구");
        log.info("==주소 수정 테스트 성공. 수정된 주소 ID: {}==", response.addressId());
    }

    @Test
    void 주소_삭제() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(addressRepository.findById(1L)).thenReturn(Optional.of(testAddress));

        // 기본 주소 삭제 시 예외가 발생해야 함
        CustomException exception = assertThrows(CustomException.class, () -> addressService.deleteAddress(1L, 1L));
        assertThat(exception.getMessage()).isEqualTo("기본 주소는 삭제할 수 없습니다.");

        // 기본 주소 삭제 예외 발생 시, delete() 메서드가 호출되지 않아야 함
        verify(addressRepository, never()).delete(any(Address.class));

        log.info("==기본 주소 삭제 예외 테스트 성공==");
    }

    @Test
    void 기본_주소_변경시_주소_없음_예외() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(addressRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> addressService.changeDefaultAddress(1L, 99L));

        log.info("==기본 주소 변경 시 없는 주소 예외 테스트 성공==");
    }
}