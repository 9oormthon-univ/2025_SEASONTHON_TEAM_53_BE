package taro.auth;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@Builder
public class TokenDto {
    private String grantType;           // 예: "Bearer"
    private String accessToken;         // 발급된 액세스 토큰 문자열
    private Long accessTokenExpiresIn;  // 액세스 토큰 만료 시간 (밀리초, timestamp)
    private String refreshToken;        // 발급된 리프레시 토큰 문자열
}
