package org.example.mollyapi.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.mollyapi.user.dto.SignUpReqDto;
import org.example.mollyapi.user.service.SignUpService;
import org.example.mollyapi.user.type.Sex;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = SignUpController.class)
class SignUpControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private SignUpService signUpService;

    @Test
    @DisplayName("회원가입 요청에 성공했다.")
    void singUp_Success() throws Exception {
        //given
        SignUpReqDto request = createRequest("꽃달린감나무", "test@example.com", "01011112222", "꽃감이", "qwer1234@", Sex.MALE, LocalDate.of(1997, 5, 12));

        //then
        mockMvc.perform(MockMvcRequestBuilders.post("/sign-up")
                        .content(objectMapper.writeValueAsString(request))
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andDo(print())
                .andExpect(status().isNoContent())
                .andExpect(jsonPath("$.message").value("회원가입에 성공하셨습니다."));
    }

    @Test
    @DisplayName("회원가입 시, 닉네임 누락했다.")
    void singUp_Without_nickName() throws Exception {
        //given
        SignUpReqDto request = createRequest("  ",
                "test@example.com",
                "01011112222",
                "꽃감이",
                "qwer1234@",
                Sex.MALE, LocalDate.of(1997, 5, 12));

        //then
        mockMvc.perform(MockMvcRequestBuilders.post("/sign-up")
                        .content(objectMapper.writeValueAsString(request))
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("NOT_PARAM_VALID"))
                .andExpect(jsonPath("$.nickname").value("적당한 닉네임이 생각나지 않으면, 하늘을나는 고양이는 어때요?"));
    }

    @Test
    @DisplayName("회원가입 시, 전화번호를 누락했다.")
    void singUp_Wrong_cellPhone() throws Exception {
        //given
        SignUpReqDto request = createRequest("  ", "test@example.com", "01011112222", "꽃감이", "qwer1234@", Sex.MALE, LocalDate.of(1997, 5, 12));

        //then
        mockMvc.perform(MockMvcRequestBuilders.post("/sign-up")
                        .content(objectMapper.writeValueAsString(request))
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("NOT_PARAM_VALID"))
                .andExpect(jsonPath("$.nickname").value("적당한 닉네임이 생각나지 않으면, 하늘을나는 고양이는 어때요?"));
    }

    @Test
    @DisplayName("회원가입 시, 성별 누락했다.")
    void singUp_Wrong_Sex() throws Exception {
        //given
        SignUpReqDto request = createRequest("꽃달린감나무",
                "test@example.com",
                "01011112222",
                "꽃감이",
                "qwer1234@",
                null, LocalDate.of(1997, 5, 12));

        //then
        mockMvc.perform(MockMvcRequestBuilders.post("/sign-up")
                        .content(objectMapper.writeValueAsString(request))
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("NOT_PARAM_VALID"))
                .andExpect(jsonPath("$.sex").value("유효하지 않은 성별이 입력되었습니다."));
    }

    @Test
    @DisplayName("회원가입 시, 이름을 잘못 입력했다.")
    void singUp_Wrong_Name() throws Exception {
        //given
        SignUpReqDto request = createRequest("꽃달린감나무",
                "test@example.com",
                "01011112222",
                "  ",
                "qwer1234@",
                Sex.MALE,
                LocalDate.of(2012,1,2));

        //then
        mockMvc.perform(MockMvcRequestBuilders.post("/sign-up")
                        .content(objectMapper.writeValueAsString(request))
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("NOT_PARAM_VALID"))
                .andExpect(jsonPath("$.name").value("이름은 필수 값입니다."));
    }

    @Test
    @DisplayName("회원가입 시, 이메일을 누락했다.")
    void singUp_Wrong_Email() throws Exception {
        //given
        SignUpReqDto request = createRequest("꽃달린감나무",
                "  ",
                "01011112222",
                "꽃달린감나무",
                "qwer1234@",
                Sex.MALE,
                LocalDate.of(2012,1,2));

        //then
        mockMvc.perform(MockMvcRequestBuilders.post("/sign-up")
                        .content(objectMapper.writeValueAsString(request))
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("NOT_PARAM_VALID"))
                .andExpect(jsonPath("$.email").value("이메일 형식을 맞춰주세요! cat@cats.com"));
    }

    @Test
    @DisplayName("회원가입 시, 이메일을 잘못 입력했다.")
    void singUp_Wrong_Email_2() throws Exception {

        //given
        SignUpReqDto request = createRequest("꽃달린감나무",
                "asdfeke",
                "01011112222",
                "꽃달린감나무",
                "qwer1234@",
                Sex.MALE,
                LocalDate.of(2012,1,2));

        //then
        mockMvc.perform(MockMvcRequestBuilders.post("/sign-up")
                        .content(objectMapper.writeValueAsString(request))
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("NOT_PARAM_VALID"))
                .andExpect(jsonPath("$.email").value("이메일 형식을 맞춰주세요! cat@cats.com"));
    }


    private static SignUpReqDto createRequest(String nickName, String email, String number, String name, String password, Sex sex, LocalDate birth) {
        return new SignUpReqDto(
                nickName,
                number,
                sex,
                birth,
                name,
                email,
                password,
                true
        );
    }
}