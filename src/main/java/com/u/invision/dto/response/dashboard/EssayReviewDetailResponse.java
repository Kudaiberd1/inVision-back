package com.u.invision.dto.response.dashboard;

import java.util.List;

public record EssayReviewDetailResponse(
		String summary,
		List<HighlightResponse> highlights,
		boolean aiGeneratedFlag,
		Integer aiGeneratedConfidence,
		CriteriaScoresResponse criteriaScores,
		int overallScore) {}
