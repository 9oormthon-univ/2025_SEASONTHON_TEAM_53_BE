package taro.card;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class CardSaveRequest {
    private Long cardId;
    private CardType cardType;
    String text;
}
