package aded.first_web_api.resume.repository;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

import aded.first_web_api.resume.model.ResumeReviewModel;

public interface ResumeReviewRepository extends JpaRepository<ResumeReviewModel, UUID> {
    List<ResumeReviewModel> findByUserIdOrderByCreatedAtDesc(Long userId);
}
