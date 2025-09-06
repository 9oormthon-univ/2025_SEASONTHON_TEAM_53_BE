package taro.card;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CardCreateRequest {
    @Schema(example = "XXI")
    @NotBlank
    private String name;

    @Schema(example = "완성과 성취, 조화롭게 모든 것이 하나로 연결할 줄 아는 사람")
    @NotBlank private String description;

    @Schema(example = "조화로운")
    @NotBlank private String strength;

    @Schema(example = "POST")
    @NotNull
    private CardType cardType;

    @Schema(example = "어떤 일을 끝까지 해내며 성취감을 느꼈던 순간은 언제였나요?")
    @NotBlank private String question;
}