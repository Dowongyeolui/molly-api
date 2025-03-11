package org.example.mollyapi.review.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.example.mollyapi.review.dto.request.UpdateReviewLikeReqDto;
import org.example.mollyapi.review.service.ReviewLikeService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ReviewLikeController.class)
public class ReviewLikeControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ReviewLikeService reviewLikeService;

    @Mock
    private HttpServletRequest mockRequest;

    @DisplayName("리뷰 좋아요 상태를 변경한다.")
    @Test
    void changeReviewLike() throws Exception {
        // given
        Long userId = 1L;
        Long reviewId = 1L;
        UpdateReviewLikeReqDto likeReqDto = new UpdateReviewLikeReqDto(reviewId, true);

        when(mockRequest.getAttribute("userId")).thenReturn(userId);

        // when & then
        mockMvc.perform(
                post("/like")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(likeReqDto))
                        .requestAttr("userId", userId))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("좋아요 상태 변경에 성공했습니다."));
    }
}
