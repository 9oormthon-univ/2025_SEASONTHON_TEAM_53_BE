package taro.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import taro.domain.Card;
import taro.domain.CardDrawHistory;
import taro.domain.User;
import taro.repository.CardDrawHistoryRepository;
import taro.repository.CardRepository;
import taro.repository.UserRepository;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class CardService {

    private final CardRepository cardRepository;
    private final CardDrawHistoryRepository historyRepository;
    private final UserRepository userRepository;

    @Transactional
    public Card drawCard(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        LocalDate today = LocalDate.now();

        // 이미 오늘 뽑았는지 체크
        historyRepository.findByUserAndDrawDate(user, today)
                .ifPresent(h -> { throw new RuntimeException("오늘 이미 카드를 뽑았습니다."); });

        // 랜덤 카드 선택
        Card card = cardRepository.findRandomCard();

        // 뽑은 카드 기록 저장
        CardDrawHistory history = CardDrawHistory.builder()
                .user(user)
                .card(card)
                .drawDate(today)
                .build();

        historyRepository.save(history);

        return card;
    }
}
