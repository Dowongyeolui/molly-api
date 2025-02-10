package org.example.mollyapi.user.service;

import lombok.RequiredArgsConstructor;
import org.example.mollyapi.common.exception.CustomException;
import org.example.mollyapi.user.dto.GetUserInfoResDto;
import org.example.mollyapi.user.dto.GetUserSummaryInfoResDto;
import org.example.mollyapi.user.dto.GetUserSummaryInfoWithPointResDto;
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
    public GetUserInfoResDto getUserInfo(Long authId) {
        validUser(authId);

        return userRepository.getUserInfo(authId);
    }


    /***
     * 사용자 이메일, 포인트, 이름 조회
     * @param authId 인증 PK
     * @return GetUserSummaryInfoWithPointResDto
     */
    public GetUserSummaryInfoWithPointResDto getUserSummaryWithPoint(Long authId) {
        validUser(authId);

        return userRepository.getUserSummaryInfo(authId);
    }

    public GetUserSummaryInfoResDto getUserSummaryInfo(Long authId){
        validUser(authId);

        GetUserSummaryInfoWithPointResDto userSummaryInfo
                = userRepository.getUserSummaryInfo(authId);
        return new GetUserSummaryInfoResDto(userSummaryInfo.name(), userSummaryInfo.email());
    }

    /***
     * 사용자 유효성 검증
     * @param authId 인증 PK
     */
    private void validUser(Long authId) {
        boolean exists = userRepository.existsByAuth_AuthId(authId);

        if (!exists) throw new CustomException(NOT_EXISTS_USER);
    }
}
