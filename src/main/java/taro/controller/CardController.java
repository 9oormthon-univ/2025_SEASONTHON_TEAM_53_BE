package taro.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import taro.domain.Card;
import taro.service.CardService;
import taro.security.CustomUserDetails;

@Slf4j
@RestController
@RequestMapping("/api/tarot")
@RequiredArgsConstructor
@Tag(name = "Tarot", description = "타로 카드 관련 API")
public class CardController {
    
    private final CardService cardService;
    
    @Operation(summary = "타로 카드 뽑기", description = "오늘의 타로 카드를 뽑습니다.")
    @GetMapping("/draw")
    public ResponseEntity<Card> drawCard(
            @RequestParam(required = false) String stage,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        
        // 튜토리얼 완료 검증 (BEFORE 단계인 경우)
        if ("BEFORE".equals(stage) && !userDetails.getUser().isTutorialCompleted()) {
            throw new RuntimeException("튜토리얼을 먼저 완료해주세요.");
        }
        
        Card card = cardService.drawCard(userDetails.getUser().getId());
        return ResponseEntity.ok(card);
    }
}
