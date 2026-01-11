package aded.first_web_api.model;

import java.time.Instant;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "wallets")
@Getter @Setter
@NoArgsConstructor
public class Wallet {

    @Id
    @Column(name = "user_id")
    private Long userId;

    @Column(nullable = false)
    private Integer balance;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    public Wallet(Long userId, Integer balance) {
        this.userId = userId;
        this.balance = balance;
    }

    @PrePersist
    void prePersist() {
        var now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    void preUpdate() {
        this.updatedAt = Instant.now();
    }
}
