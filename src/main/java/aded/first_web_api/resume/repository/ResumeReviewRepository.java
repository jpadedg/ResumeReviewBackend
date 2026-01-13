package aded.first_web_api.resume.repository;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

import aded.first_web_api.resume.model.ResumeReview;

public interface ResumeReviewRepository extends JpaRepository<ResumeReview, UUID> {
    List<ResumeReview> findByUserIdOrderByCreatedAtDesc(Long userId);
}
