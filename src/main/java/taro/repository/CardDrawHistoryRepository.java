package taro.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import taro.domain.CardDrawHistory;
import taro.domain.User;

import java.time.LocalDate;
import java.util.Optional;

public interface CardDrawHistoryRepository extends JpaRepository<CardDrawHistory, Long> {
    Optional<CardDrawHistory> findByUserAndDrawDate(User user, LocalDate drawdate);
}
