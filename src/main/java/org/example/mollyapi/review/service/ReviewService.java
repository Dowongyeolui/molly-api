package org.example.mollyapi.review.service;

import lombok.RequiredArgsConstructor;
import org.example.mollyapi.common.exception.CustomException;
import org.example.mollyapi.order.entity.OrderDetail;
import org.example.mollyapi.order.repository.OrderDetailRepository;
import org.example.mollyapi.product.dto.UploadFile;
import org.example.mollyapi.product.entity.Product;
import org.example.mollyapi.product.file.FileStore;
import org.example.mollyapi.product.repository.ProductRepository;
import org.example.mollyapi.review.dto.request.AddReviewReqDto;
import org.example.mollyapi.review.dto.response.GetMyReviewResDto;
import org.example.mollyapi.review.dto.response.GetReviewResDto;
import org.example.mollyapi.review.dto.response.MyReviewInfo;
import org.example.mollyapi.review.dto.response.ReviewInfo;
import org.example.mollyapi.review.entity.Review;
import org.example.mollyapi.review.entity.ReviewImage;
import org.example.mollyapi.review.repository.ReviewRepository;
import org.example.mollyapi.user.entity.User;
import org.example.mollyapi.user.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

import static org.example.mollyapi.common.exception.error.impl.OrderDetailError.NOT_EXIST_ORDERDETIAL;
import static org.example.mollyapi.common.exception.error.impl.ProductItemError.NOT_EXISTS_PRODUCT;
import static org.example.mollyapi.common.exception.error.impl.ReviewError.NOT_ACCESS_REVIEW;
import static org.example.mollyapi.common.exception.error.impl.ReviewError.NOT_EXIST_REVIEW;
import static org.example.mollyapi.common.exception.error.impl.UserError.NOT_EXISTS_USER;

@Service
@RequiredArgsConstructor
public class ReviewService {
    private final FileStore fileStore;
    private final UserRepository userRep;
    private final ProductRepository productRep;
    private final OrderDetailRepository orderDetailRep;
    private final ReviewRepository reviewRep;

    /**
     * 리뷰 작성 기능
     * @param addReviewReqDto 주문상세 PK와 내용이 담긴 DTO
     * @param uploadImages 업로드한 이미지 파일
     * @param userId 사용자 PK
     * */
    @Transactional
    public void addReview(
            AddReviewReqDto addReviewReqDto, // Long orderDetailId, String content,
            List<MultipartFile> uploadImages,
            Long userId
    ) {
        Long orderDetailId = addReviewReqDto.orderDetailId();
        String content = addReviewReqDto.content();

        // 가입된 사용자 여부 체크
        User user = userRep.findById(userId)
                .orElseThrow(() -> new CustomException(NOT_EXISTS_USER));

        // 주문 상세 조회
        OrderDetail orderDetail = orderDetailRep.findById(orderDetailId)
                .orElseThrow(() -> new CustomException(NOT_EXIST_ORDERDETIAL));

        // 상품 정보 조회
        Product product = productRep.findById(orderDetail.getProductItem().getProduct().getId())
                .orElseThrow(() -> new CustomException(NOT_EXISTS_PRODUCT));

        //리뷰 작성 권한 체크
        Review review = reviewRep.findByIsDeletedAndOrderDetailIdAndUserUserId(false, orderDetailId, userId);
        if(review != null) throw new CustomException(NOT_ACCESS_REVIEW);

        // 업로드된 이미지 파일 저장
        List<UploadFile> uploadImageFiles = fileStore.storeFiles(uploadImages);

        // 리뷰 생성
        Review newReview = Review.builder()
                .content(content)
                .reviewImages(null)
                .isDeleted(false)
                .user(user)
                .orderDetail(orderDetail)
                .product(product)
                .build();

        //저장된 파일로 리뷰 이미지 생성
        List<ReviewImage> images = new ArrayList<>();
        for(int i=0; i< uploadImageFiles.size(); i++) {
            images.add(ReviewImage.createReviewImage(newReview, uploadImageFiles.get(i), (long) i));
        }

        // 리뷰에 이미지 추가
        newReview.updateImages(images);
        reviewRep.save(newReview);
    }


    /**
     * 상품별 리뷰 조회
     * @param productId 상품 PK
     * @param userId 사용자 PK
     * @return reviewResDtoList 리뷰 정보를 담은 DtoList
     * */
    @Transactional
    public ResponseEntity<?> getReviewList(Long productId, Long userId) {
        // 상품 존재 여부 체크
        Product product = productRep.findById(productId)
                .orElseThrow(() -> new CustomException(NOT_EXISTS_PRODUCT));

        // 해당 상품의 리뷰 정보 조회
        List<ReviewInfo> reviewInfoList = reviewRep.getReviewInfo(productId, userId);
        if(reviewInfoList.isEmpty()) throw new CustomException(NOT_EXIST_REVIEW);

        // Response로 전달할 상품 리뷰 정보 담기
        List<GetReviewResDto> reviewResDtoList = new ArrayList<>();
        for(ReviewInfo info : reviewInfoList) {
            List<String> images = reviewRep.getImageList(info.reviewId());
            if(images.isEmpty()) continue;

            // 리스트에 리뷰 정보 담기
            reviewResDtoList.add(new GetReviewResDto(info, images));
        }
        return ResponseEntity.ok(reviewResDtoList);
    }

    /**
     * 사용자 본인이 작성한 리뷰 조회
     * @param userId 사용자 PK
     * @return myReviewResDtoList 사용자 본인이 작성한 리뷰 정보를 담은 DtoList
     * */
    public ResponseEntity<?> getMyReviewList(Long userId) {
        // 가입된 사용자 여부 체크
        User user = userRep.findById(userId)
                .orElseThrow(() -> new CustomException(NOT_EXISTS_USER));

        // 사용자 본인이 작성한 리뷰 정보 조회
        List<MyReviewInfo> myReviewInfoList = reviewRep.getMyReviewInfo(userId);
        if(myReviewInfoList.isEmpty()) throw new CustomException(NOT_EXIST_REVIEW);

        // Response로 전달할 상품 리뷰 정보 담기
        List<GetMyReviewResDto> myReviewResDtoList = new ArrayList<>();
        for(MyReviewInfo info : myReviewInfoList) {
            List<String> images = reviewRep.getImageList(info.reviewId());
            if(images.isEmpty()) continue;

            // 리스트에 리뷰 정보 담기
            myReviewResDtoList.add(new GetMyReviewResDto(info, images));
        }
        return ResponseEntity.ok(myReviewResDtoList);
    }
}
