package taro.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import taro.userdraw.UserDraw;
import taro.user.User;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface UserDrawRepository extends JpaRepository<UserDraw, Long> {
    Optional<UserDraw> findByUserAndDrawDate(User user, LocalDate drawdate);

    boolean existsByUserIdAndDrawDate(Long userId, LocalDate today);


    // 카드별 뽑힌 횟수 조회
    @Query("SELECT ud.card.id AS cardId, COUNT(ud) AS count " +
            "FROM UserDraw ud " +
            "GROUP BY ud.card.id")
    List<Object[]> findCardCounts();

    List<UserDraw> findByUserId(Long userId);

}
