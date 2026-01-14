package aded.first_web_api.resume.dto;

import org.springframework.web.multipart.MultipartFile;

public record ResumeReviewWithJobRequest(
        MultipartFile resumeFile,
        String resumeText,
        String jobDescription
) {}
