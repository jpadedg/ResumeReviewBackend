package aded.first_web_api.resume.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "resume_reviews")
@Getter @Setter
@NoArgsConstructor
public class ResumeReview {

    @Id
    private UUID id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false, length = 64)
    private String resumeHash;

    @Column(nullable = false)
    private InputType inputType;

    @Column(nullable = false)
    private Integer costCoins;

    private BigDecimal score;

    @Column(columnDefinition = "text")
    private String positives;

    @Column(columnDefinition = "text")
    private String negatives;

    @Column(columnDefinition = "text")
    private String improvements;

    @Column(columnDefinition = "text")
    private String rewrittenSummary;

    @Column(nullable = false)
    private String status;

    private String errorMessage;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    public ResumeReview(Long userId, String resumeHash, InputType inputType, Integer costCoins) {
        this.id = UUID.randomUUID();
        this.userId = userId;
        this.resumeHash = resumeHash;
        this.inputType = inputType;
        this.costCoins = costCoins;
        this.status = "FAILED";
    }

    @PrePersist
    void prePersist() {
        this.createdAt = Instant.now();
        if (this.id == null) this.id = UUID.randomUUID();
        if (this.status == null) this.status = "FAILED";
    }
}
