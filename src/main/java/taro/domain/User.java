package taro.domain;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import taro.converter.FloatArrayConverter;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 일반 로그인용 아이디 (LOCAL 계정만 사용)
    @Column(unique = true)
    private String loginId;

    // 비밀번호 해시 (LOCAL 계정만 사용)
    private String passwordHash;

    @Column(unique = true)
    private String email;

    @Column(nullable = false)
    private String nickname;

    private String profileImageUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SocialProvider socialProvider;

    @Column(unique = true)
    private String socialId; // 소셜 로그인 고유 ID (카카오 등)

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role;

    @Column(name = "refresh_token")
    private String refreshToken;

    // 튜토리얼 관련 필드 추가
    @Column(name = "job_category")
    @Enumerated(EnumType.STRING)
    private JobCategory jobCategory;

    @Column(name = "personality")
    @Enumerated(EnumType.STRING)
    private Personality personality;

    @Column(name = "tutorial_completed", nullable = false)
    @Builder.Default
    private boolean tutorialCompleted = false;

    // 튜토리얼 임베딩 정보 (추후 AI 기능용)
    @Convert(converter = FloatArrayConverter.class)
    @Column(name = "tutorial_embedding", columnDefinition = "TEXT")
    private float[] tutorialEmbedding;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public void updateRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
}
