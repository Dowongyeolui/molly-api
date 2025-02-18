package org.example.mollyapi.address.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.mollyapi.address.dto.AddressRequestDto;
import org.example.mollyapi.address.dto.AddressResponseDto;
import org.example.mollyapi.address.service.AddressService;
import org.example.mollyapi.user.auth.annotation.Auth;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/addresses")
public class AddressController {

    private final AddressService addressService;

    @Auth
    @GetMapping
    @Operation(summary = "사용자의 주소 목록 조회")
    public ResponseEntity<List<AddressResponseDto>> getUserAddresses(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        List<AddressResponseDto> addresses = addressService.getUserAddresses(userId)
                .stream()
                .map(AddressResponseDto::from)
                .toList();
        return ResponseEntity.ok(addresses);
    }

    @Auth
    @PostMapping
    @Operation(summary = "주소 추가")
    public ResponseEntity<AddressResponseDto> addAddress(
            HttpServletRequest request,
            @Valid @RequestBody AddressRequestDto requestAddress) {
        Long userId = (Long) request.getAttribute("userId");

        AddressResponseDto address = addressService.addAddress(
                userId,
                requestAddress.recipient(),
                requestAddress.recipientCellPhone(),
                requestAddress.roadAddress(),
                requestAddress.numberAddress(),
                requestAddress.addrDetail(),
                requestAddress.defaultAddr()
        );

        return ResponseEntity.ok(address);
    }

    @Auth
    @PutMapping("/{addressId}")
    @Operation(summary = "주소 수정")
    public ResponseEntity<AddressResponseDto> updateAddress(
            HttpServletRequest request,
            @PathVariable Long addressId,
            @Valid @RequestBody AddressRequestDto requestAddress) {
        Long userId = (Long) request.getAttribute("userId");

        AddressResponseDto address = addressService.updateAddress(
                userId,
                addressId,
                requestAddress.recipient(),
                requestAddress.recipientCellPhone(),
                requestAddress.roadAddress(),
                requestAddress.numberAddress(),
                requestAddress.addrDetail(),
                requestAddress.defaultAddr()
        );

        return ResponseEntity.ok(address);
    }

    @Auth
    @DeleteMapping("/{addressId}")
    @Operation(summary = "주소 삭제")
    public ResponseEntity<Void> deleteAddress(HttpServletRequest request, @PathVariable Long addressId) {
        Long userId = (Long) request.getAttribute("userId");
        addressService.deleteAddress(userId, addressId);
        return ResponseEntity.noContent().build();
    }

    @Auth
    @PatchMapping("/{addressId}/default")
    @Operation(summary = "기본 주소 변경")
    public ResponseEntity<AddressResponseDto> changeDefaultAddress(
            HttpServletRequest request,
            @PathVariable Long addressId) {

        Long userId = (Long) request.getAttribute("userId");

        AddressResponseDto address = addressService.changeDefaultAddress(userId, addressId);

        return ResponseEntity.ok(address);
    }
}