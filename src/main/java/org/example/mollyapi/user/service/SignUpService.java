package org.example.mollyapi.user.service;

import lombok.RequiredArgsConstructor;
import org.example.mollyapi.common.client.ImageClientImpl;
import org.example.mollyapi.common.exception.CustomException;
import org.example.mollyapi.user.auth.config.PasswordEncoder;
import org.example.mollyapi.user.auth.entity.Password;
import org.example.mollyapi.user.auth.repository.AuthRepository;
import org.example.mollyapi.user.dto.SignUpReqDto;
import org.example.mollyapi.user.entity.User;
import org.example.mollyapi.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static org.example.mollyapi.common.exception.error.impl.AuthError.ALREADY_EXISTS_AUTH;
import static org.example.mollyapi.common.exception.error.impl.UserError.ALREADY_EXISTS_NICKNAME;

@Service
@RequiredArgsConstructor
public class SignUpService {

    private final UserRepository userRepository;
    private final AuthRepository authRepository;
    private final PasswordEncoder passwordEncoder;

    /***
     * 회원가입
     * @param signUpReqDto 사용자 정보
     */
    @Transactional
    public void signUp(SignUpReqDto signUpReqDto) {

        boolean existsByEmail = authRepository.existsByEmail(signUpReqDto.email());
        boolean existsByNickname = userRepository.existsByNickname(signUpReqDto.nickname());

        if (existsByEmail) throw new CustomException(ALREADY_EXISTS_AUTH);
        if (existsByNickname) throw new CustomException(ALREADY_EXISTS_NICKNAME);

        byte[] salt = passwordEncoder.getSalt();
        String encryptedPassword = passwordEncoder.encrypt(signUpReqDto.password(), salt);

        User savedUser = userRepository.save(signUpReqDto.toUser());
        authRepository.save(signUpReqDto.toAuth(Password.createPassword(encryptedPassword, salt), savedUser));
    }
}
