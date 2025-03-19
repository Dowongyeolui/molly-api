package org.example.mollyapi.review.controller;

import org.example.mollyapi.review.dto.response.GetTrendingReviewResDto;
import org.example.mollyapi.review.dto.response.TrendingReviewResDto;
import org.example.mollyapi.review.service.TrendingReviewService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TrendingReviewController.class)
public class TrendingReviewControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TrendingReviewService trendingReviewService;

    @DisplayName("최근 7일간 좋아요가 높은 인기 순위 리뷰 Top12를 조회한다")
    @Test
    void getTrendingReview() throws Exception {
        // given
        List<GetTrendingReviewResDto> trendingReviewList = new ArrayList<>();
        TrendingReviewResDto reviewResDto1 = new TrendingReviewResDto(1L ,"Test content 1", "존맛 망고", "/images/profile.jpg", 1L, "2025-03-08",  10L);
        TrendingReviewResDto reviewResDto2 = new TrendingReviewResDto(2L ,"Test content 2", "최강 딸기", "/images/profile2.jpg", 1L, "2025-03-05",  8L);
        trendingReviewList.add(new GetTrendingReviewResDto(reviewResDto1,  List.of("/images/review1_image1.jpg", "/images/review1_image2.jpg")));
        trendingReviewList.add(new GetTrendingReviewResDto(reviewResDto2, List.of("/images/review2_image1.jpg")));

        when(trendingReviewService.getTrendingReview()).thenReturn(trendingReviewList);

        // when & then
        mockMvc.perform(
                get("/trending"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }
}
