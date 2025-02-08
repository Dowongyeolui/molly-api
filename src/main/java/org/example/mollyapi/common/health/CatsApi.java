package org.example.mollyapi.common.health;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.mollyapi.common.dto.CommonResDto;
import org.example.mollyapi.common.exception.CustomException;
import org.example.mollyapi.user.auth.annotation.Auth;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static org.example.mollyapi.common.exception.error.impl.CommonError.HEALTH_DISABLE;


@Tag(name = "헬스 체크 및 테스트 API", description = "나의 아주 작은 고양이 건강 챙김 및 테스트 API")
@RestController
public class CatsApi {

    @GetMapping("/cat")
    @Operation(summary = "Health Check", description = "HI this API is health Check")
    public ResponseEntity<CommonResDto> getCat(
            @Parameter(name = "catId", description = "0일 시 Error, 그 외 정상 동작", in = ParameterIn.QUERY) @RequestParam int catId) {

        if (catId == 0) throw new CustomException(HEALTH_DISABLE);
        return ResponseEntity.ok(new CommonResDto("cat is very very very cute :), you know?"));
    }

    @Auth
    @GetMapping("/cat/auth")
    @Operation(summary = "인증 테스트", description = "HI this is auth test api")
    public ResponseEntity<CommonResDto> passAuth(){
        return ResponseEntity.ok(new CommonResDto("auth cat is very strong!!"));
    }
}
