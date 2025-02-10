package org.example.mollyapi.user.service;

import lombok.RequiredArgsConstructor;
import org.example.mollyapi.common.exception.CustomException;
import org.example.mollyapi.user.dto.GetUserInfoResDto;
import org.example.mollyapi.user.repository.UserRepository;
import org.springframework.stereotype.Service;

import static org.example.mollyapi.common.exception.error.impl.UserError.*;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    /***
     * 사용자 정보 조회1 - 마이페이지 프로필 수정 기능에 필요한 조회 데이터
     * @param authId 인증 PK
     * @return GetUserInfoResDto
     */
    public GetUserInfoResDto getUserInfo(Long authId, String email) {
        boolean exists = userRepository.existsByAuth_AuthId(authId);

        if (!exists) throw new CustomException(NOT_EXISTS_USER);

        return userRepository.getUserInfo(authId, email);
    }
}
