package org.example.mollyapi.user.repository;

import org.example.mollyapi.user.dto.GetUserInfoResDto;
import org.example.mollyapi.user.dto.GetUserSummaryInfoWithPointResDto;

import java.util.Optional;

public interface UserCustomRepository {

    Optional<GetUserInfoResDto> getUserInfo(Long authId);

    Optional<GetUserSummaryInfoWithPointResDto>  getUserSummaryInfo(Long authId);

    void deleteByFlagTrue();

}
