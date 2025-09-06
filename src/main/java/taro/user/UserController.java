package taro.user;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import taro.auth.CustomUserDetails;
import taro.embedding.EmbeddingService;
import taro.userdraw.UserDrawDto;
import taro.userdraw.UserDrawService;
import taro.repository.UserRepository;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "User", description = "사용자 관련 API")
public class UserController {

    private final UserService userService;
    private final UserRepository userRepository;
    private final EmbeddingService embeddingService;
    private final UserDrawService userDrawService;


    @Operation(summary = "자소서 제출")
    @PostMapping("/resume")
    public ResponseEntity<Void> saveResume(@RequestBody ResumeRequest request,
                                           @AuthenticationPrincipal CustomUserDetails userDetails) {
        Long userId = userDetails.getUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 자소서 임베딩 생성
        byte[] resumeEmbedding = embeddingService.getEmbeddingBytes(request.getResumeText());
        user.setResumeEmbedding(resumeEmbedding);

        // DB 저장
        userRepository.save(user);

        return ResponseEntity.ok().build(); // 본문 없이 200 OK
    }

    @Operation(summary = "모아보기 페이지")
    @GetMapping("/me/draws")
    public ResponseEntity<List<UserDrawDto>> getMyDraws(
            @AuthenticationPrincipal User user  // 로그인된 유저 정보 자동 주입
    ) {
        List<UserDrawDto> drawDTOs = userDrawService.getUserDrawsWithCount(user.getId());
        return ResponseEntity.ok(drawDTOs);
    }
}
