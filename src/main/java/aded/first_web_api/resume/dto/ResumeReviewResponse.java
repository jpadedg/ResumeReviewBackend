package aded.first_web_api.resume.dto;

import java.util.List;

public record ResumeReviewResponse(
        double score,
        List<String> positives,
        List<String> negatives,
        List<String> improvements,
        String rewrittenSummary
) {}
