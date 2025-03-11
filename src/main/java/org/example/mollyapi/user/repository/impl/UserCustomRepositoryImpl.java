package org.example.mollyapi.user.repository.impl;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.example.mollyapi.user.dto.GetUserInfoResDto;
import org.example.mollyapi.user.dto.GetUserSummaryInfoWithPointResDto;
import org.example.mollyapi.user.repository.UserCustomRepository;

import java.util.Optional;

import static org.example.mollyapi.user.auth.entity.QAuth.auth;
import static org.example.mollyapi.user.entity.QUser.user;

@RequiredArgsConstructor
public class UserCustomRepositoryImpl implements UserCustomRepository {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public Optional<GetUserInfoResDto> getUserInfo(Long userId) {

        if (userId == null){
            return Optional.empty();
        }

        return Optional.ofNullable(jpaQueryFactory.select(
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
                .fetchOne());

    }

    @Override
    public Optional<GetUserSummaryInfoWithPointResDto> getUserSummaryInfo(Long userId) {

        if (userId == null){
            return Optional.empty();
        }

        return Optional.ofNullable(jpaQueryFactory.select(
                        Projections.constructor(GetUserSummaryInfoWithPointResDto.class,
                                user.name,
                                auth.email,
                                user.point
                        )).from(user)
                .innerJoin(auth).on(auth.user.eq(user))
                .where(user.userId.eq(userId))
                .fetchOne());

    }

    @Override
    public void deleteByFlagTrue() {

//        jpaQueryFactory
//                .delete(user)
//                .where(user.flag.eq(true))
//                .execute();

    }
}
