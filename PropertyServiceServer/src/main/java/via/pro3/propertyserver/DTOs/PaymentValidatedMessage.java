package via.pro3.propertyserver.DTOs;

import com.fasterxml.jackson.annotation.JsonProperty;

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