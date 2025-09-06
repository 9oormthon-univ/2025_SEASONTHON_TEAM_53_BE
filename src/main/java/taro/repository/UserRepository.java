package taro.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import taro.domain.SocialProvider;
import taro.domain.User;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    Optional<User> findByEmail(String email);
    
    Optional<User> findBySocialProviderAndSocialId(SocialProvider socialProvider, String socialId);
    
    Optional<User> findByRefreshToken(String refreshToken);
    
    boolean existsByEmail(String email);
    
    // 일반 로그인용 메서드 추가
    Optional<User> findByLoginId(String loginId);
    
    boolean existsByLoginId(String loginId);
    
    // 튜토리얼용 메서드 추가
    boolean existsByNicknameAndIdNot(String nickname, Long id);
}
