package via.pro3.datatierserver.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Instant;

@Entity
@Table(name = "\"Notification\"")
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "\"Id\"")
    private Integer id;

    @Column(name = "\"BidId\"", nullable = false)
    @NotNull
    private Integer bidId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "\"BidId\"", insertable = false, updatable = false,
            foreignKey = @ForeignKey(name = "fk_notification_bid"))
    private Bid bid;

    @Column(name = "\"UserId\"", nullable = true)
    private Integer userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "\"UserId\"", insertable = false, updatable = false,
            foreignKey = @ForeignKey(name = "fk_notification_user"))
    private User user;

    @Column(name = "\"PropertyId\"", nullable = false)
    @NotNull
    private Integer propertyId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "\"PropertyId\"", insertable = false, updatable = false,
            foreignKey = @ForeignKey(name = "fk_notification_property"))
    private Property property;

    @Column(name = "\"Status\"", length = 50, nullable = true)
    @Size(max = 50)
    private String status;

    @Column(name = "\"Message\"", length = 2000, nullable = false)
    @NotNull
    @Size(max = 2000)
    private String message;

    @Column(name = "\"PropertyTitle\"", length = 500)
    @Size(max = 500)
    private String propertyTitle;

    @Column(name = "\"CreatedAt\"", nullable = false)
    @NotNull
    private Instant createdAt;

    @Column(name = "\"IsRead\"", nullable = false)
    @NotNull
    private Boolean isRead = false;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
        if (isRead == null) {
            isRead = false;
        }
    }

    public Notification() {
    }

    public Notification(Integer bidId, Integer buyerId, Integer propertyId, String status, String message) {
        this.bidId = bidId;
        this.userId = buyerId;
        this.propertyId = propertyId;
        this.status = status;
        this.message = message;
        this.createdAt = Instant.now();
        this.isRead = false;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getBidId() {
        return bidId;
    }

    public void setBidId(Integer bidId) {
        this.bidId = bidId;
    }

    public Bid getBid() {
        return bid;
    }

    public void setBid(Bid bid) {
        this.bid = bid;
    }

    public Integer getUserId() {return userId;}

    public void setUserId(Integer userId) {this.userId = userId;}

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getPropertyTitle() {
        return propertyTitle;
    }

    public void setPropertyTitle(String propertyTitle) {
        this.propertyTitle = propertyTitle;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Boolean getIsRead() {
        return isRead;
    }

    public void setIsRead(Boolean isRead) {
        this.isRead = isRead;
    }

}

