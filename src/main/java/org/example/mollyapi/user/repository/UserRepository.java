package org.example.mollyapi.user.repository;

import org.example.mollyapi.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long>, UserCustomRepository {

    boolean existsByNickname(String nickName);

}
