package org.example.mollyapi.user.repository.impl;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.example.mollyapi.user.auth.entity.QAuth;
import org.example.mollyapi.user.dto.GetUserInfoResDto;
import org.example.mollyapi.user.dto.GetUserSummaryInfoWithPointResDto;
import org.example.mollyapi.user.repository.UserCustomRepository;

import static org.example.mollyapi.user.auth.entity.QAuth.*;
import static org.example.mollyapi.user.auth.entity.QAuth.auth;
import static org.example.mollyapi.user.entity.QUser.*;

@RequiredArgsConstructor
public class UserCustomRepositoryImpl implements UserCustomRepository {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public GetUserInfoResDto getUserInfo(Long userId) {

        return jpaQueryFactory.select(
                Projections.constructor(GetUserInfoResDto.class,
                        user.profileImage,
                        user.nickname,
                        user.name,
                        user.birth,
                        user.cellPhone,
                        auth.email
                )).from(user)
                .innerJoin(auth).on(auth.user.eq(user))
                .where(user.userId.eq(userId))
                .fetchOne();

    }

    @Override
    public GetUserSummaryInfoWithPointResDto getUserSummaryInfo(Long userId) {


        return jpaQueryFactory.select(
                Projections.constructor(GetUserSummaryInfoWithPointResDto.class,
                        user.name,
                        auth.email,
                        user.point
                )).from(user)
                .innerJoin(auth).on(auth.user.eq(user))
                .where(user.userId.eq(userId))
                .fetchOne();

    }

    @Override
    public void deleteByFlagTrue() {

//        jpaQueryFactory
//                .delete(user)
//                .where(user.flag.eq(true))
//                .execute();

    }
}
