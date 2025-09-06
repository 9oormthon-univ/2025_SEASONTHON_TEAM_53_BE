package taro.security.jwt;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;

    @BeforeEach
    void setUp() {
        // 테스트용 JWT secret 키
        String secretKey = "dGVzdC1qd3Qtc2VjcmV0LWtleS1mb3ItdGVzdGluZy1wdXJwb3NlLW9ubHktbWFrZS1pdC1sb25nLWVub3VnaA==";
        long tokenValidityInSeconds = 3600; // 1시간
        long refreshTokenValidityInSeconds = 604800; // 7일

        jwtTokenProvider = new JwtTokenProvider(secretKey, tokenValidityInSeconds, refreshTokenValidityInSeconds);
    }

    @Test
    @DisplayName("Access Token 생성 및 검증 테스트")
    void createAndValidateAccessToken() {
        // given
        String username = "test@example.com";
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                username,
                null,
                Collections.singleton(new SimpleGrantedAuthority("ROLE_USER"))
        );

        // when
        String accessToken = jwtTokenProvider.createAccessToken(authentication);

        // then
        assertThat(accessToken).isNotNull();
        assertThat(jwtTokenProvider.validateToken(accessToken)).isTrue();
        assertThat(jwtTokenProvider.getUsername(accessToken)).isEqualTo(username);
    }

    @Test
    @DisplayName("Refresh Token 생성 및 검증 테스트")
    void createAndValidateRefreshToken() {
        // given
        String username = "test@example.com";
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                username,
                null,
                Collections.singleton(new SimpleGrantedAuthority("ROLE_USER"))
        );

        // when
        String refreshToken = jwtTokenProvider.createRefreshToken(authentication);

        // then
        assertThat(refreshToken).isNotNull();
        assertThat(jwtTokenProvider.validateToken(refreshToken)).isTrue();
        assertThat(jwtTokenProvider.getUsername(refreshToken)).isEqualTo(username);
    }

    @Test
    @DisplayName("잘못된 토큰 검증 실패 테스트")
    void validateInvalidToken() {
        // given
        String invalidToken = "invalid.token.here";

        // when
        boolean isValid = jwtTokenProvider.validateToken(invalidToken);

        // then
        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("토큰에서 Claims 추출 테스트")
    void getClaimsFromToken() {
        // given
        String username = "test@example.com";
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                username,
                null,
                Collections.singleton(new SimpleGrantedAuthority("ROLE_USER"))
        );
        String token = jwtTokenProvider.createAccessToken(authentication);

        // when
        var claims = jwtTokenProvider.getClaims(token);

        // then
        assertThat(claims).isNotNull();
        assertThat(claims.getSubject()).isEqualTo(username);
        assertThat(claims.get("auth")).isEqualTo("ROLE_USER");
    }
}
