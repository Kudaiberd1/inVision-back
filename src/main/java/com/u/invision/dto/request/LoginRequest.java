package com.u.invision.dto.request;

import jakarta.validation.constraints.NotBlank;

/**
 * {@code username} in JSON is the value used to look up the user by email (MVP: send the email here).
 */
public record LoginRequest(@NotBlank String username, @NotBlank String password) {}
