package Cards.controller;


import Cards.feignClients.UserFeignClient;
import Cards.model.Card;
import Cards.model.RecordCard;
import Cards.service.CardsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users/{userId}/cards")
public class CardController {
    @Autowired
    private CardsService cardsService;

    @Autowired
    private UserFeignClient userFeignClient;

    @PostMapping
    public ResponseEntity<Card> createUserCard(@PathVariable String userId, @RequestBody RecordCard card,
                                               @RequestHeader("Authorization") String token){
        if (!existsUser(userId, token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        try {
            return ResponseEntity.ok(cardsService.createCard(userId, card));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{cardId}")
    public ResponseEntity<Card> getUserCard(@PathVariable String userId, @PathVariable String cardId,
                            @RequestHeader ("Authorization") String token){
        if (!existsUser(userId, token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return cardsService.getCard(userId, cardId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping()
    public ResponseEntity<List<Card>> getUserCards(@PathVariable String userId,
                                             @RequestHeader("Authorization") String token){
        if(!existsUser(userId, token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        List<Card> cardList = cardsService.getCardsPorUser(userId);
        return ResponseEntity.ok(cardList);

    }

    private boolean existsUser(String userId, String token) {
        return userFeignClient.obtenerUsuarioPorId(userId, token).getStatusCode().is2xxSuccessful();
    }

    @DeleteMapping("/{cardId}")
    public ResponseEntity<Card> deleteCardLogic(@PathVariable String userId, @PathVariable String cardId, @RequestHeader("Authorization") String token) {
        if (!existsUser(userId, token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(cardsService.deleteCardLogic(userId, cardId));
    }
}
