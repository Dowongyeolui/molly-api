package org.example.mollyapi.review.service;

import lombok.RequiredArgsConstructor;
import org.example.mollyapi.common.client.ImageClient;
import org.example.mollyapi.common.exception.CustomException;
import org.example.mollyapi.order.entity.OrderDetail;
import org.example.mollyapi.order.repository.OrderDetailRepository;
import org.example.mollyapi.product.dto.UploadFile;
import org.example.mollyapi.product.dto.response.ListResDto;
import org.example.mollyapi.product.dto.response.PageResDto;
import org.example.mollyapi.product.entity.Product;
import org.example.mollyapi.common.enums.ImageType;
import org.example.mollyapi.product.repository.ProductRepository;
import org.example.mollyapi.review.dto.request.AddReviewReqDto;
import org.example.mollyapi.review.dto.response.GetMyReviewResDto;
import org.example.mollyapi.review.dto.response.GetReviewResDto;
import org.example.mollyapi.review.dto.response.MyReviewInfoDto;
import org.example.mollyapi.review.dto.response.ReviewInfoDto;
import org.example.mollyapi.review.entity.Review;
import org.example.mollyapi.review.entity.ReviewImage;
import org.example.mollyapi.review.repository.ReviewImageRepository;
import org.example.mollyapi.review.repository.ReviewLikeRepository;
import org.example.mollyapi.review.repository.ReviewRepository;
import org.example.mollyapi.user.entity.User;
import org.example.mollyapi.user.repository.UserRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.SliceImpl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

import static org.example.mollyapi.common.exception.error.impl.OrderDetailError.NOT_EXIST_ORDERDETIAL;
import static org.example.mollyapi.common.exception.error.impl.ProductItemError.NOT_EXISTS_PRODUCT;
import static org.example.mollyapi.common.exception.error.impl.ReviewError.*;
import static org.example.mollyapi.common.exception.error.impl.UserError.NOT_EXISTS_USER;

@Service
@RequiredArgsConstructor
public class ReviewService {
    private final ImageClient imageClient;
    private final UserRepository userRep;
    private final ProductRepository productRep;
    private final OrderDetailRepository orderDetailRep;
    private final ReviewRepository reviewRep;
    private final ReviewImageRepository reviewImageRep;
    private final ReviewLikeRepository reviewLikeRep;

    /**
     * 리뷰 작성 기능
     * @param addReviewReqDto 주문상세 PK와 내용이 담긴 DTO
     * @param uploadImages 업로드한 이미지 파일
     * @param userId 사용자 PK
     * */
    @Transactional
    public void registerReview(
            AddReviewReqDto addReviewReqDto, // Long orderDetailId, String content
            List<MultipartFile> uploadImages,
            Long userId
    ) {
        Long orderDetailId = addReviewReqDto.id(); //주문상세 PK
        String content = addReviewReqDto.content(); //리뷰 내용

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
        Review review = reviewRep.findByIsDeletedAndOrderDetailIdAndUserUserId(true, orderDetailId, userId);
        if(review != null) throw new CustomException(NOT_ACCESS_REVIEW);

        // 리뷰 생성
        Review newReview = Review.builder()
                .content(content)
                .isDeleted(Boolean.FALSE)
                .user(user)
                .orderDetail(orderDetail)
                .product(product)
                .build();

        // 업로드된 이미지 파일 저장
        saveReviewImages(newReview, uploadImages);

        reviewRep.save(newReview);
    }

    /**
     * 상품별 리뷰 조회
     * @param pageable 페이지 처리에 필요한 정보를 담는 인터페이스
     * @param productId 상품 PK
     * @param userId 사용자 PK
     * @return reviewResDtoList 리뷰 정보를 담은 DtoList
     * */
    @Transactional
    public ResponseEntity<?> getReviewList(Pageable pageable, Long productId, Long userId) {
        // 상품 존재 여부 체크
        boolean existsProduct = productRep.existsById(productId);
        if(!existsProduct) throw new CustomException(NOT_EXISTS_PRODUCT);

        // 해당 상품의 리뷰 정보 조회
        List<ReviewInfoDto> reviewInfoList = reviewRep.getReviewInfo(pageable, productId, userId);
        if(reviewInfoList.isEmpty()) throw new CustomException(NOT_EXIST_REVIEW);

        // Response로 전달할 상품 리뷰 정보 담기
        List<GetReviewResDto> reviewResDtoList = new ArrayList<>();
        for(ReviewInfoDto info : reviewInfoList) {
            List<String> images = reviewRep.getImageList(info.reviewId());
            if(images.isEmpty()) continue;

            // 리뷰 정보를 DTO에 추가
            reviewResDtoList.add(new GetReviewResDto(info, images));
        }

        // 페이지네이션을 위한 hasNext 플래그 설정
        boolean hasNext = false;
        if (reviewResDtoList.size() > pageable.getPageSize()) {
            reviewResDtoList.remove(pageable.getPageSize());
            hasNext = true;
        }

        // Slice 형태로 리뷰 리스트 생성
        SliceImpl<GetReviewResDto> sliceList = new SliceImpl<>(reviewResDtoList, pageable, hasNext);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(new ListResDto(
                        new PageResDto(
                                (long) sliceList.getNumberOfElements(), // 현재 페이지 요소 개수
                                sliceList.hasNext(), // 다음 페이지 존재 여부
                                sliceList.isFirst(), // 첫 번째 페이지 여부
                                sliceList.isLast() //마지막 페이지 여부
                        ),
                        sliceList.getContent()
                ));
    }

    /**
     * 사용자 본인이 작성한 리뷰 조회
     * @param pageable 페이지 처리에 필요한 정보를 담는 인터페이스
     * @param userId 사용자 PK
     * @return myReviewResDtoList 사용자 본인이 작성한 리뷰 정보를 담은 DtoList
     * */
    public ResponseEntity<?> getMyReviewList(Pageable pageable, Long userId) {
        // 가입된 사용자 여부 체크
        boolean existsUser = userRep.existsById(userId);
        if(!existsUser) throw new CustomException(NOT_EXISTS_USER);

        // 사용자 본인이 작성한 리뷰 정보 조회
        List<MyReviewInfoDto> myReviewInfoList = reviewRep.getMyReviewInfo(pageable, userId);
        if(myReviewInfoList.isEmpty()) throw new CustomException(NOT_EXIST_REVIEW);

        // Response로 전달할 상품 리뷰 정보 담기
        List<GetMyReviewResDto> myReviewResDtoList = new ArrayList<>();
        for(MyReviewInfoDto info : myReviewInfoList) {
            List<String> images = reviewRep.getImageList(info.reviewId());
            if(images.isEmpty()) continue;

            // 리스트에 리뷰 정보 담기
            myReviewResDtoList.add(new GetMyReviewResDto(info, images));
        }

        // 페이지네이션을 위한 hasNext 플래그 설정
        boolean hasNext = false;
        if (myReviewResDtoList.size() > pageable.getPageSize()) {
            myReviewResDtoList.remove(pageable.getPageSize());
            hasNext = true;
        }

        // Slice 형태로 리뷰 리스트 생성
        SliceImpl<GetMyReviewResDto> sliceList = new SliceImpl<>(myReviewResDtoList, pageable, hasNext);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(new ListResDto(
                        new PageResDto(
                                (long) sliceList.getNumberOfElements(), // 현재 페이지 요소 개수
                                sliceList.hasNext(), // 다음 페이지 존재 여부
                                sliceList.isFirst(), // 첫 번째 페이지 여부
                                sliceList.isLast() //마지막 페이지 여부
                        ),
                        sliceList.getContent()
                ));
    }

    /**
     * 리뷰 작성 기능
     * @param addReviewReqDto 주문상세 PK와 내용이 담긴 DTO
     * @param uploadImages 업로드한 이미지 파일
     * @param userId 사용자 PK
     * */
    @Transactional
    public void updateReview(
            AddReviewReqDto addReviewReqDto, // Long reviewId, String content
            List<MultipartFile> uploadImages,
            Long userId
    ) {
        Long reviewId = addReviewReqDto.id(); //리뷰 PK
        String content = addReviewReqDto.content();

        // 가입된 사용자 여부 체크
        boolean existsUser = userRep.existsById(userId);
        if(!existsUser) throw new CustomException(NOT_EXISTS_USER);

        // 변경하려는 리뷰 체크
        Review review = reviewRep.findByIdAndIsDeleted(reviewId, false)
                .orElseThrow(() -> new CustomException(NOT_ACCESS_REVIEW));

        // 리뷰 내용 변경
        if(content != null) review.updateContent(content);

        // 리뷰 이미지 변경 여부 체크
        if(uploadImages != null) {
            // 기존의 리뷰 이미지 삭제
            // 추가 예정

            // 리뷰에 새로운 이미지 추가
            saveReviewImages(review, uploadImages);
            reviewRep.save(review);
        }
    }

    /**
     * 리뷰 삭제 기능(삭제 후 재작성이 불가능하기 때문에, 삭제 여부 칼럼 업데이트
     * @param reviewId 리뷰 PK
     * @param userId 사용자 PK
     * */
    @Transactional
    public ResponseEntity<?> deleteReview(Long reviewId, Long userId) {
        // 해당하는 리뷰가 있다면
        Review review = reviewRep.findByIdAndUserUserId(reviewId, userId)
                .orElseThrow(() -> new CustomException(NOT_EXIST_REVIEW));

        // 리뷰 삭제 여부 변경
        boolean isUpdate = review.updateIsDeleted(Boolean.TRUE);
        if(!isUpdate) throw new CustomException(FAIL_UPDATE);

        // 리뷰와 연결된 이미지 삭제 - 이미지 서버(구현 예정)
        // 리뷰와 연결된 이미지 삭제 - 테이블
        // reviewImageRep.deleteAllByReviewId(reviewId);

        // 리뷰와 연결된 좋아요 삭제
        reviewLikeRep.deleteAllByReviewId(reviewId);

        return ResponseEntity.ok().body("리뷰 삭제에 성공했습니다.");
    }

    /**
     * 업로드된 이미지 파일 저장
     * @param review review Entity
     * @param uploadImages 업로드한 이미지 파일
     * */
    private void saveReviewImages(Review review, List<MultipartFile> uploadImages) {
        List<UploadFile> uploadFiles = imageClient.upload(ImageType.REVIEW, uploadImages);

        for (int i = 0; i < uploadFiles.size(); i++) {
            UploadFile uploadFile = uploadFiles.get(i);
            ReviewImage reviewImage = ReviewImage.createReviewImage(review, uploadFile, i);
            review.addImage(reviewImage);  // 리뷰에 이미지 추가
        }
    }
}
