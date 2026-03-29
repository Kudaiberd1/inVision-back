package com.u.invision.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Step 1: applicant picks a program only. Creates a {@link com.u.invision.entity.Form} with status
 * {@code DRAFT} and other fields null until step 2 ({@code POST /api/forms/{id}/submit}).
 */
public record CreateDraftFormRequest(
		@NotBlank @Size(max = 512) String fieldOfStudy) {}
