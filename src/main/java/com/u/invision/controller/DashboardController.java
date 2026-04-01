package com.u.invision.controller;

import com.u.invision.dto.request.ApplicationStatusPatchRequest;
import com.u.invision.dto.response.ExtraActivityResponse;
import com.u.invision.dto.response.dashboard.CandidateDetailResponse;
import com.u.invision.dto.response.dashboard.ChatbotAnalysisResponse;
import com.u.invision.dto.response.dashboard.CandidateSummaryResponse;
import com.u.invision.dto.response.dashboard.CvReviewPanelResponse;
import com.u.invision.dto.response.dashboard.EssayReviewPanelResponse;
import com.u.invision.dto.response.dashboard.ScoreOverviewResponse;
import com.u.invision.service.DashboardService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

	private final DashboardService dashboardService;

	@GetMapping("/candidates/{id}")
	public CandidateDetailResponse getCandidate(@PathVariable("id") Long formId) {
		return dashboardService.getCandidateDetail(formId);
	}

	@GetMapping("/candidates/{id}/score-overview")
	public ScoreOverviewResponse getScoreOverview(@PathVariable("id") Long formId) {
		return dashboardService.getScoreOverview(formId);
	}

	@GetMapping("/candidates")
	public List<CandidateSummaryResponse> listCandidates() {
		return dashboardService.listCandidates();
	}

	@GetMapping("/candidates/{id}/cv-review")
	public CvReviewPanelResponse cvReview(@PathVariable("id") Long formId) {
		return dashboardService.getCvReview(formId);
	}

	@GetMapping("/candidates/{id}/essay-review")
	public EssayReviewPanelResponse essayReview(@PathVariable("id") Long formId) {
		return dashboardService.getEssayReview(formId);
	}

	@GetMapping("/candidates/{id}/chatbot-analysis")
	public ChatbotAnalysisResponse chatbotAnalysis(@PathVariable("id") Long formId) {
		return dashboardService.getChatbotAnalysis(formId);
	}

    @GetMapping("/candidates/{id}/extra")
    public ExtraActivityResponse getExtra(@PathVariable("id") Long formId) {
        return dashboardService.getExtra(formId);
    }

	@PatchMapping("/candidates/{id}/status")
	public ResponseEntity<Void> patchStatus(
			@PathVariable("id") Long formId, @Valid @RequestBody ApplicationStatusPatchRequest body) {
		dashboardService.patchApplicationStatus(formId, body);
		return ResponseEntity.noContent().build();
	}
}
