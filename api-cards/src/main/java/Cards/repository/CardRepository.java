package Cards.repository;


import Cards.model.Card;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CardRepository extends JpaRepository<Card, String> {
    Optional<Card> findByUserIdAndId(String userId, String cardId);

    List<Card> findAllByUserId(String userId);
}
