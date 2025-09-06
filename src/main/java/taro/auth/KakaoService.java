package taro.auth;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import taro.embedding.EmbeddingService;
import taro.repository.UserRepository;
import taro.JwtTokenProvider;
import taro.user.User;

import java.util.Collections;

@Service
@RequiredArgsConstructor
public class KakaoService {

    private final UserRepository userRepository;
    private final JwtTokenProvider jwtProvider;
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final EmbeddingService embeddingService;

    @Value("${kakao.client-id}")
    private String kakaoClientId;

    @Value("${kakao.redirect-uri}")
    private String kakaoRedirectUri;

    @Transactional
    public KakaoLoginResponse handleKakaoLogin(String code) {
        String accessToken = getAccessTokenFromKakao(code);
        String kakaoEmail = getKakaoEmail(accessToken);

        if(kakaoEmail ==null){
            return new KakaoLoginResponse(false,null,null);
        }

        return userRepository.findByKakaoEmail(kakaoEmail)
                .map(user -> {
                    TokenDto token = generateTokenFor(user);
                    user.setRefreshToken(token.getRefreshToken());
                    return new KakaoLoginResponse(true, kakaoEmail, token);
                })
                .orElseGet(() -> new KakaoLoginResponse(false, kakaoEmail, null));
    }

    @Transactional
    public TokenDto completeSignup(SignUpRequest req) {
        if (userRepository.findByKakaoEmail(req.getKakaoEmail()).isPresent()) {
            throw new IllegalStateException("이미 가입된 사용자입니다.");
        }

        String role = req.getRole();
        if (role == null || role.isBlank()) {
            role = "ROLE_USER";
        }
        new SimpleGrantedAuthority(role);

        User user = User.builder()
                .nickname(req.getNickName())
                .kakaoEmail(req.getKakaoEmail())
                .role(role)
                .ie(req.getIe())
                .ns(req.getNs())
                .ft(req.getFt())
                .jp(req.getJp())
                .build();

        TokenDto token = generateTokenFor(user);
        user.setRefreshToken(token.getRefreshToken());
        userRepository.save(user);

        return token;
    }

    public TokenDto refreshToken(String refreshToken) {
        User user = userRepository.findByRefreshToken(refreshToken)
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 리프레시 토큰입니다."));

        TokenDto token = generateTokenFor(user);
        user.setRefreshToken(token.getRefreshToken());
        userRepository.save(user);

        return token;
    }

    public String getAccessTokenFromKakao(String code) {
        String url = "https://kauth.kakao.com/oauth/token";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        String body = "grant_type=authorization_code"
                + "&client_id=" + kakaoClientId
                + "&redirect_uri=" + kakaoRedirectUri
                + "&code=" + code;

        HttpEntity<String> request = new HttpEntity<>(body, headers);
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, request, String.class);

        System.out.println("카카오 응답 상태: " + response.getStatusCode());
        System.out.println("카카오 응답 바디: " + response.getBody());

        if (response.getStatusCode() != HttpStatus.OK) {
            throw new IllegalStateException("카카오 토큰 요청 실패: " + response.getStatusCode());
        }

        try {
            JsonNode json = objectMapper.readTree(response.getBody());
            JsonNode accessTokenNode = json.get("access_token");
            if (accessTokenNode == null) {
                throw new IllegalStateException("카카오 응답에 access_token 없음: " + response.getBody());
            }
            return accessTokenNode.asText();
        } catch (Exception e) {
            throw new IllegalStateException("카카오 토큰 파싱 실패", e);
        }
    }


    private String getKakaoId(String accessToken) {
        String url = "https://kapi.kakao.com/v2/user/me";

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        HttpEntity<Void> request = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, request, String.class);


        System.out.println("사용자 정보 요청 토큰: "+ accessToken);
        System.out.println("카카오 사용자 정보 응답 바디: "+ response.getBody());

        try {
            JsonNode json = objectMapper.readTree(response.getBody());
            return json.get("id").asText();
        } catch (Exception e) {
            throw new IllegalStateException("카카오 사용자 정보 파싱 실패", e);
        }
    }

    public String getKakaoEmail(String token) {
        String url = "https://kapi.kakao.com/v2/user/me";
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        HttpEntity<Void> request = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, request, String.class);

        System.out.println("사용자 정보 요청 토큰: " + token);
        System.out.println("카카오 사용자 정보 응답 바디: " + response.getBody());

        if (response.getBody() == null) {
            System.err.println("카카오 응답 바디가 null임. 이메일 조회 실패");
            return null; // 여기서 null 반환
        }

        try {
            JsonNode json = objectMapper.readTree(response.getBody());

            // 1순위: 이메일
            JsonNode account = json.get("kakao_account");
            if (account != null && account.has("email")) {
                return account.get("email").asText();
            }

            // 2순위: 닉네임
            if (account != null && account.has("profile") && account.get("profile").has("nickname")) {
                return account.get("profile").get("nickname").asText() + "@kakao-temp.local";
            }

            // 3순위: ID라도 넘기기
            if (json.has("id")) {
                return "kakao_" + json.get("id").asText() + "@kakao-temp.local";
            }

            return null;
        } catch (Exception e) {
            System.err.println("카카오 사용자 정보 파싱 실패: " + e.getMessage());
            return null;
        }
    }

    private TokenDto generateTokenFor(User user) {
        return jwtProvider.generateTokenDto(
                new UsernamePasswordAuthenticationToken(
                        user.getKakaoEmail(),
                        null,
                        Collections.singleton(new SimpleGrantedAuthority(user.getRole()))));
    }
}
