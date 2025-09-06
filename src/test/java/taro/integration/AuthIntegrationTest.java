package taro.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import taro.domain.SocialProvider;
import taro.domain.User;
import taro.domain.UserRole;
import taro.domain.auth.dto.TokenRefreshRequest;
import taro.repository.UserRepository;
import taro.security.jwt.JwtTokenProvider;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AuthIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private ObjectMapper objectMapper;

    private User testUser;
    private String validAccessToken;
    private String validRefreshToken;

    @BeforeEach
    void setUp() {
        // 테스트 사용자 생성
        testUser = User.builder()
                .email("test@example.com")
                .nickname("테스트유저")
                .socialProvider(SocialProvider.KAKAO)
                .socialId("test-kakao-id")
                .role(UserRole.ROLE_USER)
                .build();
        
        testUser = userRepository.save(testUser);

        // 테스트용 토큰 생성
        validAccessToken = "test-access-token"; // 실제로는 jwtTokenProvider로 생성
        validRefreshToken = "test-refresh-token";
        
        testUser.updateRefreshToken(validRefreshToken);
        userRepository.save(testUser);
    }

    @Test
    @DisplayName("인증된 사용자 정보 조회 테스트")
    @WithMockUser(username = "test@example.com", roles = "USER")
    void testGetUserInfo() throws Exception {
        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.nickname").value("테스트유저"));
    }

    @Test
    @DisplayName("인증되지 않은 사용자 접근 차단 테스트")
    void testUnauthorizedAccess() throws Exception {
        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("로그아웃 테스트")
    @WithMockUser(username = "test@example.com", roles = "USER")
    void testLogout() throws Exception {
        mockMvc.perform(post("/api/auth/logout"))
                .andExpect(status().isOk());

        // 로그아웃 후 refresh token이 null인지 확인
        User loggedOutUser = userRepository.findById(testUser.getId()).orElseThrow();
        assert(loggedOutUser.getRefreshToken() == null);
    }

    @Test
    @DisplayName("회원 탈퇴 테스트")
    @WithMockUser(username = "test@example.com", roles = "USER")
    void testWithdraw() throws Exception {
        Long userId = testUser.getId();
        
        mockMvc.perform(delete("/api/auth/withdraw"))
                .andExpect(status().isOk());

        // 사용자가 삭제되었는지 확인
        assert(!userRepository.existsById(userId));
    }

    @Test
    @DisplayName("Swagger UI 접근 테스트")
    void testSwaggerAccess() throws Exception {
        mockMvc.perform(get("/swagger-ui.html"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("정적 리소스 접근 테스트")
    void testStaticResourceAccess() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk());
    }
}
