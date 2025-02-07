package Cards.model;

public record RecordCard(
        String userId,
        String expiration,
        String number,
        String name,
        String cvc
) {
}
