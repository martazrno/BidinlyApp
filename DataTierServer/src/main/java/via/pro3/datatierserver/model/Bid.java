package via.pro3.datatierserver.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "\"Bid\"")
public class Bid {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "\"Id\"")
    private Integer id;

    @Column(name = "\"BuyerId\"", nullable = false)
    @NotNull
    private Integer buyerId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "\"BuyerId\"", insertable = false, updatable = false,
            foreignKey = @ForeignKey(name = "fk_buyer"))
    private User buyer;

    @Column(name = "\"PropertyId\"", nullable = false)
    @NotNull
    private Integer propertyId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "\"PropertyId\"", insertable = false, updatable = false,
            foreignKey = @ForeignKey(name = "fk_property"))
    private Property property;

    @Column(name = "\"Amount\"", precision = 18, scale = 2, nullable = false)
    @NotNull
    private BigDecimal amount;

    @Column(name = "\"Deal\"")
    private String deal;

    @Column(name = "\"ExpiryDate\"", nullable = false)
    @NotNull
    private Instant expiryDate;

    @Column(name = "\"Status\"", length = 50)
    @Size(max = 50)
    private String status = "Pending";

    @Column(name = "\"Signature\"", columnDefinition = "TEXT")
    private String signature;

    public Bid() {
    }

    public Bid(Integer buyerId, Integer propertyId, BigDecimal amount, Instant expiryDate, String deal) {
        this.buyerId = buyerId;
        this.propertyId = propertyId;
        this.amount = amount;
        this.expiryDate = expiryDate;
        this.status = "Pending";
        this.deal = deal;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getBuyerId() {
        return buyerId;
    }

    public void setBuyerId(Integer buyerId) {
        this.buyerId = buyerId;
    }

    public User getBuyer() {
        return buyer;
    }

    public void setBuyer(User buyer) {
        this.buyer = buyer;
    }

    public Integer getPropertyId() {
        return propertyId;
    }

    public void setPropertyId(Integer propertyId) {
        this.propertyId = propertyId;
    }

    public Property getProperty() {
        return property;
    }

    public void setProperty(Property property) {
        this.property = property;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getDeal() {return deal;}

    public void setDeal(String deal) {this.deal = deal;}

    public Instant getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(Instant expiryDate) {
        this.expiryDate = expiryDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getSignature() { return signature; }
    public void setSignature(String signature) { this.signature = signature; }
}