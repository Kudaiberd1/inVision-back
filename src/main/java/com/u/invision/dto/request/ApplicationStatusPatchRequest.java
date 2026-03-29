package com.u.invision.dto.request;

import jakarta.validation.constraints.NotBlank;

public record ApplicationStatusPatchRequest(@NotBlank String status) {}
