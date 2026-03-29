package com.u.invision.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

@Component
public class JsonAccessDeniedHandler implements AccessDeniedHandler {

	private final ErrorResponseWriter errorResponseWriter;

	public JsonAccessDeniedHandler(ErrorResponseWriter errorResponseWriter) {
		this.errorResponseWriter = errorResponseWriter;
	}

	@Override
	public void handle(
			HttpServletRequest request,
			HttpServletResponse response,
			AccessDeniedException accessDeniedException)
			throws IOException {
		String message =
				accessDeniedException != null && accessDeniedException.getMessage() != null
						? accessDeniedException.getMessage()
						: "Access denied";
		errorResponseWriter.writeJson(request, response, HttpStatus.FORBIDDEN, "Forbidden", message);
	}
}
