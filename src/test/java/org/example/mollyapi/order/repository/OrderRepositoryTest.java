//package org.example.mollyapi.order.repository;
//
//import com.querydsl.jpa.impl.JPAQueryFactory;
//import org.example.mollyapi.common.config.QueryDSLConfig;
//import org.example.mollyapi.order.entity.Order;
//import org.example.mollyapi.order.entity.OrderDetail;
//import org.example.mollyapi.order.type.OrderStatus;
//import org.example.mollyapi.product.entity.Product;
//import org.example.mollyapi.product.entity.ProductItem;
//import org.example.mollyapi.product.repository.ProductItemRepository;
//import org.example.mollyapi.product.repository.ProductRepository;
//import org.example.mollyapi.review.repository.impl.ReviewCustomRepositoryImpl;
//import org.example.mollyapi.user.entity.User;
//import org.example.mollyapi.user.repository.UserRepository;
//import org.junit.jupiter.api.*;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
//import org.springframework.boot.test.mock.mockito.MockBean;
//import org.springframework.context.annotation.Import;
//import org.springframework.test.context.ActiveProfiles;
//
//import java.util.List;
//import java.util.Optional;
//
//import static org.assertj.core.api.Assertions.assertThat;
//
//@DataJpaTest
//@Import(QueryDSLConfig.class)
//@ActiveProfiles("test")
//class OrderRepositoryTest {
//
//    @Autowired
//    private OrderRepository orderRepository;
//
//    @Autowired
//    private OrderDetailRepository orderDetailRepository;
//
//    @Autowired
//    private UserRepository userRepository;
//
//    @Autowired
//    private ProductItemRepository productItemRepository;
//
//    @Autowired
//    private ProductRepository productRepository;
//
//    @Autowired
//    private JPAQueryFactory jpaQueryFactory;
//
//    private User testUser;
//    private Order testOrder;
//    private ProductItem testProductItem;
//
//    @BeforeEach
//    void setup() {
//        // 1. 사용자 생성
//        testUser = userRepository.save(User.builder()
//                .name("test_user")
//                .cellPhone("01012345678")
//                .flag(true)
//                .nickname("test_nickname")
//                .sex(org.example.mollyapi.user.type.Sex.FEMALE)
//                .point(1000)
//                .build());
//
//        // 2. 상품 생성
//        Product testProduct = productRepository.save(Product.builder()
//                .brandName("Nike")
//                .price(10000L)
//                .build());
//
//        testProductItem = productItemRepository.save(ProductItem.builder()
//                .product(testProduct)
//                .color("Black")
//                .size("M")
//                .quantity(10L)
//                .build());
//
//        // 3. 주문 생성 및 저장
//        testOrder = orderRepository.save(new Order(testUser, "ORD-202503111234-5678"));
//        testOrder.updateTotalAmount(5000L);
//        testOrder.updateStatus(OrderStatus.PENDING);
//
//        // 4. 주문 상세(OrderDetail) 추가
//        OrderDetail orderDetail = new OrderDetail(testOrder, testProductItem, "M", 10000L, 1L, "Nike", "신발", null);
//        orderDetailRepository.save(orderDetail);
//        orderRepository.save(testOrder);
//
//        System.out.println("저장된 주문: " + orderRepository.findAll());
//    }
//
//
//    @Test
//    @DisplayName("findByTossOrderIdWithDetails 개별 실행")
//    void testFindByTossOrderIdWithDetails_개별실행() {
//        /// given
//        // Order 테이블에 있는 모든 데이터 출력
//        List<Order> allOrders = orderRepository.findAll();
//        System.out.println("모든 주문 데이터: " + allOrders);
//
//        /// when
//        // 특정 TossOrderId로 조회
//        Optional<Order> foundOrder = orderRepository.findByTossOrderIdWithDetails("ORD-202503111234-5678");
//
//        // 조회된 주문 로그 출력
//        System.out.println("조회 결과: " + foundOrder);
//
//        /// then
//        // 테스트 검증
//        assertThat(foundOrder).isPresent();
//
//        // 주문 객체 검증
//        Order order = foundOrder.get();
//        assertThat(order.getTossOrderId()).isEqualTo("ORD-202503111234-5678");
//        assertThat(order.getOrderDetails()).isNotEmpty();
//        assertThat(order.getOrderDetails().size()).isEqualTo(1);
//    }
//    }