package com.u.invision.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;

/**
 * JWT scheme is defined here; only operations annotated with {@code @SecurityRequirement(name =
 * "bearerAuth")} require a token. A global requirement would make Swagger send {@code Authorization}
 * on every call (e.g. multipart form), which is easy to misconfigure with stale tokens.
 */
@OpenAPIDefinition
@SecurityScheme(
		name = "bearerAuth",
		description = "JWT — obtain via POST /api/auth/login",
		scheme = "bearer",
		type = SecuritySchemeType.HTTP,
		bearerFormat = "JWT",
		in = SecuritySchemeIn.HEADER)
public class OpenApiConfig {}