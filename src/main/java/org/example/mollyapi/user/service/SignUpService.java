package org.example.mollyapi.user.service;

import lombok.RequiredArgsConstructor;
import org.example.mollyapi.common.exception.CustomException;
import org.example.mollyapi.common.exception.error.impl.AuthError;
import org.example.mollyapi.user.config.PasswordEncoder;
import org.example.mollyapi.user.dto.SignUpReqDto;
import org.example.mollyapi.user.entity.Auth;
import org.example.mollyapi.user.entity.Password;
import org.example.mollyapi.user.entity.User;
import org.example.mollyapi.user.repository.AuthRepository;
import org.example.mollyapi.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static org.example.mollyapi.common.exception.error.impl.AuthError.ALREADY_EXISTS;

@Service
@RequiredArgsConstructor
public class SignUpService {

    private final UserRepository userRepository;
    private final AuthRepository authRepository;
    private final PasswordEncoder passwordEncoder;


    @Transactional
    public void signUp(SignUpReqDto signUpReqDto) {

        boolean exists = authRepository.existsByEmail(signUpReqDto.email());

        if(exists) throw new CustomException(ALREADY_EXISTS);

        Password password = passwordEncoder.encrypt(signUpReqDto.email(), signUpReqDto.password());

        User savedUser = userRepository.save(signUpReqDto.toUser());
        authRepository.save(signUpReqDto.toAuth(savedUser, password));
    }
}
