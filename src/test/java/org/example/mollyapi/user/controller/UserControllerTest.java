package org.example.mollyapi.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.mollyapi.user.service.SignUpService;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.*;

@WebMvcTest(controllers = UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private SignUpService signUpService;


    @Test
    void getUserInfo() {

    }

    @Test
    void updateUserInfo() {
    }

    @Test
    void getUserSummaryInfoWithOptionalPoint() {
    }

    @Test
    void deleteUserInfo() {
    }
}