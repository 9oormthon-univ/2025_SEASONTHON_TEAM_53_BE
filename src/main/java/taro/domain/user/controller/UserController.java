package taro.domain.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import taro.domain.user.dto.TutorialRequest;
import taro.domain.user.dto.UserResponse;
import taro.domain.user.service.UserService;
import taro.security.CustomUserDetails;

@Slf4j
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "User", description = "사용자 관련 API")
public class UserController {

    private final UserService userService;

    @Operation(summary = "내 정보 조회", description = "현재 로그인된 사용자의 정보를 조회합니다.")
    @GetMapping("/me")
    public ResponseEntity<UserResponse> getMyInfo(@AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(userService.getUserInfo(userDetails.getUser().getId()));
    }
    
    @Operation(summary = "튜토리얼 정보 저장", description = "닉네임, 직업 분야, 성향을 저장하고 튜토리얼 완료 처리합니다.")
    @PutMapping("/profile")
    public ResponseEntity<UserResponse> saveTutorial(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody TutorialRequest request) {
        return ResponseEntity.ok(userService.saveTutorial(userDetails.getUser().getId(), request));
    }
}
