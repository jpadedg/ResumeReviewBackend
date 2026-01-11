package aded.first_web_api.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import aded.first_web_api.dto.ResumeReviewResponse;
import aded.first_web_api.service.ResumeReviewService;

@RestController
@RequestMapping("/resumes")
public class ResumeController {

    private final ResumeReviewService resumeReviewService;

    public ResumeController(ResumeReviewService resumeReviewService) {
        this.resumeReviewService = resumeReviewService;
    }

    @PostMapping(value = "/{userId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResumeReviewResponse reviewResume(
        @PathVariable Long userId,
        @RequestPart(required = false) MultipartFile file,
        @RequestPart(required = false) String text
    ) throws Exception {

        if ((file == null || file.isEmpty()) && (text == null || text.isBlank())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Envie 'file' (PDF/DOCX) ou 'text'.");
        }

        return resumeReviewService.review(userId, file, text);
    }
}
