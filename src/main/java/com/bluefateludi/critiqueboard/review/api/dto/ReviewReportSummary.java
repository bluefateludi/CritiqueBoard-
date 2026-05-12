package com.bluefateludi.critiqueboard.review.api.dto;

import java.util.List;

public record ReviewReportSummary(
        int overallScore,
        String summary,
        List<String> strengths,
        List<String> weaknesses,
        List<String> actions,
        String finalMarkdown
) {
}
