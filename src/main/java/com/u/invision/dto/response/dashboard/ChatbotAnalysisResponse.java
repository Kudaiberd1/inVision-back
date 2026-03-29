package com.u.invision.dto.response.dashboard;

import java.util.List;

public record ChatbotAnalysisResponse(
		List<ChatbotTurnResponse> turns,
		CriteriaScoresResponse criteriaScores,
		int overallScore,
		String summary,
		CriteriaSummariesResponse criteriaSummaries) {}
