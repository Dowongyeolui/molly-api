package org.example.mollyapi.user.auth.dto;

import org.example.mollyapi.user.type.Role;

import java.util.List;

public record SignInResDto(
        String accessToken,
        List<Role> roles
) {
}
