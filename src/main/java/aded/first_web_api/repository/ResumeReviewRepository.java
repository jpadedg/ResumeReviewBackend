package aded.first_web_api.repository;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import aded.first_web_api.model.ResumeReview;

public interface ResumeReviewRepository extends JpaRepository<ResumeReview, UUID> {
    List<ResumeReview> findByUserIdOrderByCreatedAtDesc(Long userId);
}
