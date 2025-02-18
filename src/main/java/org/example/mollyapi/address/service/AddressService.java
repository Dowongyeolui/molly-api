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
import java.util.Optional;

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

        if (defaultAddr) {
            Optional<Address> findByUserAndDefaultAddr = addressRepository.findByUserAndDefaultAddr(user, defaultAddr);
            if (findByUserAndDefaultAddr.isEmpty()) {
                throw new IllegalArgumentException("기본 배송지는 한개만 등록 가능합니다.");
            }
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

        // 기존 기본 주소 찾아서 일반 주소로 변경
        addressRepository.findByUserAndDefaultAddr(user, true)
                .ifPresent(existingDefault -> existingDefault.updateDefaultAddr(false));

        // 새 기본 주소 설정
        newDefaultAddress.updateDefaultAddr(true);

        return AddressResponseDto.from(newDefaultAddress);
    }


    // 주소 수정
    public AddressResponseDto updateAddress(Long userId, Long addressId, String recipient, String recipientCellPhone, String roadAddress, String numberAddress, String addrDetail, boolean defaultAddr) {
        log.info("userId = {}", userId);
        User user = getUserById(userId);
        Address address = getAddressById(addressId, user);

        address.updateAddress(recipient, recipientCellPhone, roadAddress, numberAddress, addrDetail);
        address.updateDefaultAddr(defaultAddr);
        addressRepository.save(address);

        return AddressResponseDto.from(address);
    }

    // 주소 삭제
    public void deleteAddress(Long userId, Long addressId) {
        User user = getUserById(userId);
        Address address = getAddressById(addressId, user);

        try {
            if (address.getDefaultAddr()) {
                log.error("기본 주소 삭제 불가. addressId: {}", addressId);
                throw new CustomException(AddressError.ADDRESS_DELETE_NOT_ALLOWED);
            }

            addressRepository.delete(address);
            log.info("주소 삭제 완료. addressId: {}", addressId);
        } catch (CustomException e) {
            log.error("주소 삭제 실패. {}", e.getMessage());
            throw e;
        }
    }

    private User getUserById(Long userId) {
        log.info("userId = {}", userId);
        return userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(UserError.NOT_EXISTS_USER));
    }

    private Address getAddressById(Long addressId, User user) {
        log.info("addressId = {}, user ={}", addressId, user.getUserId());
        return addressRepository.findById(addressId)
                .filter(address -> address.getUser().equals(user))
                .orElseThrow(() -> new CustomException(UserError.NOT_EXISTS_USER));
    }
}