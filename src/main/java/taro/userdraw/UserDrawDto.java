package taro.userdraw;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
public class UserDrawDto {
    private Long drawId;
    private String cardName;
    private String ownText;
    private LocalDate drawDate;
    private Long cardCount;   // 카드가 뽑힌 횟수
}
