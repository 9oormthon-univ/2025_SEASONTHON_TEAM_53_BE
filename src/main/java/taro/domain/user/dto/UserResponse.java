package taro.domain.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import taro.domain.JobCategory;
import taro.domain.Personality;
import taro.domain.SocialProvider;
import taro.domain.User;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    private Long id;
    private String loginId;
    private String email;
    private String nickname;
    private String profileImageUrl;
    private SocialProvider socialProvider;
    private JobCategory jobCategory;
    private Personality personality;
    private boolean tutorialCompleted;
    private LocalDateTime createdAt;

    public static UserResponse from(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .loginId(user.getLoginId())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .profileImageUrl(user.getProfileImageUrl())
                .socialProvider(user.getSocialProvider())
                .jobCategory(user.getJobCategory())
                .personality(user.getPersonality())
                .tutorialCompleted(user.isTutorialCompleted())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
