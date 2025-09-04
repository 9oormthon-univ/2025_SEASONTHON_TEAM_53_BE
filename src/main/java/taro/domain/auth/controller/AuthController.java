package taro.domain.auth.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import taro.domain.auth.dto.TokenRefreshRequest;
import taro.domain.auth.dto.TokenResponse;
import taro.domain.auth.service.AuthService;
import taro.security.CustomUserDetails;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Auth", description = "인증 관련 API")
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "토큰 갱신", description = "Refresh Token을 이용해 새로운 Access Token을 발급받습니다.")
    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refreshToken(@RequestBody TokenRefreshRequest request) {
        return ResponseEntity.ok(authService.refreshToken(request.getRefreshToken()));
    }

    @Operation(summary = "로그아웃", description = "로그아웃 처리를 합니다.")
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@AuthenticationPrincipal CustomUserDetails userDetails) {
        authService.logout(userDetails.getUser().getId());
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "회원 탈퇴", description = "현재 로그인된 사용자의 계정을 삭제합니다.")
    @DeleteMapping("/withdraw")
    public ResponseEntity<Void> withdraw(@AuthenticationPrincipal CustomUserDetails userDetails) {
        authService.withdraw(userDetails.getUser().getId());
        return ResponseEntity.ok().build();
    }
}
