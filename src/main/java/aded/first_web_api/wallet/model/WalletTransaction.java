package aded.first_web_api.wallet.model;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "wallet_transactions")
@Getter @Setter
@NoArgsConstructor
public class WalletTransaction {

    @Id
    private UUID id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private String type; // SIGNUP_BONUS, RESUME_REVIEW_DEBIT, REFUND

    @Column(nullable = false)
    private Integer amount; // +100, -25, +25

    private UUID referenceId;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    public WalletTransaction(Long userId, String type, Integer amount, UUID referenceId) {
        this.id = UUID.randomUUID();
        this.userId = userId;
        this.type = type;
        this.amount = amount;
        this.referenceId = referenceId;
    }

    @PrePersist
    void prePersist() {
        this.createdAt = Instant.now();
        if (this.id == null) this.id = UUID.randomUUID();
    }
}
