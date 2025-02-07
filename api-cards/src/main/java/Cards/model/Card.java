package Cards.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.Data;
import org.hibernate.annotations.UuidGenerator;

@Data
@Entity
public class Card {
    @Id
    @GeneratedValue
    @UuidGenerator
    private String id;
    @Column(name = "user_id")
    private String userId;
    private String expiration;
    private String number;
    private String name;
    private String cvc;
    private boolean vigente = true;
}
