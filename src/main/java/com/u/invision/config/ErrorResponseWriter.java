package com.u.invision.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

@Component
public class ErrorResponseWriter {

	private final ObjectMapper objectMapper = new ObjectMapper();

	public void writeJson(
			HttpServletRequest request,
			HttpServletResponse response,
			HttpStatus status,
			String error,
			String message)
			throws IOException {
		response.setStatus(status.value());
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		response.setCharacterEncoding("UTF-8");
		Map<String, Object> body = new LinkedHashMap<>();
		body.put("status", status.value());
		body.put("error", error);
		body.put("message", message);
		body.put("path", request.getRequestURI());
		objectMapper.writeValue(response.getWriter(), body);
	}
}
