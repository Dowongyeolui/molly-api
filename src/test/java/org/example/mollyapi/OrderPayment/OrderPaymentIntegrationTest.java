package org.example.mollyapi.OrderPayment;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class OrderPaymentIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("주문 생성 후 결제 요청 -> 결제 성공 후 주문 상태 업데이트 확인")
    void testOrderPaymentFlow() throws Exception {
        // Step 1: 주문 생성
        String orderRequest = """
            {
                "userId": 1001,
                "orderRequests": [
                    {"itemId": 101, "quantity": 2},
                    {"itemId": 102, "quantity": 1}
                ]
            }
        """;

        MvcResult orderResult = mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(orderRequest))
                .andExpect(status().isOk())
                .andReturn();

        String orderResponse = orderResult.getResponse().getContentAsString();
        Long orderId = JsonPath.read(orderResponse, "$.orderId"); // JSON 응답에서 orderId 추출

        // Step 2: 결제 요청
        String paymentRequest = """
            {
                "userId": 1001,
                "paymentKey": "payment_abc123",
                "amount": 50000,
                "paymentType": "CREDIT_CARD"
            }
        """;

        mockMvc.perform(post("/orders/" + orderId + "/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(paymentRequest))
                .andExpect(status().isOk());

        // Step 3: 주문 상태 확인 (1초 대기 후)
        Thread.sleep(1000);

        mockMvc.perform(get("/orders/" + orderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCEEDED")); // 주문 상태가 SUCCEEDED인지 확인
    }
}