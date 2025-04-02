package org.example.mollyapi.review.event.v4.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.example.mollyapi.common.dto.CommonResDto;
import org.example.mollyapi.common.exception.CustomErrorResponse;
import org.example.mollyapi.review.dto.request.ReviewLikeReqDto;
import org.example.mollyapi.review.event.v4.service.ReviewLikeService_v4;
import org.example.mollyapi.user.auth.annotation.Auth;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "좋아요 Controller V3", description = "리뷰 좋아요 기능을 담당")
@RestController
@RequiredArgsConstructor
public class ReviewLikeController_v4 {
    private final ReviewLikeService_v4 reviewLikeServiceV3;

    @Auth
    @PostMapping("/v4/like")
    @Operation(summary = "좋아요 기능", description = "리뷰에 좋아요를 누를 수 있습니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "좋아요 상태 변경 성공",
                    content = @Content(schema = @Schema(implementation = ReviewLikeReqDto.class))),
            @ApiResponse(responseCode = "400", description = "좋아요 상태 변경 실패",
                    content = @Content(schema = @Schema(implementation = CustomErrorResponse.class)))
    })
    public ResponseEntity<?> addReviewLike(
            @RequestParam Long reviewId,
            HttpServletRequest request
    ) {
        Long userId = (Long) request.getAttribute("userId");
        reviewLikeServiceV3.addReviewLike(reviewId, userId);
        return ResponseEntity.ok(new CommonResDto("좋아요 상태 변경에 성공했습니다."));
    }

    @Auth
    @DeleteMapping("/v4/removelike")
    @Operation(summary = "좋아요 해제 기능", description = "리뷰에 좋아요를 해제할 수 있습니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "좋아요 상태 변경 성공",
                    content = @Content(schema = @Schema(implementation = ReviewLikeReqDto.class))),
            @ApiResponse(responseCode = "400", description = "좋아요 상태 변경 실패",
                    content = @Content(schema = @Schema(implementation = CustomErrorResponse.class)))
    })
    public ResponseEntity<?> removeReviewLike(
            @RequestParam Long reviewId,
            HttpServletRequest request
    ) {
        Long userId = (Long) request.getAttribute("userId");
        reviewLikeServiceV3.removeReviewLike(reviewId, userId);
        return ResponseEntity.ok(new CommonResDto("좋아요 상태 변경에 성공했습니다."));
    }
}
