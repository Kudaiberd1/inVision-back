package com.u.invision.dto.response.dashboard;

public record CodingReviewResponse(
		PlatformReview codeforces,
		PlatformReview leetcode) {

	public record PlatformReview(
			String platform,
			String handle,
			Integer submissionsLastYear,
			Section proactiveness,
			SkillSection skill,
			Integer finalScore) {}

	public record Section(Integer score, String reason) {}

	public record SkillSection(Integer score, Object breakdown) {}
}

