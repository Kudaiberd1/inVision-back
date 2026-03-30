package com.u.invision.controller;

import com.u.invision.dto.interview.InterviewReplyRequest;
import com.u.invision.dto.interview.InterviewSessionResponse;
import com.u.invision.dto.interview.InterviewStartRequest;
import com.u.invision.service.InterviewService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/interview")
public class InterviewController {

	private final InterviewService interviewService;

	public InterviewController(InterviewService interviewService) {
		this.interviewService = interviewService;
	}

	@PostMapping("/start")
	public InterviewSessionResponse start(@Valid @RequestBody InterviewStartRequest request) {
		return interviewService.start(request);
	}

	@PostMapping("/{sessionId}/reply")
	public InterviewSessionResponse reply(
			@PathVariable String sessionId, @Valid @RequestBody InterviewReplyRequest request) {
		return interviewService.reply(sessionId, request);
	}
}
