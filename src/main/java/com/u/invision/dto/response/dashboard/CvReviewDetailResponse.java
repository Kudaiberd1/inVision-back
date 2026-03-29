package com.u.invision.dto.response.dashboard;

import java.util.List;

public record CvReviewDetailResponse(
		String summary, List<HighlightResponse> highlights, CriteriaScoresResponse criteriaScores, int overallScore) {}
