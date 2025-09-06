package taro.userdraw;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import taro.card.Card;
import taro.card.CardRepository;
import taro.embedding.EmbeddingService;
import taro.user.User;
import taro.repository.UserDrawRepository;
import taro.repository.UserRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserDrawService {

    private final UserDrawRepository userDrawRepository;
    private final UserRepository userRepository;
    private final CardRepository cardRepository;
    private final EmbeddingService embeddingService;

    public void saveUserDraw(Long userId, Long cardId, String ownText) {
        LocalDate today = LocalDate.now();

        User user = userRepository.findById(userId).orElseThrow();
        Card card = cardRepository.findById(cardId).orElseThrow();

        UserDraw userdraw = UserDraw.builder()
                .user(user)
                .card(card)
                .ownText(ownText)
                .drawDate(today)
                .build();

        List<UserDraw> draws = user.getUserDraws();
        StringBuilder combinedText = new StringBuilder();
        for (UserDraw draw : draws) {
            combinedText.append(draw.getOwnText()).append("\n");
        }

        byte[] embedding = embeddingService.getEmbeddingBytes(combinedText.toString());

        user.setTextEmbedding(embedding);
        userDrawRepository.save(userdraw);

    }

    public List<UserDrawDto> getUserDrawsWithCount(Long userId) {
        List<UserDraw> draws = userDrawRepository.findByUserId(userId);

        // 카드별 count 맵 생성
        Map<Long, Long> cardCountMap = userDrawRepository.findCardCounts().stream()
                .collect(Collectors.toMap(
                        r -> (Long) r[0],
                        r -> (Long) r[1]
                ));

        return draws.stream()
                .map(ud -> new UserDrawDto(
                        ud.getId(),
                        ud.getCard().getName(),
                        ud.getOwnText(),
                        ud.getDrawDate(),
                        cardCountMap.getOrDefault(ud.getCard().getId(), 0L)
                ))
                .toList();
}}
