package taro;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import taro.domain.SocialProvider;
import taro.domain.User;
import taro.domain.UserRole;
import taro.domain.auth.dto.TokenResponse;
import taro.domain.auth.service.AuthService;
import taro.repository.UserRepository;
import taro.security.jwt.JwtTokenProvider;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class CoreFunctionalityTest {

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuthService authService;

    @Test
    @DisplayName("JWT 토큰 생성 및 검증 - 핵심 기능 테스트")
    void testJwtTokenCreationAndValidation() {
        // Given: 인증 정보 생성
        String email = "test@example.com";
        Authentication auth = new UsernamePasswordAuthenticationToken(
                email,
                null,
                Collections.singleton(new SimpleGrantedAuthority("ROLE_USER"))
        );

        // When: Access Token과 Refresh Token 생성
        String accessToken = jwtTokenProvider.createAccessToken(auth);
        String refreshToken = jwtTokenProvider.createRefreshToken(auth);

        // Then: 토큰 검증
        assertThat(accessToken).isNotNull();
        assertThat(refreshToken).isNotNull();
        
        // 토큰 유효성 검증
        assertTrue(jwtTokenProvider.validateToken(accessToken));
        assertTrue(jwtTokenProvider.validateToken(refreshToken));
        
        // 토큰에서 사용자 정보 추출
        assertEquals(email, jwtTokenProvider.getUsername(accessToken));
        
        System.out.println("JWT 토큰 생성 및 검증 성공!");
        System.out.println("Access Token: " + accessToken.substring(0, 20) + "...");
        System.out.println("Refresh Token: " + refreshToken.substring(0, 20) + "...");
    }

    @Test
    @DisplayName("카카오 소셜 로그인 사용자 저장 - 핵심 기능 테스트")
    void testKakaoUserSaveAndRetrieve() {
        // Given: 카카오 로그인 사용자 정보
        User kakaoUser = User.builder()
                .email("kakao@test.com")
                .nickname("카카오유저")
                .socialProvider(SocialProvider.KAKAO)
                .socialId("kakao_123456")
                .role(UserRole.ROLE_USER)
                .profileImageUrl("http://kakao.image.url")
                .build();

        // When: 사용자 저장
        User savedUser = userRepository.save(kakaoUser);

        // Then: 저장된 사용자 검증
        assertThat(savedUser.getId()).isNotNull();
        assertThat(savedUser.getEmail()).isEqualTo("kakao@test.com");
        assertThat(savedUser.getSocialProvider()).isEqualTo(SocialProvider.KAKAO);
        
        // 카카오 소셜 ID로 조회
        User foundUser = userRepository.findBySocialProviderAndSocialId(
                SocialProvider.KAKAO, "kakao_123456"
        ).orElse(null);
        
        assertNotNull(foundUser);
        assertEquals("카카오유저", foundUser.getNickname());
        
        System.out.println("카카오 사용자 저장 및 조회 성공!");
        System.out.println("User ID: " + savedUser.getId());
        System.out.println("Email: " + savedUser.getEmail());
        System.out.println("Provider: " + savedUser.getSocialProvider());
    }

    @Test
    @DisplayName("Refresh Token으로 Access Token 갱신 - 핵심 기능 테스트")
    void testTokenRefresh() {
        // Given: 테스트 사용자 생성 및 초기 토큰 발급
        User user = User.builder()
                .email("refresh@test.com")
                .nickname("리프레시테스트")
                .socialProvider(SocialProvider.KAKAO)
                .socialId("refresh_test_id")
                .role(UserRole.ROLE_USER)
                .build();
        
        user = userRepository.save(user);
        
        Authentication auth = new UsernamePasswordAuthenticationToken(
                user.getEmail(),
                null,
                Collections.singleton(new SimpleGrantedAuthority(user.getRole().name()))
        );
        
        String initialAccessToken = jwtTokenProvider.createAccessToken(auth);
        String refreshToken = jwtTokenProvider.createRefreshToken(auth);
        
        // Refresh Token을 사용자에 저장
        user.updateRefreshToken(refreshToken);
        userRepository.save(user);

        // 토큰 생성 시간차를 위한 짧은 대기 (JWT는 초 단위로 생성됨)
        try {
            Thread.sleep(1100); // 1.1초 대기
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // When: Refresh Token으로 새 Access Token 발급
        TokenResponse newTokens = authService.refreshToken(refreshToken);

        // Then: 새로운 토큰 검증
        assertNotNull(newTokens);
        assertNotNull(newTokens.getAccessToken());
        assertNotNull(newTokens.getRefreshToken());
        
        // 새 Access Token이 유효한지 확인
        assertTrue(jwtTokenProvider.validateToken(newTokens.getAccessToken()));
        
        // 새 토큰이 이전 토큰과 다른지 확인
        assertNotEquals(initialAccessToken, newTokens.getAccessToken());
        
        System.out.println("토큰 갱신 성공!");
        System.out.println("Initial Access Token: " + initialAccessToken.substring(0, 20) + "...");
        System.out.println("New Access Token: " + newTokens.getAccessToken().substring(0, 20) + "...");
    }

    @Test
    @DisplayName("로그아웃 시 Refresh Token 제거 - 핵심 기능 테스트")
    void testLogoutRemovesRefreshToken() {
        // Given: 로그인된 사용자
        User user = User.builder()
                .email("logout@test.com")
                .nickname("로그아웃테스트")
                .socialProvider(SocialProvider.KAKAO)
                .socialId("logout_test_id")
                .role(UserRole.ROLE_USER)
                .refreshToken("test_refresh_token")
                .build();
        
        user = userRepository.save(user);
        Long userId = user.getId();

        // When: 로그아웃
        authService.logout(userId);

        // Then: Refresh Token이 제거되었는지 확인
        User loggedOutUser = userRepository.findById(userId).orElseThrow();
        assertNull(loggedOutUser.getRefreshToken());
        
        System.out.println("로그아웃 처리 성공!");
        System.out.println("User ID: " + userId);
        System.out.println("Refresh Token after logout: " + loggedOutUser.getRefreshToken());
    }

    @Test
    @DisplayName("전체 인증 플로우 통합 테스트")
    void testCompleteAuthenticationFlow() {
        System.out.println("\n========== 전체 인증 플로우 테스트 시작 ==========");
        
        // 1. 카카오 로그인 시뮬레이션
        User kakaoUser = User.builder()
                .email("complete@test.com")
                .nickname("통합테스트유저")
                .socialProvider(SocialProvider.KAKAO)
                .socialId("complete_test_id")
                .role(UserRole.ROLE_USER)
                .build();
        
        User savedUser = userRepository.save(kakaoUser);
        System.out.println("카카오 사용자 등록 완료: " + savedUser.getEmail());

        // 2. JWT 토큰 발급
        Authentication auth = new UsernamePasswordAuthenticationToken(
                savedUser.getEmail(),
                null,
                Collections.singleton(new SimpleGrantedAuthority(savedUser.getRole().name()))
        );
        
        String accessToken = jwtTokenProvider.createAccessToken(auth);
        String refreshToken = jwtTokenProvider.createRefreshToken(auth);
        
        savedUser.updateRefreshToken(refreshToken);
        userRepository.save(savedUser);
        System.out.println("JWT 토큰 발급 완료");

        // 3. Access Token 검증
        assertTrue(jwtTokenProvider.validateToken(accessToken));
        assertEquals(savedUser.getEmail(), jwtTokenProvider.getUsername(accessToken));
        System.out.println("Access Token 검증 성공");

        // 4. Token Refresh
        TokenResponse newTokens = authService.refreshToken(refreshToken);
        assertNotNull(newTokens);
        assertTrue(jwtTokenProvider.validateToken(newTokens.getAccessToken()));
        System.out.println("토큰 갱신 성공");

        // 5. 로그아웃
        authService.logout(savedUser.getId());
        User loggedOutUser = userRepository.findById(savedUser.getId()).orElseThrow();
        assertNull(loggedOutUser.getRefreshToken());
        System.out.println("로그아웃 성공");

        System.out.println("========== 전체 인증 플로우 테스트 완료 ==========\n");
    }
}
