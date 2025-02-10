package org.example.mollyapi.user.repository.impl;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.example.mollyapi.user.dto.GetUserInfoResDto;
import org.example.mollyapi.user.dto.GetUserSummaryInfoWithPointResDto;
import org.example.mollyapi.user.repository.UserCustomRepository;

import static org.example.mollyapi.user.entity.QUser.*;

@RequiredArgsConstructor
public class UserCustomRepositoryImpl implements UserCustomRepository {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public GetUserInfoResDto getUserInfo(Long authId) {

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

    @Override
    public GetUserSummaryInfoWithPointResDto getUserSummaryInfo(Long authId) {
        return jpaQueryFactory.select(
                Projections.constructor(GetUserSummaryInfoWithPointResDto.class,
                        user.name,
                        user.auth.email,
                        user.point
                )).from(user)
                .where(user.auth.authId.eq(authId))
                .fetchOne();

    }
}
