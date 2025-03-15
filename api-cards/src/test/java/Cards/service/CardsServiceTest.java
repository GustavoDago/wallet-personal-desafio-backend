package Cards.service;

import Cards.model.Card;
import Cards.model.RecordCard;
import Cards.repository.CardRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CardsServiceTest {

    @Mock
    private CardRepository cardRepository;

    @InjectMocks
    private CardsService cardsService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void createCard_ShouldSaveCardAndReturn() {
        // Arrange
        String userId = "user123";
        RecordCard recordCard = new RecordCard("2025-12", "1234567890123456", "John Doe", "123");
        Card expectedCard = new Card();
        expectedCard.setUserId(userId);
        expectedCard.setExpiration(recordCard.expiration());
        expectedCard.setName(recordCard.name());
        expectedCard.setNumber(recordCard.number());
        expectedCard.setCvc(recordCard.cvc());


        when(cardRepository.save(any(Card.class))).thenReturn(expectedCard);

        // Act
        Card actualCard = cardsService.createCard(userId, recordCard);

        // Assert
        assertNotNull(actualCard);
        assertEquals(expectedCard.getUserId(), actualCard.getUserId());
        assertEquals(expectedCard.getExpiration(), actualCard.getExpiration());
        assertEquals(expectedCard.getName(), actualCard.getName());
        assertEquals(expectedCard.getNumber(), actualCard.getNumber());
        assertEquals(expectedCard.getCvc(), actualCard.getCvc());
        verify(cardRepository, times(1)).save(any(Card.class));
    }

    @Test
    void getCard_ShouldReturnCard_WhenCardExists() {
        // Arrange
        String userId = "user123";
        String cardId = "card456";
        Card expectedCard = new Card();
        expectedCard.setId(cardId);
        expectedCard.setUserId(userId);

        when(cardRepository.findByUserIdAndId(userId, cardId)).thenReturn(Optional.of(expectedCard));

        // Act
        Optional<Card> actualCard = cardsService.getCard(userId, cardId);

        // Assert
        assertTrue(actualCard.isPresent());
        assertEquals(expectedCard, actualCard.get());
        verify(cardRepository, times(1)).findByUserIdAndId(userId, cardId);
    }

    @Test
    void getCard_ShouldReturnEmptyOptional_WhenCardDoesNotExist() {
        // Arrange
        String userId = "user123";
        String cardId = "card456";

        when(cardRepository.findByUserIdAndId(userId, cardId)).thenReturn(Optional.empty());

        // Act
        Optional<Card> actualCard = cardsService.getCard(userId, cardId);

        // Assert
        assertFalse(actualCard.isPresent());
        verify(cardRepository, times(1)).findByUserIdAndId(userId, cardId);
    }

    @Test
    void getCardsPorUser_ShouldReturnListOfCards() {
        // Arrange
        String userId = "user123";
        Card card1 = new Card();
        card1.setId("card1");
        card1.setUserId(userId);
        Card card2 = new Card();
        card2.setId("card2");
        card2.setUserId(userId);
        List<Card> expectedCards = List.of(card1, card2);

        when(cardRepository.findAllByUserId(userId)).thenReturn(expectedCards);

        // Act
        List<Card> actualCards = cardsService.getCardsPorUser(userId);

        // Assert
        assertEquals(expectedCards.size(), actualCards.size());
        assertEquals(expectedCards, actualCards);
        verify(cardRepository, times(1)).findAllByUserId(userId);
    }

    @Test
    void getCardsPorUser_ShouldReturnEmptyList_WhenNoCardsForUser() {
        // Arrange
        String userId = "user123";

        when(cardRepository.findAllByUserId(userId)).thenReturn(List.of());

        // Act
        List<Card> actualCards = cardsService.getCardsPorUser(userId);

        // Assert
        assertTrue(actualCards.isEmpty());
        verify(cardRepository, times(1)).findAllByUserId(userId);
    }


    @Test
    void deleteCardLogic_ShouldSetVigenteToFalse_WhenCardExists() {
        // Arrange
        String userId = "user123";
        String cardId = "card456";
        Card existingCard = new Card();
        existingCard.setId(cardId);
        existingCard.setUserId(userId);
        existingCard.setVigente(true);

        Card updatedCard = new Card();
        updatedCard.setId(cardId);
        updatedCard.setUserId(userId);
        updatedCard.setVigente(false);

        when(cardRepository.findByUserIdAndId(userId, cardId)).thenReturn(Optional.of(existingCard));
        when(cardRepository.save(any(Card.class))).thenReturn(updatedCard);

        // Act
        Card resultCard = cardsService.deleteCardLogic(userId, cardId);

        // Assert
        assertFalse(resultCard.isVigente());
        verify(cardRepository, times(1)).findByUserIdAndId(userId, cardId);
        verify(cardRepository, times(1)).save(existingCard);
    }

    @Test
    void deleteCardLogic_ShouldThrowException_WhenCardDoesNotExist() {
        // Arrange
        String userId = "user123";
        String cardId = "card456";

        when(cardRepository.findByUserIdAndId(userId, cardId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> cardsService.deleteCardLogic(userId, cardId));
        verify(cardRepository, times(1)).findByUserIdAndId(userId, cardId);
        verify(cardRepository, never()).save(any(Card.class));
    }
}