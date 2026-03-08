package via.pro3.datatierserver.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "\"User\"")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "\"Id\"")
    private Integer id;

    @Column(name = "\"Username\"", length = 50, nullable = false, unique = true)
    @NotNull
    @Size(max = 50)
    private String username;

    @Column(name = "\"Password\"", length = 100, nullable = false)
    @NotNull
    @Size(max = 100)
    private String password;

    @Column(name = "\"RoleId\"", nullable = false)
    @NotNull
    private Integer roleId;

    @Column(name = "\"IsActive\"", nullable = false)
    @NotNull
    private Boolean isActive = true;

    @Column(name = "\"Email\"", length = 100, nullable = true)
    @Size(max = 100)
    private String email;

    @Column(name = "\"PublicKey\"", columnDefinition = "TEXT")
    private String publicKey;

    @Column(name = "\"PrivateKey\"", columnDefinition = "TEXT")
    private String privateKey;

    @Column(name = "\"Certificate\"", columnDefinition = "TEXT")
    private String certificate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "\"RoleId\"", insertable = false, updatable = false,
            foreignKey = @ForeignKey(name = "fk_role"))
    private Role role;

    // Constructors
    public User() {
    }

    public User(String username, String password, Integer roleId) {
        this.username = username;
        this.password = password;
        this.roleId = roleId;
        this.isActive = true;
    }

    public User(String username, String password, Integer roleId, String email) {
        this.username = username;
        this.password = password;
        this.roleId = roleId;
        this.isActive = true;
        this.email = email;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Integer getRoleId() {
        return roleId;
    }

    public void setRoleId(Integer roleId) {
        this.roleId = roleId;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPublicKey() { return publicKey; }
    public void setPublicKey(String publicKey) { this.publicKey = publicKey; }

    public String getPrivateKey() { return privateKey; }
    public void setPrivateKey(String privateKey) { this.privateKey = privateKey; }

    public String getCertificate() { return certificate; }
    public void setCertificate(String certificate) { this.certificate = certificate; }
}