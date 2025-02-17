package org.example.mollyapi.review;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.mollyapi.common.dto.CommonResDto;
import org.example.mollyapi.common.exception.CustomErrorResponse;
import org.example.mollyapi.review.dto.request.AddReviewReqDto;
import org.example.mollyapi.review.service.ReviewService;
import org.example.mollyapi.user.auth.annotation.Auth;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Slf4j
@Tag(name = "Review Controller", description = "리뷰 기능을 담당")
@RestController
@RequestMapping("/review")
@RequiredArgsConstructor
public class ReviewController {
    private final ReviewService reviewService;

    @Auth
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "리뷰 작성 기능", description = "배송완료된 상품의 리뷰를 작성할 수 있습니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "리뷰 등록 성공",
                    content = @Content(schema = @Schema(implementation = CommonResDto.class))),
            @ApiResponse(responseCode = "400", description = "1. 존재 하지 않는 사용자 \t\n 2. 주문 상세 조회 불가",
                    content = @Content(schema = @Schema(implementation = CustomErrorResponse.class)))
    })
    public ResponseEntity<?> addReview(
            @Valid @RequestPart("review") AddReviewReqDto addReviewReqDto,
            @RequestPart(value = "reviewImages", required = false) List<MultipartFile> uploadImages,
            HttpServletRequest request){
        Long userId = (Long) request.getAttribute("userId");
        reviewService.addReview(addReviewReqDto, uploadImages, userId);
        return ResponseEntity.status(HttpStatusCode.valueOf(204)).body(
                new CommonResDto("리뷰 등록에 성공했습니다."));
    }
}
