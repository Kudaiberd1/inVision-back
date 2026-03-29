package com.u.invision.dto.interview;

import jakarta.validation.constraints.NotBlank;

public record InterviewReplyRequest(@NotBlank String answer) {}
