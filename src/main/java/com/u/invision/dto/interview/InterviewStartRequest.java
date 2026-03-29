package com.u.invision.dto.interview;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;

public record InterviewStartRequest(
		@NotBlank
				@JsonProperty("candidate_id")
				@JsonAlias("candidateId")
				String candidateId,
		@NotBlank
				@JsonProperty("candidate_stage")
				@JsonAlias("candidateStage")
				String candidateStage) {}
