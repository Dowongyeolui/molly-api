package org.example.mollyapi.user.repository;

import org.example.mollyapi.user.dto.GetUserInfoResDto;
import org.example.mollyapi.user.dto.GetUserSummaryInfoWithPointResDto;

public interface UserCustomRepository {

    GetUserInfoResDto getUserInfo(Long authId);

    GetUserSummaryInfoWithPointResDto getUserSummaryInfo(Long authId);

}
