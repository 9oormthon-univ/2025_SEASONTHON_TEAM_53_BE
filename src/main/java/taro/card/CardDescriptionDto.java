package taro.card;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CardDescriptionDto {
    private Long id;
    private String name;
    private String description;
}
