package taro.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
@Schema(description = "카카오 로그인 응답 DTO")
public class KakaoLoginResponse {

    @Schema(description = "가입 여부", example = "true")
    private boolean isSignedUp;

    @Schema(description = "카카오 이메일")
    private String kakaoEmail;

    @Schema(description = "JWT 토큰 정보 (가입된 사용자만 존재)")
    private TokenDto tokenDto;
}