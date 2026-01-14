package aded.first_web_api.resume.controller;

import java.io.IOException;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import aded.first_web_api.resume.dto.AtsReviewResponse;
import aded.first_web_api.resume.dto.ResumeReviewResponse;
import aded.first_web_api.resume.dto.ResumeReviewWithJobRequest;
import aded.first_web_api.resume.service.AtsReviewService;
import aded.first_web_api.resume.service.ResumeReviewService;

@RestController
@RequestMapping("/resumes")
public class ResumeController {

    private final ResumeReviewService resumeReviewService;
    private final AtsReviewService atsReviewService;

    public ResumeController(ResumeReviewService resumeReviewService, AtsReviewService atsReviewService) {
        this.resumeReviewService = resumeReviewService;
        this.atsReviewService = atsReviewService;
    }

    @PostMapping(value = "/{userId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResumeReviewResponse reviewResume(
        @PathVariable Long userId,
        @RequestPart(value = "file", required = false) MultipartFile file,
        @RequestPart(value = "text", required = false) String text
    ) throws Exception {

        if ((file == null || file.isEmpty()) && (text == null || text.isBlank())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Envie 'file' (PDF/DOCX) ou 'text'.");
        }

        return resumeReviewService.review(userId, file, text);
    }

    @PostMapping(
        value = "/{userId}/job",
        consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<AtsReviewResponse> reviewWithJob(
            @PathVariable("userId") Long userId,
            @RequestPart(value = "file", required = false) MultipartFile file,
            @RequestPart(value = "text", required = false) String text,
            @RequestPart("jobDescription") String jobDescription
    ) throws IOException {

        return ResponseEntity.ok(
            atsReviewService.review(userId, file, text, jobDescription)
        );
    }
}
