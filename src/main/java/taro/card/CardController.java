package taro.card;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import taro.auth.CustomUserDetails;
import taro.userdraw.UserDrawService;

@RestController
@RequiredArgsConstructor

public class CardController {

    private final CardService cardService;
    private final UserDrawService userDrawService;
    private final CardRepository cardRepository;

    @Operation(summary = "샘플 카드 삽입", description = "요청 없이 예시 한 건을 삽입합니다.")
    @PostMapping("/seed-one")
    public ResponseEntity<Card> seedOne(@RequestBody CardCreateRequest card) {
        Card created = cardService.create(card);
        return ResponseEntity.ok(created);
    }

    // 카드 뽑기 api (PRE/POST 통합)
    @Operation(description = "카드 뽑기 api, request 로 PRE, POST 구분이 필요함")
    @PostMapping("/card/draw")
    public ResponseEntity<CardSummaryDto> drawCard(@RequestBody CardDrawRequest request,
                                   @AuthenticationPrincipal CustomUserDetails userDetails) {
        Long userId = userDetails.getUserId();
        CardSummaryDto dto = cardService.recommendCard(userId, request.getCardType());
        return ResponseEntity.ok(dto);
    }

    // 기록하러 가기 버튼 클릭 시
    @Operation(description = "카드 저장 api")
    @PostMapping("/card/save")
    public ResponseEntity<Void> saveCardDraw(@RequestBody CardSaveRequest request,
                                 @AuthenticationPrincipal CustomUserDetails userDetails) {
        Long userId = userDetails.getUserId();
        userDrawService.saveUserDraw(userId, request.getCardId(), request.getText());
        return ResponseEntity.ok().build();
    }

    @Operation(description = "카드 조회 api")
    // 카드 조회(설명 보기 단계)
    @GetMapping("/card/{id}/description")
    public ResponseEntity<CardDescriptionDto> getCardDescription(@PathVariable Long id) {
        Card card = cardRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Card not found"));
        CardDescriptionDto dto = new CardDescriptionDto(id,card.getName(), card.getDescription());
        return ResponseEntity.ok(dto);
    }
}
