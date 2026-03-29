package com.u.invision.dto.response.dashboard;

import java.time.Instant;

public record CandidateSummaryResponse(
		Long id,
		String fullName,
		String email,
		String fieldOfStudy,
		String programId,
		Instant submissionDate,
		Integer aiScore,
		CriteriaScoresResponse criteriaScores,
		String status) {}
