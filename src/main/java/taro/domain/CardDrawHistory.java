package taro.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "user_card_history",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"user_id", "draw_date"})  //DB단에서 중복 방지
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CardDrawHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 어떤 유저가
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // 어떤 카드를
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "card_id", nullable = false)
    private Card card;

    // 언제 뽑았는지
    @Column(name = "draw_date", nullable = false)
    private LocalDate drawDate;
}

