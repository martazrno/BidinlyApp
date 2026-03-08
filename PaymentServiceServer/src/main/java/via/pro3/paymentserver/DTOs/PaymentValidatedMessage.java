package via.pro3.paymentserver.DTOs;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;

public class PaymentValidatedMessage {
    
    @JsonProperty("propertyId")
    private Integer propertyId;
    
    @JsonProperty("bidId")
    private Integer bidId;
    
    @JsonProperty("buyerId")
    private Integer buyerId;
    
    @JsonProperty("agentId")
    private Integer agentId;
    
    @JsonProperty("cardNumber")
    private String cardNumber;
    
    @JsonProperty("timestamp")
    private Long timestamp;
    
    public PaymentValidatedMessage() {
        this.timestamp = Instant.now().getEpochSecond();
    }
    
    public PaymentValidatedMessage(Integer propertyId, Integer bidId, 
                                   Integer buyerId, Integer agentId, String cardNumber) {
        this.propertyId = propertyId;
        this.bidId = bidId;
        this.buyerId = buyerId;
        this.agentId = agentId;
        this.cardNumber = cardNumber;
        this.timestamp = Instant.now().getEpochSecond();
    }
    
    // Getters and Setters
    public Integer getPropertyId() {
        return propertyId;
    }
    
    public void setPropertyId(Integer propertyId) {
        this.propertyId = propertyId;
    }
    
    public Integer getBidId() {
        return bidId;
    }
    
    public void setBidId(Integer bidId) {
        this.bidId = bidId;
    }
    
    public Integer getBuyerId() {
        return buyerId;
    }
    
    public void setBuyerId(Integer buyerId) {
        this.buyerId = buyerId;
    }
    
    public Integer getAgentId() {
        return agentId;
    }
    
    public void setAgentId(Integer agentId) {
        this.agentId = agentId;
    }
    
    public String getCardNumber() {
        return cardNumber;
    }
    
    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }
    
    public Long getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }
}