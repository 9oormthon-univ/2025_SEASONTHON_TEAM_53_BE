package taro.card;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import taro.user.User;
import taro.embedding.EmbeddingService;
import taro.repository.UserDrawRepository;
import taro.repository.UserRepository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CardService {

    private final CardRepository cardRepository;
    private final UserDrawRepository userDrawRepository;
    private final UserRepository userRepository;
    private final EmbeddingService embeddingService;

    // 컨트롤러에서 호출되는 단일 메서드
    public CardSummaryDto recommendCard(Long userId, CardType cardType) {
        if (cardType == CardType.PRE) {
            return drawPreCard(userId);
        } else if (cardType == CardType.POST) {
            return drawPostCard(userId);
        } else {
            throw new IllegalArgumentException("지원하지 않는 카드 타입: " + cardType);
        }
    }

    public CardSummaryDto drawPreCard(Long userId) {

        CardType cardType = CardType.PRE;

        LocalDate today = LocalDate.now();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));


        // 1. 오늘 이미 뽑았는지 체크
        boolean alreadyDrawnToday = userDrawRepository.existsByUserIdAndDrawDate(userId, today);
        if (alreadyDrawnToday) {
            throw new RuntimeException("오늘 이미 카드를 뽑았습니다.");
        }


        List<float[]> allEmbeddings = new ArrayList<>();

        float[] textEmb = embeddingService.bytesToFloatArray(user.getTextEmbedding());
        if (textEmb != null) allEmbeddings.add(textEmb);

        // 2️⃣ 평균 벡터 계산
        float[] finalVector = embeddingService.averageEmbedding(allEmbeddings);

        // 3️⃣ 카드 가져오기 (PRE 또는 POST)
        List<Card> cards = cardRepository.findByType(cardType);

        // 4️⃣ 가장 유사한 카드 찾기
        Card bestCard = embeddingService.findMostSimilar(finalVector, cards);

        // 5️⃣ DTO 반환
        return new CardSummaryDto(bestCard.getId(), bestCard.getName());
    }


    public CardSummaryDto drawPostCard(Long userId) {

        CardType cardType = CardType.POST;

        LocalDate today = LocalDate.now();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));


        // 1. 오늘 이미 뽑았는지 체크
        boolean alreadyDrawnToday = userDrawRepository.existsByUserIdAndDrawDate(userId, today);
        if (alreadyDrawnToday) {
            throw new RuntimeException("오늘 이미 카드를 뽑았습니다.");
        }


        List<float[]> allEmbeddings = new ArrayList<>();



        float[] textEmb = embeddingService.bytesToFloatArray(user.getTextEmbedding());
        if (textEmb != null) allEmbeddings.add(textEmb);

        float[] resumeEmb = embeddingService.bytesToFloatArray(user.getResumeEmbedding());
        if(resumeEmb != null) allEmbeddings.add(resumeEmb);

        // 2️⃣ 평균 벡터 계산
        float[] finalVector = embeddingService.averageEmbedding(allEmbeddings);

        // 3️⃣ 카드 가져오기 (PRE 또는 POST)
        List<Card> cards = cardRepository.findByType(cardType);

        // 4️⃣ 가장 유사한 카드 찾기
        Card bestCard = embeddingService.findMostSimilar(finalVector, cards);

        // 5️⃣ DTO 반환
        return new CardSummaryDto(bestCard.getId(), bestCard.getName());
    }

    @Transactional
    public Card create(CardCreateRequest req) {
        Card card = Card.builder()
                .name(req.getName())
                .description(req.getDescription())
                .strength(req.getStrength())
                .type(req.getCardType())
                .question(req.getQuestion())
                .build();
        Card saved = cardRepository.save(card);
        return saved;
    }



}

