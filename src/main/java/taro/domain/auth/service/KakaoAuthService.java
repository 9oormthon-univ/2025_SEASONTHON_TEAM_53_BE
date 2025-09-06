package taro.domain.auth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import taro.domain.SocialProvider;
import taro.domain.User;
import taro.domain.UserRole;
import taro.domain.auth.dto.TokenResponse;
import taro.repository.UserRepository;
import taro.security.jwt.JwtTokenProvider;

import java.util.Collections;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class KakaoAuthService {
    
    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final RestTemplate restTemplate = new RestTemplate();
    
    @Value("${spring.security.oauth2.client.registration.kakao.client-id}")
    private String clientId;
    
    @Value("${spring.security.oauth2.client.registration.kakao.client-secret}")
    private String clientSecret;
    
    @Value("${spring.security.oauth2.client.registration.kakao.redirect-uri}")
    private String redirectUri;
    
    @Transactional
    public TokenResponse processKakaoLogin(String code) {
        // 1. 카카오 액세스 토큰 획득
        String kakaoAccessToken = getKakaoAccessToken(code);
        
        // 2. 카카오 사용자 정보 조회
        Map<String, Object> kakaoUserInfo = getKakaoUserInfo(kakaoAccessToken);
        
        // 3. 사용자 정보 처리 (신규/기존)
        User user = processUserInfo(kakaoUserInfo);
        
        // 4. JWT 토큰 생성
        String principal = user.getEmail() != null ? user.getEmail() : user.getSocialId();
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                principal,
                null,
                Collections.singleton(new SimpleGrantedAuthority(user.getRole().name()))
        );
        
        String accessToken = jwtTokenProvider.createAccessToken(authentication);
        String refreshToken = jwtTokenProvider.createRefreshToken(authentication);
        
        // 5. Refresh Token 저장
        user.updateRefreshToken(refreshToken);
        userRepository.save(user);
        
        return TokenResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .needsTutorial(!user.isTutorialCompleted())
                .build();
    }
    
    private String getKakaoAccessToken(String code) {
        String tokenUrl = "https://kauth.kakao.com/oauth/token";
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("client_id", clientId);
        params.add("client_secret", clientSecret);
        params.add("redirect_uri", "http://localhost:8080/api/auth/kakao/callback");
        params.add("code", code);
        
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);
        
        ResponseEntity<Map> response = restTemplate.postForEntity(tokenUrl, request, Map.class);
        Map<String, Object> responseBody = response.getBody();
        
        if (responseBody == null || !responseBody.containsKey("access_token")) {
            throw new RuntimeException("Failed to get Kakao access token");
        }
        
        return (String) responseBody.get("access_token");
    }
    
    private Map<String, Object> getKakaoUserInfo(String accessToken) {
        String userInfoUrl = "https://kapi.kakao.com/v2/user/me";
        
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        
        HttpEntity<Void> request = new HttpEntity<>(headers);
        
        ResponseEntity<Map> response = restTemplate.exchange(
                userInfoUrl,
                HttpMethod.GET,
                request,
                Map.class
        );
        
        Map<String, Object> responseBody = response.getBody();
        if (responseBody == null) {
            throw new RuntimeException("Failed to get Kakao user info");
        }
        
        return responseBody;
    }
    
    private User processUserInfo(Map<String, Object> kakaoUserInfo) {
        String kakaoId = String.valueOf(kakaoUserInfo.get("id"));
        
        Map<String, Object> properties = (Map<String, Object>) kakaoUserInfo.get("properties");
        Map<String, Object> kakaoAccount = (Map<String, Object>) kakaoUserInfo.get("kakao_account");
        
        String nickname = properties != null ? (String) properties.get("nickname") : null;
        String profileImage = properties != null ? (String) properties.get("profile_image") : null;
        String email = kakaoAccount != null ? (String) kakaoAccount.get("email") : null;
        
        // 기존 사용자 조회
        User user = userRepository.findBySocialProviderAndSocialId(SocialProvider.KAKAO, kakaoId)
                .orElse(null);
        
        if (user == null) {
            // 신규 사용자 생성
            user = User.builder()
                    .socialProvider(SocialProvider.KAKAO)
                    .socialId(kakaoId)
                    .email(email)
                    .nickname(nickname != null ? nickname : "카카오사용자" + kakaoId.substring(0, 5))
                    .profileImageUrl(profileImage)
                    .role(UserRole.ROLE_USER)
                    .tutorialCompleted(false)
                    .build();
            
            user = userRepository.save(user);
            log.info("New Kakao user created: {}", kakaoId);
        } else {
            // 기존 사용자 정보 업데이트
            if (email != null && user.getEmail() == null) {
                user.setEmail(email);
            }
            if (profileImage != null) {
                user.setProfileImageUrl(profileImage);
            }
            user = userRepository.save(user);
            log.info("Existing Kakao user logged in: {}", kakaoId);
        }
        
        return user;
    }
}
