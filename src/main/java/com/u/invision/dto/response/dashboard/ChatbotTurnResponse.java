package com.u.invision.dto.response.dashboard;

public record ChatbotTurnResponse(
		String dimension,
		Integer questionId,
		String questionType,
		String questionText,
		String answerText) {}
