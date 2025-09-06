package taro.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import taro.domain.Card;

public interface CardRepository extends JpaRepository<Card, Long> {

    @Query(value = "SELECT * FROM card ORDER BY RANDOM() LIMIT 1", nativeQuery = true)
    Card findRandomCard();


}
