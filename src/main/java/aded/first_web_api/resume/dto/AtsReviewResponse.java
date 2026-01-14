package aded.first_web_api.resume.dto;

import java.util.List;
import java.util.Map;

public record AtsReviewResponse(
        double overallScore,
        double keywordMatchRate,
        double hardSkillCoverage,
        double seniorityMatch,
        double titleAlignment,
        double atsReadability,
        double sectionCompleteness,

        Map<String, Object> atsMetrics,

        List<String> positives,
        List<String> negatives,
        List<String> improvements,
        String rewrittenSummary,
        List<String> rewrittenBullets
) {}
