package taro.user;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import taro.userdraw.UserDraw;

import java.time.LocalDateTime;
import java.util.List;

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

    @Column(nullable = false)
    private String role = "ROLE_USER";

    @Column(unique = true, nullable = false)
    private String kakaoEmail;

    private String nickname;


    @Enumerated(EnumType.STRING)
    private JobCategory jobCategory;

    @Enumerated(EnumType.STRING)
    private Personality personality;


    @Column(name = "refresh_token")
    private String refreshToken;

    // 1. 개인 특성 임베딩 (직무+성향)
    @Lob
    private byte[] personalEmbedding;

    // 2. 자소서 기반 임베딩
    @Lob
    private byte[] resumeEmbedding;

    // 3. 텍스트 기반 임베딩 (카드 입력 등)
    @Lob
    private byte[] textEmbedding;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<UserDraw> userDraws;

    public void updateRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
}
