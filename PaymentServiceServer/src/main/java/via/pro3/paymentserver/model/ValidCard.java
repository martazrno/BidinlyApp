package via.pro3.paymentserver.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "valid_cards")
public class ValidCard {

    @Id
    @Column(name = "card_number", length = 19, nullable = false, unique = true)
    @NotNull
    @Size(max = 19)
    private String cardNumber;

    @Column(name = "expiration_date", columnDefinition = "CHAR(5)", nullable = false)
    @NotNull
    @Size(max = 5)
    private String expirationDate;

    @Column(name = "cvc", columnDefinition = "CHAR(4)", nullable = false)
    @NotNull
    @Size(max = 4)
    private String cvc;

    @Column(name = "name", length = 100, nullable = false)
    @NotNull
    @Size(max = 100)
    private String name;

    // Constructors
    public ValidCard() {
    }

    public ValidCard(String cardNumber, String expirationDate, String cvc, String name) {
        this.cardNumber = cardNumber;
        this.expirationDate = expirationDate;
        this.cvc = cvc;
        this.name = name;
    }

    // Getters and Setters
    public String getCardNumber() {
        return cardNumber;
    }

    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    public String getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(String expirationDate) {
        this.expirationDate = expirationDate;
    }

    public String getCvc() {
        return cvc;
    }

    public void setCvc(String cvc) {
        this.cvc = cvc;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}

