package org.example.mollyapi.user.repository;

import org.example.mollyapi.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long>, UserCustomRepository {

    boolean existsByNickname(String nickName);

    boolean existsByAuth_AuthId(Long authId);

}
