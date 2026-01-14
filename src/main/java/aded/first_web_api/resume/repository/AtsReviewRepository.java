package aded.first_web_api.resume.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import aded.first_web_api.resume.model.AtsResumeReviewModel;

public interface AtsReviewRepository extends JpaRepository<AtsResumeReviewModel, UUID> {

    Optional<AtsResumeReviewModel> findByCombinedHash(String combinedHash);
}