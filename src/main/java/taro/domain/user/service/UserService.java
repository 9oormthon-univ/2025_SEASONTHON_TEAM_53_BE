package taro.domain.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import taro.domain.JobCategory;
import taro.domain.Personality;
import taro.domain.user.dto.TutorialRequest;
import taro.domain.user.dto.UserResponse;
import taro.domain.User;
import taro.repository.UserRepository;
import taro.service.EmbeddingService;

import java.util.Arrays;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final EmbeddingService embeddingService;
    
    // 금칙어 목록
    private static final List<String> FORBIDDEN_NICKNAMES = Arrays.asList("admin", "root", "manager");

    @Transactional(readOnly = true)
    public UserResponse getUserInfo(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        return UserResponse.from(user);
    }
    
    @Transactional
    public UserResponse saveTutorial(Long userId, TutorialRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        // 닉네임 금칙어 검사
        if (FORBIDDEN_NICKNAMES.stream().anyMatch(request.getNickname().toLowerCase()::contains)) {
            throw new RuntimeException("사용할 수 없는 닉네임입니다.");
        }
        
        // 닉네임 중복 검사 (본인 제외)
        if (userRepository.existsByNicknameAndIdNot(request.getNickname(), userId)) {
            throw new RuntimeException("이미 사용중인 닉네임입니다.");
        }
        
        // 튜토리얼 정보 업데이트
        user.setNickname(request.getNickname());
        user.setJobCategory(request.getJobCategory());
        user.setPersonality(request.getPersonality());
        user.setTutorialCompleted(true);
        
        // 임베딩 처리
        saveTutorialInput(user, request.getJobCategory(), request.getPersonality());
        
        User savedUser = userRepository.save(user);
        log.info("Tutorial saved for user: {}", userId);
        
        return UserResponse.from(savedUser);
    }
    
    /**
     * 튜토리얼 입력 정보를 임베딩으로 변환하여 저장
     * @param user 사용자 엔티티
     * @param jobCategory 직업 분야
     * @param personality 성향
     */
    private void saveTutorialInput(User user, JobCategory jobCategory, Personality personality) {
        // Enum → 문자열로 변환 후 임베딩
        String text = "JobCategory: " + jobCategory.name() + ", Personality: " + personality.name();
        float[] embedding = embeddingService.embedText(text);
        user.setTutorialEmbedding(embedding);
        log.debug("Tutorial embedding saved for user: {}", user.getId());
    }
}
