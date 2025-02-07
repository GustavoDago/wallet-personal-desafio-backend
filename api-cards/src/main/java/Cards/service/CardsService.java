package Cards.service;


import Cards.model.Card;
import Cards.model.RecordCard;
import Cards.repository.CardRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CardsService {


    @Autowired
    private CardRepository cardRepository;

    public Card createCard(String userId, RecordCard card){

        Card nuevaCard = new Card();

        nuevaCard.setUserId(userId);
        nuevaCard.setExpiration(card.expiration());
        nuevaCard.setName(card.name());
        nuevaCard.setNumber(card.number());
        nuevaCard.setCvc(card.cvc());

        return cardRepository.save(nuevaCard);
    }


    public Optional<Card> getCard(String userId, String cardId) {
        // Buscar la tarjeta en la base de datos
        return cardRepository.findByUserIdAndId(userId, cardId);
    }

    public List<Card> getCardsPorUser(String userId) {
        return cardRepository.findAllByUserId(userId);
    }

    public Card deleteCardLogic(String userId, String cardId) {
        Optional<Card> card = getCard(userId, cardId);
        if (card.isPresent()) {
            Card cardToUpdate = card.get();
            cardToUpdate.setVigente(false);
            return cardRepository.save(cardToUpdate);
        } else {
            throw new RuntimeException("Tarjeta no encontrada");
        }
    }
}
