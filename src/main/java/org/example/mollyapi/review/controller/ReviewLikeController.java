package org.example.mollyapi.review.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.mollyapi.common.exception.CustomErrorResponse;
import org.example.mollyapi.review.dto.request.UpdateReviewLikeReqDto;
import org.example.mollyapi.review.service.ReviewLikeService;
import org.example.mollyapi.user.auth.annotation.Auth;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "좋아요 Controller", description = "리뷰 좋아요 기능을 담당")
@RestController
@RequestMapping("/like")
@RequiredArgsConstructor
public class ReviewLikeController {
    private final ReviewLikeService reviewLikeService;

    @Auth
    @PostMapping()
    @Operation(summary = "좋아요 기능", description = "리뷰에 좋아요를 누를 수 있습니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "좋아요 상태 변경 성공",
                    content = @Content(schema = @Schema(implementation = UpdateReviewLikeReqDto.class))),
            @ApiResponse(responseCode = "204", description = "변경사항 없음",
                    content = @Content(schema = @Schema(implementation = CustomErrorResponse.class))),
            @ApiResponse(responseCode = "400", description = "좋아요 상태 변경 실패",
                    content = @Content(schema = @Schema(implementation = CustomErrorResponse.class)))
    })
    public ResponseEntity<?> addReviewLike(
        @Valid @RequestBody UpdateReviewLikeReqDto updateReviewLikeReqDto,
        HttpServletRequest request
    ) {
        Long userId = (Long) request.getAttribute("userId");
        return reviewLikeService.changeReviewLike(updateReviewLikeReqDto, userId);
    }
}
