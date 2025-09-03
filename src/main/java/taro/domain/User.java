package taro.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 카카오 식별자 고유 ID (중복 방지 위해 unique)
    @Column(nullable = false, unique = true)
    private String kakaoId;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false, length = 50)
    private String nickname; // 유저 닉네임

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
