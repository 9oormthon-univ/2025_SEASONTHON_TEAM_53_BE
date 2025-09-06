package taro.auth;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Kakao Auth", description = "카카오 로그인 및 회원가입 API")
public class KakaoController {

    private final KakaoService kakaoService;

    // 🔍 code를 브라우저로 받기 위한 디버그 엔드포인트
    @GetMapping("/kakao/test")
    public ResponseEntity<String> testCode(@RequestParam("code") String code) {
        return ResponseEntity.ok("받은 인가 코드: " + code);
    }

    @Operation(summary = "카카오 리디렉트 처리용 GET", description = "카카오가 리디렉트해주는 code를 GET으로 받아 로그인 처리")
    @GetMapping("/kakao")
    public ResponseEntity<KakaoLoginResponse> kakaoRedirectLogin(@RequestParam("code") String code) {
        return ResponseEntity.ok(kakaoService.handleKakaoLogin(code));
    }

    @Operation(summary = "회원가입", description = "카카오 이메일 + 사용자 정보 입력 후 최종 회원가입 -> ★★★★★★★★이거 쓰면 됨")
    @PostMapping("/kakao/signup")
    public ResponseEntity<TokenDto> signup(@RequestBody SignUpRequest request) {
        return ResponseEntity.ok(kakaoService.completeSignup(request));
    }

    @Operation(summary = "JWT 재발급", description = "Refresh Token을 이용해 Access Token 재발급")
    @PostMapping("/refresh")
    public ResponseEntity<TokenDto> refresh(@RequestParam("refreshToken") String refreshToken) {
        return ResponseEntity.ok(kakaoService.refreshToken(refreshToken));
    }
}
