package taro.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import taro.repository.UserRepository;
import taro.embedding.EmbeddingService;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final EmbeddingService embeddingService;


    public void saveTutorialInput(Long userId, JobCategory jobCategory, Personality personality) {
        User user = userRepository.findById(userId).orElseThrow();

        user.setJobCategory(jobCategory);
        user.setPersonality(personality);

        // Enum → 문자열로 변환 후 임베딩
        String text = "JobCategory: " + jobCategory.name() + ", Personality: " + personality.name();

        // byte[] 형태로 임베딩 생성
        byte[] embeddingBytes = embeddingService.getEmbeddingBytes(text);

        // User 엔티티에 저장
        user.setPersonalEmbedding(embeddingBytes);

        userRepository.save(user);
    }
}
