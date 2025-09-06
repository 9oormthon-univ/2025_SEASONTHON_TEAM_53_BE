package taro.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import taro.user.SocialProvider;
import taro.user.User;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    


    Optional<User> findByRefreshToken(String refreshToken);
    

    Optional<User> findByKakaoEmail(String kakaoEmail);


}
