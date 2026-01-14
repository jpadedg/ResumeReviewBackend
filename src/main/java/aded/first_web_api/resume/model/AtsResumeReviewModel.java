package aded.first_web_api.resume.model;

import java.time.OffsetDateTime;
import java.util.UUID;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import com.fasterxml.jackson.databind.JsonNode;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "ats_reviews")
@Getter
@Setter
@NoArgsConstructor
public class AtsResumeReviewModel {

    @Id
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "finished_at")
    private OffsetDateTime finishedAt;

    @Column(nullable = false, length = 20)
    private String status;

    @Column(name = "error_message", columnDefinition = "text")
    private String errorMessage;

    @Column(name = "cost_coins", nullable = false)
    private Integer costCoins;

    @Enumerated(EnumType.STRING)
    @Column(name = "input_type", nullable = false, length = 20)
    private InputType inputType;

    @Column(name = "resume_hash", nullable = false, length = 64)
    private String resumeHash;

    @Column(name = "job_hash", nullable = false, length = 64)
    private String jobHash;

    @Column(name = "combined_hash", nullable = false, length = 64)
    private String combinedHash;

    @Column(name = "job_description", columnDefinition = "text", nullable = false)
    private String jobDescription;

    @Column(name = "overall_score")
    private Double overallScore;

    @Column(name = "keyword_match_rate")
    private Double keywordMatchRate;

    @Column(name = "hard_skill_coverage")
    private Double hardSkillCoverage;

    @Column(name = "seniority_match")
    private Double seniorityMatch;

    @Column(name = "title_alignment")
    private Double titleAlignment;

    @Column(name = "ats_readability")
    private Double atsReadability;

    @Column(name = "section_completeness")
    private Double sectionCompleteness;

    @Column(columnDefinition = "text")
    private String positives; 

    @Column(columnDefinition = "text")
    private String negatives; 

    @Column(columnDefinition = "text")
    private String improvements; 

    @Column(name = "rewritten_summary", columnDefinition = "text")
    private String rewrittenSummary;

    @Column(name = "rewritten_bullets", columnDefinition = "text")
    private String rewrittenBullets;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "ats_metrics", columnDefinition = "jsonb")
    private JsonNode atsMetrics;

    @PrePersist
    void prePersist() {
        if (id == null)
            id = UUID.randomUUID();
        if (createdAt == null)
            createdAt = OffsetDateTime.now();
        if (costCoins == null)
            costCoins = 0;
        if (status == null)
            status = "PENDING";
    }
}
