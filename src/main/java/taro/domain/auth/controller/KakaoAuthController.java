package taro.domain.auth.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import taro.domain.auth.dto.TokenResponse;
import taro.domain.auth.service.KakaoAuthService;

import java.io.IOException;

@Slf4j
@RestController
@RequestMapping("/api/auth/kakao")
@RequiredArgsConstructor
@Tag(name = "Kakao Auth", description = "카카오 로그인 API")
public class KakaoAuthController {

    private final KakaoAuthService kakaoAuthService;

    @Value("${spring.security.oauth2.client.registration.kakao.client-id}")
    private String kakaoClientId;

    @Value("${spring.security.oauth2.client.registration.kakao.redirect-uri}")
    private String kakaoRedirectUri;

    @Operation(summary = "카카오 로그인", description = "카카오 OAuth 인증 페이지로 리다이렉트합니다.")
    @GetMapping("/login")
    public void kakaoLogin(HttpServletResponse response) throws IOException {
        String redirectUri = "http://localhost:8080/api/auth/kakao/callback";
        String kakaoAuthUrl = "https://kauth.kakao.com/oauth/authorize"
                + "?client_id=" + kakaoClientId
                + "&redirect_uri=" + redirectUri
                + "&response_type=code"
                + "&scope=profile_nickname,profile_image,account_email";
        
        response.sendRedirect(kakaoAuthUrl);
    }
    
    @Operation(summary = "카카오 콜백", description = "카카오 인증 후 JWT 토큰을 발급합니다.")
    @GetMapping("/callback")
    public ResponseEntity<TokenResponse> kakaoCallback(@RequestParam String code) {
        return ResponseEntity.ok(kakaoAuthService.processKakaoLogin(code));
    }
    
    private String getBaseUrl() {
        // 실제 운영 환경에서는 환경변수나 설정으로 관리
        return "http://localhost:8080";
    }
}
