package org.example.mollyapi.address.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.mollyapi.address.dto.AddressResponseDto;
import org.example.mollyapi.address.entity.Address;
import org.example.mollyapi.address.repository.AddressRepository;
import org.example.mollyapi.common.exception.CustomException;
import org.example.mollyapi.common.exception.error.impl.AddressError;
import org.example.mollyapi.common.exception.error.impl.UserError;
import org.example.mollyapi.user.entity.User;
import org.example.mollyapi.user.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AddressService {

    private final AddressRepository addressRepository;
    private final UserRepository userRepository;


    // 사용자의 모든 주소 목록 조회
    public List<Address> getUserAddresses(Long userId) {
        User user = getUserById(userId);
        return addressRepository.findByUserOrderByDefaultAddrDesc(user);
    }


    // 주소 추가
    public AddressResponseDto addAddress(Long userId, String recipient, String recipientCellPhone, String roadAddress, String numberAddress, String addrDetail, boolean defaultAddr) {
        User user = getUserById(userId);

        // 기본 주소로 설정하는 경우 기존 기본 주소 해제
        if (defaultAddr) {
            addressRepository.findByUserAndDefaultAddr(user, true)
                    .ifPresent(existingDefault -> existingDefault.updateDefaultAddr(false));
        }

        Address address = Address.builder()
                .user(user)
                .recipient(recipient)
                .recipientCellPhone(recipientCellPhone)
                .roadAddress(roadAddress)
                .numberAddress(numberAddress)
                .addrDetail(addrDetail)
                .defaultAddr(defaultAddr)
                .build();

        addressRepository.save(address);
        return AddressResponseDto.from(address);
    }


    // 기본 주소 변경
    public AddressResponseDto changeDefaultAddress(Long userId, Long addressId) {
        User user = getUserById(userId);
        Address newDefaultAddress = getAddressById(addressId, user);

        if (newDefaultAddress.getDefaultAddr()) {
            log.info("이미 기본 주소로 설정된 주소입니다. addressId={}", addressId);
            return AddressResponseDto.from(newDefaultAddress);
        }

        // 기존 기본 주소 찾아서 일반 주소로 변경
        addressRepository.findByUserAndDefaultAddr(user, true)
                .ifPresent(existingDefault -> existingDefault.updateDefaultAddr(false));

        // 새 기본 주소 설정
        newDefaultAddress.updateDefaultAddr(true);

        return AddressResponseDto.from(newDefaultAddress);
    }


    // 주소 수정
    public AddressResponseDto updateAddress(Long userId, Long addressId, String recipient, String recipientCellPhone, String roadAddress, String numberAddress, String addrDetail, boolean defaultAddr) {
        User user = getUserById(userId);
        Address address = getAddressById(addressId, user);

        // 기본 주소 변경을 요청한 경우, 기존 기본 주소 해제 후 새 주소를 기본 주소로 설정
        if (defaultAddr && !address.getDefaultAddr()) {
            addressRepository.findByUserAndDefaultAddr(user, true)
                    .ifPresent(existingDefault -> existingDefault.updateDefaultAddr(false));
        }

        address.updateAddress(recipient, recipientCellPhone, roadAddress, numberAddress, addrDetail);
        address.updateDefaultAddr(defaultAddr);
        addressRepository.save(address);

        return AddressResponseDto.from(address);
    }

    // 주소 삭제
    public void deleteAddress(Long userId, Long addressId) {
        User user = getUserById(userId);
        Address address = getAddressById(addressId, user);

        if (address.getDefaultAddr()) {
            log.error("기본 주소 삭제 불가. addressId: {}", addressId);
            throw new CustomException(AddressError.ADDRESS_DELETE_NOT_ALLOWED);
        }

        addressRepository.delete(address);
        log.info("주소 삭제 완료. addressId: {}", addressId);
    }

    private User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(UserError.NOT_EXISTS_USER));
    }

    private Address getAddressById(Long addressId, User user) {
        return addressRepository.findById(addressId)
                .filter(address -> address.getUser().equals(user))
                .orElseThrow(() -> new CustomException(UserError.NOT_EXISTS_USER));
    }
}