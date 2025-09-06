package taro.domain.auth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import taro.domain.SocialProvider;
import taro.domain.User;
import taro.domain.UserRole;
import taro.domain.auth.dto.LoginRequest;
import taro.domain.auth.dto.RegisterRequest;
import taro.domain.auth.dto.TokenResponse;
import taro.repository.UserRepository;
import taro.security.jwt.JwtTokenProvider;

import java.util.Collections;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void register(RegisterRequest request) {
        // 아이디 중복 체크
        if (userRepository.existsByLoginId(request.getLoginId())) {
            throw new RuntimeException("이미 사용중인 아이디입니다.");
        }

        // 비밀번호 일치 확인
        if (!request.getPassword().equals(request.getPasswordConfirm())) {
            throw new RuntimeException("비밀번호가 일치하지 않습니다.");
        }

        // 사용자 생성
        User user = User.builder()
                .loginId(request.getLoginId())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .nickname(request.getNickname())
                .socialProvider(SocialProvider.LOCAL)
                .role(UserRole.ROLE_USER)
                .build();

        userRepository.save(user);
        log.info("New user registered: {}", request.getLoginId());
    }

    @Transactional
    public TokenResponse login(LoginRequest request) {
        // 사용자 조회
        User user = userRepository.findByLoginId(request.getLoginId())
                .orElseThrow(() -> new RuntimeException("아이디 또는 비밀번호가 올바르지 않습니다."));

        // 비밀번호 확인
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new RuntimeException("아이디 또는 비밀번호가 올바르지 않습니다.");
        }

        // Authentication 객체 생성 (loginId 사용)
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                user.getLoginId(),
                null,
                Collections.singleton(new SimpleGrantedAuthority(user.getRole().name()))
        );

        // JWT 토큰 생성
        String accessToken = jwtTokenProvider.createAccessToken(authentication);
        String refreshToken = jwtTokenProvider.createRefreshToken(authentication);

        // Refresh Token 저장
        user.updateRefreshToken(refreshToken);
        userRepository.save(user);

        return TokenResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .needsTutorial(!user.isTutorialCompleted())
                .build();
    }

    @Transactional
    public TokenResponse refreshToken(String refreshToken) {
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new RuntimeException("Invalid refresh token");
        }

        User user = userRepository.findByRefreshToken(refreshToken)
                .orElseThrow(() -> new RuntimeException("User not found with refresh token"));

        // loginId가 있으면 loginId 사용, 없으면 email 사용 (소셜 로그인 계정)
        String principal = user.getLoginId() != null ? user.getLoginId() : user.getEmail();
        
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                principal,
                null,
                Collections.singleton(new SimpleGrantedAuthority(user.getRole().name()))
        );

        String newAccessToken = jwtTokenProvider.createAccessToken(authentication);
        String newRefreshToken = jwtTokenProvider.createRefreshToken(authentication);

        user.updateRefreshToken(newRefreshToken);
        userRepository.save(user);

        return TokenResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .needsTutorial(!user.isTutorialCompleted())
                .build();
    }

    @Transactional
    public void logout(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        user.updateRefreshToken(null);
        userRepository.save(user);
    }

    @Transactional
    public void withdraw(Long userId) {
        userRepository.deleteById(userId);
    }
}
