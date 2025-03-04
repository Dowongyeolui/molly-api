package org.example.mollyapi.order;

import org.example.mollyapi.address.dto.AddressResponseDto;
import org.example.mollyapi.address.repository.AddressRepository;
import org.example.mollyapi.cart.repository.CartRepository;
import org.example.mollyapi.order.dto.OrderRequestDto;
import org.example.mollyapi.order.dto.OrderResponseDto;
import org.example.mollyapi.order.entity.Order;
import org.example.mollyapi.order.repository.OrderRepository;
import org.example.mollyapi.order.service.OrderServiceImpl;
import org.example.mollyapi.order.type.OrderStatus;
import org.example.mollyapi.product.entity.ProductItem;
import org.example.mollyapi.product.repository.ProductItemRepository;
import org.example.mollyapi.user.entity.User;
import org.example.mollyapi.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @InjectMocks
    private OrderServiceImpl orderService;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProductItemRepository productItemRepository;

    @Mock
    private AddressRepository addressRepository;

    @Mock
    private CartRepository cartRepository;

    private User testUser;
    private ProductItem testProductItem;
    private OrderRequestDto testOrderRequest;

    @BeforeEach
    void setUp() {
        // 사용자 정보 설정
        testUser = new User();
        testUser.setUserId(1L);

        // 상품 정보 설정
        testProductItem = new ProductItem();
        testProductItem.setId(100L);
        testProductItem.setQuantity(10);

        // 주문 요청 DTO 생성
        testOrderRequest = new OrderRequestDto(null, testProductItem.getId(), 2L); // 2개 구매 요청

        // Mock 데이터 설정
        when(userRepository.findById(testUser.getUserId())).thenReturn(Optional.of(testUser));
        when(productItemRepository.findProductItemById(testProductItem.getId())).thenReturn(testProductItem);
        when(addressRepository.findByUserAndDefaultAddr(testUser, true))
                .thenReturn(Optional.of(AddressResponseDto.from(null)));
    }

    /**
     * 정상 주문 생성 테스트
     */
    @Test
    void createOrder_Success() {
        // when
        OrderResponseDto response = orderService.createOrder(testUser.getUserId(), List.of(testOrderRequest));

        // then
        assertThat(response).isNotNull();
        assertThat(response.status()).isEqualTo(OrderStatus.PENDING);
        verify(orderRepository, times(1)).save(any(Order.class));
    }


    @Test
    void 예외_존재하지_않는_사용자_ID로_주문_시_예외_발생() {
        // given
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        // when & then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> orderService.createOrder(999L, List.of(testOrderRequest)));

        assertThat(exception.getMessage()).contains("사용자를 찾을 수 없습니다.");
    }


    @Test
    void 예외_tossOrderId_생성_규칙_위배_테스트() {
        // given
        when(orderRepository.findByTossOrderId(anyString())).thenReturn(true);

        // when & then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> orderService.createOrder(testUser.getUserId(), List.of(testOrderRequest)));

        assertThat(exception.getMessage()).contains("중복된 tossOrderId 생성 오류");
    }


    @Test
    void 예외_유효하지_않은_상품_ID로_주문_시_예외_발생() {
        // given
        when(productItemRepository.findProductItemById(anyLong())).thenReturn(null);

        // when & then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> orderService.createOrder(testUser.getUserId(), List.of(testOrderRequest)));

        assertThat(exception.getMessage()).contains("상품을 찾을 수 없습니다.");
    }


    @Test
    void 예외_재고_부족_시_주문_실패_테스트() {
        // given
        testProductItem.setQuantity(1); // 재고를 1개로 설정
        when(productItemRepository.findProductItemById(testProductItem.getId())).thenReturn(testProductItem);

        // when & then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> orderService.createOrder(testUser.getUserId(), List.of(testOrderRequest)));

        assertThat(exception.getMessage()).contains("재고가 부족하여 주문할 수 없습니다.");
    }


    @Test
    void 예외_장바구니_데이터_오류_테스트() {
        // given
        when(cartRepository.findById(anyLong())).thenReturn(Optional.empty());

        // when & then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> orderService.createOrder(testUser.getUserId(), List.of(new OrderRequestDto(99L, null, 1L))));

        assertThat(exception.getMessage()).contains("장바구니 항목을 찾을 수 없습니다.");
    }


    @Test
    void 예외_기본_배송지_없음_테스트() {
        // given
        when(addressRepository.findByUserAndDefaultAddr(testUser, true)).thenReturn(Optional.empty());

        // when
        OrderResponseDto response = orderService.createOrder(testUser.getUserId(), List.of(testOrderRequest));

        // then
        assertThat(response.defaultAddress()).isNull();
    }


    @Test
    void 예외_중복_주문_요청_테스트() {
        // given
        when(orderRepository.findByTossOrderId(anyString())).thenReturn(true);

        // when & then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> orderService.createOrder(testUser.getUserId(), List.of(testOrderRequest, testOrderRequest)));

        assertThat(exception.getMessage()).contains("주문이 이미 존재합니다.");
    }


    @Test
    void 예외_하나의_사용자가_다중_주문_요청_시_예외_발생() {
        // given
        when(orderRepository.findByUserAndStatus(testUser, OrderStatus.PENDING)).thenReturn(true);

        // when & then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> orderService.createOrder(testUser.getUserId(), List.of(testOrderRequest)));

        assertThat(exception.getMessage()).contains("이미 진행 중인 주문이 있습니다.");
    }
}