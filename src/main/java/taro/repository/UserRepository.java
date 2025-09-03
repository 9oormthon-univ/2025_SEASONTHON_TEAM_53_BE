package taro.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import taro.domain.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    // ID로 유저 조회
    Optional<User> findById(Long id);



}
