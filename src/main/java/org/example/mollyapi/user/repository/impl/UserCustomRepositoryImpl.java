package org.example.mollyapi.user.repository.impl;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.example.mollyapi.user.dto.GetUserInfoResDto;
import org.example.mollyapi.user.repository.UserCustomRepository;

import static org.example.mollyapi.user.entity.QUser.*;

@RequiredArgsConstructor
public class UserCustomRepositoryImpl implements UserCustomRepository {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public GetUserInfoResDto getUserInfo(Long authId, String email) {

        return jpaQueryFactory.select(
                Projections.constructor(GetUserInfoResDto.class,
                        user.profileImage,
                        user.nickname,
                        user.name,
                        user.birth,
                        user.cellPhone,
                        user.auth.email
                )).from(user)
                .where(user.auth.authId.eq(authId))
                .fetchOne();

    }
}
