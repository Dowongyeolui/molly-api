package org.example.mollyapi.review.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.example.mollyapi.common.exception.CustomErrorResponse;
import org.example.mollyapi.review.dto.response.TrendingReviewResDto;
import org.example.mollyapi.review.service.TrendingReviewService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "리뷰 인기순위 Controller", description = "최근 7일 간 리뷰 인기순위 Top 18 조회")
@RestController
@RequestMapping("/trending")
@RequiredArgsConstructor
public class TrendingReviewController {
    private final TrendingReviewService trendingReviewService;

    @GetMapping()
    @Operation(summary = "리뷰 인기순위 조회 API", description = "최근 7일 간 리뷰의 좋아요 인기순위를 조회 할 수 있습니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "리뷰 인기순위 조회 성공",
                    content = @Content(schema = @Schema(implementation = TrendingReviewResDto.class))),
            @ApiResponse(responseCode = "204", description = "리뷰 조회 불가",
                    content = @Content(schema = @Schema(implementation = CustomErrorResponse.class)))
    })
    public ResponseEntity<?> getTrendingReview() {
        return trendingReviewService.getTrendingReview();
    }
}
