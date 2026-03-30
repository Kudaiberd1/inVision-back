package com.u.invision.service;

import com.u.invision.dto.interview.InterviewReplyRequest;
import com.u.invision.dto.interview.InterviewSessionResponse;
import com.u.invision.dto.interview.InterviewStartRequest;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.UriUtils;

import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class InterviewService {

	private static final Set<String> ALLOWED_STAGES = Set.of("school", "university", "unknown");

	private final RestTemplate restTemplate;
	private final String interviewBase;
	private final InterviewResultPersistenceService interviewResultPersistenceService;

	public InterviewService(
			@Qualifier("aiEvaluatorRestTemplate") RestTemplate restTemplate,
			@Value("${ai.evaluator.base-url:http://127.0.0.1:8000}") String baseUrl,
			InterviewResultPersistenceService interviewResultPersistenceService) {
		this.restTemplate = restTemplate;
		this.interviewResultPersistenceService = interviewResultPersistenceService;
		String base = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
		this.interviewBase = base + "/interview";
	}

	public InterviewSessionResponse start(InterviewStartRequest request) {
		String stage = normalizeCandidateStage(request.candidateStage());
		if (stage == null) {
			throw new ResponseStatusException(
					HttpStatus.BAD_REQUEST,
					"candidate_stage must be one of: school, university, unknown "
							+ "(aliases: college-university, college, high-school → mapped automatically)");
		}
		InterviewStartRequest outbound = new InterviewStartRequest(request.candidateId(), stage);
		try {
			InterviewSessionResponse body =
					restTemplate.postForObject(interviewBase + "/start", outbound, InterviewSessionResponse.class);
			if (body == null) {
				throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Interview API returned empty body");
			}
			return body;
		} catch (ResponseStatusException e) {
			throw e;
		} catch (HttpStatusCodeException e) {
			String detail = e.getResponseBodyAsString();
			throw new ResponseStatusException(
					HttpStatus.BAD_GATEWAY,
					"Interview start HTTP " + e.getStatusCode().value() + ": " + (detail.isBlank() ? e.getMessage() : detail),
					e);
		} catch (Exception e) {
			throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Interview start failed: " + e.getMessage(), e);
		}
	}

	static String normalizeCandidateStage(String raw) {
		if (raw == null || raw.isBlank()) {
			return null;
		}
		String s = raw.trim().toLowerCase(Locale.ROOT).replace('_', '-');
		if (ALLOWED_STAGES.contains(s)) {
			return s;
		}
		return switch (s) {
			case "high-school", "highschool", "secondary" -> "school";
			case "college-university", "college", "uni", "undergrad", "graduate", "higher-ed", "higher-education" ->
					"university";
			case "other", "not-say" -> "unknown";
			default -> null;
		};
	}

	public InterviewSessionResponse reply(String sessionId, InterviewReplyRequest request) {
		String sid = UriUtils.encodePathSegment(sessionId, StandardCharsets.UTF_8);
		String url = interviewBase + "/" + sid + "/reply";
		try {
			InterviewSessionResponse body = restTemplate.postForObject(url, request, InterviewSessionResponse.class);
			if (body == null) {
				throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Interview API returned empty body");
			}
			try {
				interviewResultPersistenceService.saveFinalIfCompleted(body);
			} catch (Exception ex) {
				log.warn("Interview completed but DB save failed sessionId={}: {}", body.sessionId, ex.toString());
			}
			return body;
		} catch (ResponseStatusException e) {
			throw e;
		} catch (HttpStatusCodeException e) {
			String detail = e.getResponseBodyAsString();
			throw new ResponseStatusException(
					HttpStatus.BAD_GATEWAY,
					"Interview reply HTTP " + e.getStatusCode().value() + ": " + (detail.isBlank() ? e.getMessage() : detail),
					e);
		} catch (Exception e) {
			throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Interview reply failed: " + e.getMessage(), e);
		}
	}
}
