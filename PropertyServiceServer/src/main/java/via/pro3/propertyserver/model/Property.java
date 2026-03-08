package via.pro3.propertyserver.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

@Entity
@Table(name = "\"Property\"")
public class Property {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "\"Id\"")
    private Integer id;

    @Column(name = "\"AgentId\"", nullable = false)
    @NotNull
    private Integer agentId;

    @Column(name = "\"Title\"", length = 2000, nullable = false)
    @NotNull
    @Size(max = 2000)
    private String title;

    @Column(name = "\"Address\"", length = 500)
    @Size(max = 500)
    private String address;

    @Column(name = "\"StartingPrice\"", precision = 18, scale = 2, nullable = false)
    @NotNull
    private BigDecimal startingPrice;

    @Column(name = "\"Bedrooms\"", nullable = false)
    @NotNull
    private Integer bedrooms;

    @Column(name = "\"Bathrooms\"", nullable = false)
    @NotNull
    private Integer bathrooms;

    @Column(name = "\"SizeInSquareMeters\"", nullable = false)
    @NotNull
    private Double sizeInSquareMeters;

    @Column(name = "\"Description\"", length = 5000)
    @Size(max = 5000)
    private String description;

    @Column(name = "\"Status\"", length = 50)
    @Size(max = 50)
    private String status = "Available";

    @Column(name = "\"CreationStatus\"", length = 50)
    @Size(max = 50)
    private String creationStatus = "Pending";

    @Column(name = "\"ImageUrl\"", columnDefinition = "TEXT")
    private String imageUrl;

    public Property() {
    }

    public Property(Integer agentId, String title, BigDecimal startingPrice,
                    Integer bedrooms, Integer bathrooms, Double sizeInSquareMeters, String creationStatus) {
        this.agentId = agentId;
        this.title = title;
        this.startingPrice = startingPrice;
        this.bedrooms = bedrooms;
        this.bathrooms = bathrooms;
        this.sizeInSquareMeters = sizeInSquareMeters;
        this.status = "Available";
        this.creationStatus = creationStatus;
        this.imageUrl = null;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getAgentId() {
        return agentId;
    }

    public void setAgentId(Integer agentId) {
        this.agentId = agentId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public BigDecimal getStartingPrice() {
        return startingPrice;
    }

    public void setStartingPrice(BigDecimal startingPrice) {
        this.startingPrice = startingPrice;
    }

    public Integer getBedrooms() {
        return bedrooms;
    }

    public void setBedrooms(Integer bedrooms) {
        this.bedrooms = bedrooms;
    }

    public Integer getBathrooms() {
        return bathrooms;
    }

    public void setBathrooms(Integer bathrooms) {
        this.bathrooms = bathrooms;
    }

    public Double getSizeInSquareMeters() {
        return sizeInSquareMeters;
    }

    public void setSizeInSquareMeters(Double sizeInSquareMeters) {
        this.sizeInSquareMeters = sizeInSquareMeters;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCreationStatus() {
        return creationStatus;
    }

    public void setCreationStatus(String status) {
        this.creationStatus = status;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}