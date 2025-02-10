package org.example.mollyapi.user.dto;

public record GetUserSummaryInfoWithPointResDto(
        String name,
        String email,
        int point
) {
}
