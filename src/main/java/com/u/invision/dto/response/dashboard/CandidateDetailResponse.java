package com.u.invision.dto.response.dashboard;

import com.u.invision.entity.ApplicationStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public record CandidateDetailResponse(
		Long id,
		String fullName,
		String email,
		String phone,
		LocalDate dateOfBirth,
		String city,
		String schoolUniversity,
		Integer untScore,
		BigDecimal ielts,
		Integer toefl,
		String codeforces,
		String leetcode,
		String github,
		String linkedin,
		String fieldOfStudy,
		String cvUrl,
		String motivationEssayUrl,
		String videoUrl,
		Instant createdAt,
		ApplicationStatus status) {}

		