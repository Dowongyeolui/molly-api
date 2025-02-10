package org.example.mollyapi.user.repository;

import org.example.mollyapi.user.dto.GetUserInfoResDto;

public interface UserCustomRepository {

    GetUserInfoResDto getUserInfo(Long authId, String email);

}
