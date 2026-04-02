package com.u.invision.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.u.invision.dto.interview.InterviewSessionResponse;
import com.u.invision.dto.interview.InterviewSessionResponse.ConversationTurn;
import com.u.invision.dto.interview.InterviewSessionResponse.InterviewScoring;
import com.u.invision.dto.request.ApplicationStatusPatchRequest;
import com.u.invision.dto.response.ExtraActivityResponse;
import com.u.invision.dto.response.dashboard.CandidateDetailResponse;
import com.u.invision.dto.response.dashboard.ChatbotAnalysisResponse;
import com.u.invision.dto.response.dashboard.ChatbotTurnResponse;
import com.u.invision.dto.response.dashboard.CandidateSummaryResponse;
import com.u.invision.dto.response.dashboard.CodingReviewResponse;
import com.u.invision.dto.response.dashboard.CriteriaScoresResponse;
import com.u.invision.dto.response.dashboard.CriteriaSummariesResponse;
import com.u.invision.dto.response.dashboard.CvReviewDetailResponse;
import com.u.invision.dto.response.dashboard.CvReviewPanelResponse;
import com.u.invision.dto.response.dashboard.EssayReviewDetailResponse;
import com.u.invision.dto.response.dashboard.EssayReviewPanelResponse;
import com.u.invision.dto.response.dashboard.HighlightResponse;
import com.u.invision.dto.response.dashboard.ScoreOverviewResponse;
import com.u.invision.entity.ApplicationStatus;
import com.u.invision.entity.CVReview;
import com.u.invision.entity.CodingPlatformReview;
import com.u.invision.entity.EssayReview;
import com.u.invision.entity.Form;
import com.u.invision.entity.InterviewResult;
import com.u.invision.repository.CVReviewRepository;
import com.u.invision.repository.EssayReviewRepository;
import com.u.invision.repository.CodingPlatformReviewRepository;
import com.u.invision.repository.FormRepository;
import com.u.invision.repository.InterviewResultRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@Service
@RequiredArgsConstructor
public class DashboardService {

	private final FormRepository formRepository;
	private final CVReviewRepository cvReviewRepository;
	private final EssayReviewRepository essayReviewRepository;
	private final InterviewResultRepository interviewResultRepository;
	private final CodingPlatformReviewRepository codingPlatformReviewRepository;
	private final ApplicantPdfTexService applicantPdfTexService;
	private final ObjectMapper objectMapper = new ObjectMapper();

	@Transactional(readOnly = true)
	public List<CandidateSummaryResponse> listCandidates() {
		List<CandidateSummaryResponse> out = new ArrayList<>();
		for (Form form : formRepository.findAllForDashboard()) {
			CVReview cv = cvReviewRepository.findByForm_Id(form.getId()).orElse(null);
			EssayReview essay = essayReviewRepository.findByForm_Id(form.getId()).orElse(null);
			if (cv == null || essay == null) {
				continue;
			}
			InterviewResult chat = findInterview(form).orElse(null);
			CriteriaScoresResponse rowCriteria = averageRowCriteria(cv, essay);
			CodingPlatformReview cfReview =
					loadOrFetchPlatformReview(form, "codeforces", form.getCodeforces(), false);
			CodingPlatformReview lcReview =
					loadOrFetchPlatformReview(form, "leetcode", form.getLeetcode(), false);
			Integer overallScore = computeAggregateOverAllScore(
					cv,
					essay,
					chat,
					form.getUnt_score(),
					form.getIELTS() != null ? form.getIELTS().doubleValue() : null,
					form.getTOEFL(), cfReview != null ? cfReview.getFinalScore() : 0, lcReview != null ? lcReview.getFinalScore() : 0);
			out.add(new CandidateSummaryResponse(
					form.getId(),
					form.getFullName(),
					form.getEmail(),
					form.getFieldOfStudy(),
					programSlug(form.getFieldOfStudy()),
					form.getCreatedAt(),
					overallScore,
					rowCriteria,
					(form.getStatus() != null ? form.getStatus() : ApplicationStatus.PENDING)
							.name()
							.toLowerCase(Locale.ROOT)));
		}
		return out;
	}

	@Transactional(readOnly = true)
	public CandidateDetailResponse getCandidateDetail(Long formId) {
		Form form = loadForm(formId);
		return new CandidateDetailResponse(
				form.getId(),
				form.getFullName(),
				form.getEmail(),
				form.getPhone(),
				form.getDateOfBirth(),
				form.getCity(),
				form.getSchoolUniversity(),
				form.getUnt_score(),
				form.getIELTS(),
				form.getTOEFL(),
				form.getCodeforces(),
				form.getLeetcode(),
				form.getGithub(),
				form.getLinkedin(),
				form.getFieldOfStudy(),
				form.getCvUrl(),
				form.getMotivationEssayUrl(),
				form.getVideoUrl(),
				form.getCreatedAt(),
				form.getStatus() != null ? form.getStatus() : ApplicationStatus.PENDING);
	}

	@Transactional(readOnly = true)
	public ScoreOverviewResponse getScoreOverview(Long formId) {
		Form form = loadForm(formId);
		CVReview cv = cvReviewRepository.findByForm_Id(formId).orElse(null);
		EssayReview essay = essayReviewRepository.findByForm_Id(formId).orElse(null);
		InterviewResult chat = findInterview(form).orElse(null);

		double cvPoints = 0;
		double essayPoints = 0;
		double chatPoints = 0;
		double cvLeadershipPoints = 0;
		double cvProactivenessPoints = 0;
		double cvEnergyPoints = 0;
		double essayLeadershipPoints = 0;
		double essayProactivenessPoints = 0;
		double essayEnergyPoints = 0;
		double chatLeadershipPoints = 0;
		double chatProactivenessPoints = 0;
		double chatEnergyPoints = 0;
		int untPoints = 0;
		int ieltsPoints = 0;
		int toeflPoints = 0;

		if (cv != null
				&& cv.getLeadershipScore() != null
				&& cv.getProactivenessScore() != null
				&& cv.getEnergyScore() != null) {
			cvLeadershipPoints = cv.getLeadershipScore();
			cvProactivenessPoints = cv.getProactivenessScore();
			cvEnergyPoints = cv.getEnergyScore();
			cvPoints = (cvLeadershipPoints + cvProactivenessPoints + cvEnergyPoints) / 3.0;
		}
		if (essay != null
				&& essay.getLeadershipScore() != null
				&& essay.getProactivenessScore() != null
				&& essay.getEnergyScore() != null) {
			essayLeadershipPoints = essay.getLeadershipScore();
			essayProactivenessPoints = essay.getProactivenessScore();
			essayEnergyPoints = essay.getEnergyScore();
			essayPoints = (essayLeadershipPoints + essayProactivenessPoints + essayEnergyPoints) / 3.0;
		}
		if (chat != null
				&& chat.getLeadershipScore() != null
				&& chat.getProactivenessScore() != null
				&& chat.getEnergyScore() != null) {
			chatLeadershipPoints = chat.getLeadershipScore();
			chatProactivenessPoints = chat.getProactivenessScore();
			chatEnergyPoints = chat.getEnergyScore();
			chatPoints = (chatLeadershipPoints + chatProactivenessPoints + chatEnergyPoints) / 3.0;
		}

		Integer unt = form.getUnt_score();
		if (unt != null) {
			if (unt >= 120) {
				untPoints = 5;
			} else if (unt >= 110) {
				untPoints = 4;
			} else if (unt >= 100) {
				untPoints = 3;
			} else if (unt >= 90) {
				untPoints = 2;
			} else if (unt >= 80) {
				untPoints = 1;
			}
		}

		if (form.getIELTS() != null) {
			double ielts = form.getIELTS().doubleValue();
			if (ielts >= 8.0) {
				ieltsPoints = 5;
			} else if (ielts >= 7.5) {
				ieltsPoints = 4;
			} else if (ielts >= 7.0) {
				ieltsPoints = 3;
			} else if (ielts >= 6.5) {
				ieltsPoints = 2;
			} else if (ielts >= 6.0) {
				ieltsPoints = 1;
			}
		}

		Integer toefl = form.getTOEFL();
		if (toefl != null) {
			if (toefl >= 110) {
				toeflPoints = 5;
			} else if (toefl >= 100) {
				toeflPoints = 4;
			} else if (toefl >= 90) {
				toeflPoints = 3;
			} else if (toefl >= 80) {
				toeflPoints = 2;
			} else if (toefl >= 60) {
				toeflPoints = 1;
			}
		}

		CodingPlatformReview cfReview =
				loadOrFetchPlatformReview(form, "codeforces", form.getCodeforces(), false);
		CodingPlatformReview lcReview =
				loadOrFetchPlatformReview(form, "leetcode", form.getLeetcode(), false);

		double roundedCvPoints = Math.round(cvPoints);
		double roundedEssayPoints = Math.round(essayPoints);
		double roundedChatPoints = Math.round(chatPoints);
		double roundedCvLeadership = Math.round(cvLeadershipPoints);
		double roundedCvProactiveness = Math.round(cvProactivenessPoints);
		double roundedCvEnergy = Math.round(cvEnergyPoints);
		double roundedEssayLeadership = Math.round(essayLeadershipPoints);
		double roundedEssayProactiveness = Math.round(essayProactivenessPoints);
		double roundedEssayEnergy = Math.round(essayEnergyPoints);
		double roundedChatLeadership = Math.round(chatLeadershipPoints);
		double roundedChatProactiveness = Math.round(chatProactivenessPoints);
		double roundedChatEnergy = Math.round(chatEnergyPoints);

        Integer codeforcesScore = cfReview != null ? cfReview.getFinalScore() : null;
        Integer leetcodeScore = lcReview != null ? lcReview.getFinalScore() : null;

        int overall = (int) (roundedCvPoints
				+ roundedEssayPoints
				+ roundedChatPoints
				+ untPoints
				+ ieltsPoints
				+ toeflPoints
				+ (codeforcesScore != null ? codeforcesScore : 0)
				+ (leetcodeScore != null ? leetcodeScore : 0));

		return new ScoreOverviewResponse(
				overall,
				roundedCvPoints,
				roundedEssayPoints,
				roundedChatPoints,
				roundedCvLeadership,
				roundedCvProactiveness,
				roundedCvEnergy,
				roundedEssayLeadership,
				roundedEssayProactiveness,
				roundedEssayEnergy,
				roundedChatLeadership,
				roundedChatProactiveness,
				roundedChatEnergy,
				codeforcesScore,
				leetcodeScore,
				unt,
				form.getIELTS() != null ? form.getIELTS().doubleValue() : null,
				toefl,
				untPoints,
				ieltsPoints,
				toeflPoints);
	}

	@Transactional(readOnly = true)
	public CvReviewPanelResponse getCvReview(Long formId) {
		Form form = loadForm(formId);
		CVReview cv =
				cvReviewRepository.findByForm_Id(formId).orElseThrow(() -> notFound("CV review not found"));
		cv.getStrongEvidences().size();
		cv.getWeakEvidences().size();
		String tex = applicantPdfTexService.pdfPublicUrlToLatexVerbatim(form.getCvUrl());
		return new CvReviewPanelResponse(tex, form.getCvUrl(), mapCvDetail(cv));
	}

	@Transactional(readOnly = true)
	public EssayReviewPanelResponse getEssayReview(Long formId) {
		Form form = loadForm(formId);
		EssayReview essay = essayReviewRepository
				.findByForm_Id(formId)
				.orElseThrow(() -> notFound("Essay review not found"));
		essay.getStrongEvidences().size();
		essay.getWeakEvidences().size();
		String tex = applicantPdfTexService.pdfPublicUrlToLatexVerbatim(form.getMotivationEssayUrl());
		return new EssayReviewPanelResponse(tex, form.getMotivationEssayUrl(), mapEssayDetail(essay));
	}

	@Transactional(readOnly = true)
	public ChatbotAnalysisResponse getChatbotAnalysis(Long formId) {
		Form form = loadForm(formId);
		InterviewResult result = findInterview(form)
				.orElseThrow(() -> notFound("No completed interview linked to this candidate (match by full name or email)"));
		InterviewSessionResponse parsed = parseInterviewJson(result.getResponseJson()).orElse(null);
		return mapChatbot(result, parsed);
	}

	@Transactional
	public CodingReviewResponse getCodingReview(Long formId) {
		Form form = loadForm(formId);

		CodingPlatformReview cfReview =
				loadOrFetchPlatformReview(form, "codeforces", form.getCodeforces(), true);
		CodingPlatformReview lcReview =
				loadOrFetchPlatformReview(form, "leetcode", form.getLeetcode(), true);

		CodingReviewResponse.PlatformReview cfDto = toPlatformDto(cfReview, form.getCodeforces());
		CodingReviewResponse.PlatformReview lcDto = toPlatformDto(lcReview, form.getLeetcode());

		return new CodingReviewResponse(cfDto, lcDto);
	}

	@Transactional
	public void patchApplicationStatus(Long formId, ApplicationStatusPatchRequest request) {
		ApplicationStatus status;
		try {
			status = ApplicationStatus.valueOf(request.status().trim().toUpperCase(Locale.ROOT));
		} catch (IllegalArgumentException e) {
			throw new ResponseStatusException(
					HttpStatus.BAD_REQUEST, "status must be PENDING, ACCEPTED, or REJECTED");
		}
		if (status == ApplicationStatus.DRAFT) {
			throw new ResponseStatusException(
					HttpStatus.BAD_REQUEST, "status cannot be set to DRAFT via this endpoint");
		}
		Form form = formRepository.findById(formId).orElseThrow(() -> notFound("Application not found"));
		form.setStatus(status);
	}

    public ExtraActivityResponse getExtra(Long formId){
        var extra = formRepository.getExtraForForm(formId);
        ExtraActivityResponse extraActivity = ExtraActivityResponse.builder()
                .codeforces(extra.getCodeforces())
                .leetcode(extra.getLeetcode())
                .github(extra.getGithub())
                .linkedin(extra.getLinkedin())
                .build();

        return extraActivity;
    }

	private Form loadForm(Long formId) {
		return formRepository.findById(formId).orElseThrow(() -> notFound("Application not found"));
	}

	private static ResponseStatusException notFound(String message) {
		return new ResponseStatusException(HttpStatus.NOT_FOUND, message);
	}

	private Optional<InterviewResult> findInterview(Form form) {
		Optional<InterviewResult> byName =
				interviewResultRepository.findFirstByCandidateIdIgnoreCaseOrderByCompletedAtDesc(String.valueOf(form.getId()));
		if (byName.isPresent()) {
			return byName;
		}
		return interviewResultRepository.findFirstByCandidateIdIgnoreCaseOrderByCompletedAtDesc(form.getEmail());
	}

	private Optional<InterviewSessionResponse> parseInterviewJson(String json) {
		if (json == null || json.isBlank()) {
			return Optional.empty();
		}
		try {
			return Optional.of(objectMapper.readValue(json, InterviewSessionResponse.class));
		} catch (Exception e) {
			log.warn("Interview JSON parse failed: {}", e.getMessage());
			return Optional.empty();
		}
	}

	private static String programSlug(String field) {
		if (field == null || field.isBlank()) {
			return null;
		}
		return field.trim()
				.toLowerCase(Locale.ROOT)
				.replaceAll("[^a-z0-9]+", "-")
				.replaceAll("(^-|-$)", "");
	}

	private CodingPlatformReview loadOrFetchPlatformReview(
			Form form, String platform, String handle, boolean fetchIfMissing) {
		if (handle == null || handle.isBlank()) {
			return null;
		}

		CodingPlatformReview existing =
				codingPlatformReviewRepository.findByForm_IdAndPlatform(form.getId(), platform).orElse(null);
		if (existing != null || !fetchIfMissing) {
			return existing;
		}

		try {
			String url = "http://localhost:3000/review/" + platform + "/" + handle;
			org.springframework.web.client.RestTemplate rt = new org.springframework.web.client.RestTemplate();
			String json = rt.getForObject(url, String.class);
			if (json == null) {
				return null;
			}
			com.fasterxml.jackson.databind.JsonNode node = objectMapper.readTree(json);
			Integer finalScore = node.path("final_score").isMissingNode() ? null : node.path("final_score").asInt();

			CodingPlatformReview entity = new CodingPlatformReview();
			entity.setForm(form);
			entity.setPlatform(platform);
			entity.setHandle(handle);
			entity.setRawResponseJson(json);
			entity.setFinalScore(finalScore);
			entity.setCreatedAt(java.time.Instant.now());
			return codingPlatformReviewRepository.save(entity);
		} catch (Exception e) {
			log.warn("Failed to fetch coding platform review for {} {}: {}", platform, handle, e.getMessage());
			return null;
		}
	}

	private CodingReviewResponse.PlatformReview toPlatformDto(CodingPlatformReview review, String handle) {
		if (review == null) {
			return null;
		}
		try {
			com.fasterxml.jackson.databind.JsonNode root =
					objectMapper.readTree(review.getRawResponseJson());
			Integer submissionsLastYear = root.path("submissions_last_year").isMissingNode()
					? null
					: root.path("submissions_last_year").asInt();

			com.fasterxml.jackson.databind.JsonNode proNode = root.path("proactiveness");
			Integer proScore = proNode.path("score").isMissingNode() ? null : proNode.path("score").asInt();
			String proReason = proNode.path("reason").isMissingNode() ? null : proNode.path("reason").asText(null);

			com.fasterxml.jackson.databind.JsonNode skillNode = root.path("skill");
			Integer skillScore = skillNode.path("score").isMissingNode() ? null : skillNode.path("score").asInt();
			Object breakdown =
					skillNode.path("breakdown").isMissingNode()
							? null
							: objectMapper.convertValue(skillNode.path("breakdown"), Object.class);

			Integer finalScore = root.path("final_score").isMissingNode() ? null : root.path("final_score").asInt();

			return new CodingReviewResponse.PlatformReview(
					root.path("platform").asText(),
					handle,
					submissionsLastYear,
					new CodingReviewResponse.Section(proScore, proReason),
					new CodingReviewResponse.SkillSection(skillScore, breakdown),
					finalScore);
		} catch (Exception e) {
			log.warn("Failed to parse stored coding platform review JSON: {}", e.getMessage());
			return null;
		}
	}

	private static Integer computeAggregateOverAllScore(CVReview cv, EssayReview essay, InterviewResult chat, Integer unt, Double ielts, Integer toefl, Integer codeforcesScore, Integer leetcodeScore) {
        double cvPoints = 0;
        double essayPoints = 0;
        double chatPoints = 0;
        double cvLeadershipPoints = 0;
        double cvProactivenessPoints = 0;
        double cvEnergyPoints = 0;
        double essayLeadershipPoints = 0;
        double essayProactivenessPoints = 0;
        double essayEnergyPoints = 0;
        double chatLeadershipPoints = 0;
        double chatProactivenessPoints = 0;
        double chatEnergyPoints = 0;
        int untPoints = 0;
        int ieltsPoints = 0;
        int toeflPoints = 0;

        if (cv != null
				&& cv.getLeadershipScore() != null
				&& cv.getProactivenessScore() != null
				&& cv.getEnergyScore() != null) {
            cvLeadershipPoints = cv.getLeadershipScore();
            cvProactivenessPoints = cv.getProactivenessScore();
            cvEnergyPoints = cv.getEnergyScore();
            cvPoints = (cvLeadershipPoints + cvProactivenessPoints + cvEnergyPoints) / 3.0;
        }
        if (essay != null
				&& essay.getLeadershipScore() != null
				&& essay.getProactivenessScore() != null
				&& essay.getEnergyScore() != null) {
            essayLeadershipPoints = essay.getLeadershipScore();
            essayProactivenessPoints = essay.getProactivenessScore();
            essayEnergyPoints = essay.getEnergyScore();
            essayPoints = (essayLeadershipPoints + essayProactivenessPoints + essayEnergyPoints) / 3.0;
        }
        if (chat != null
				&& chat.getLeadershipScore() != null
				&& chat.getProactivenessScore() != null
				&& chat.getEnergyScore() != null) {
            chatLeadershipPoints = chat.getLeadershipScore();
            chatProactivenessPoints = chat.getProactivenessScore();
            chatEnergyPoints = chat.getEnergyScore();
            chatPoints = (chatLeadershipPoints + chatProactivenessPoints + chatEnergyPoints) / 3.0;
        }

        if (unt != null) {
            if (unt >= 120) {
                untPoints = 5;
            } else if (unt >= 110) {
                untPoints = 4;
            } else if (unt >= 100) {
                untPoints = 3;
            } else if (unt >= 90) {
                untPoints = 2;
            } else if (unt >= 80) {
                untPoints = 1;
            }
        }

        if (ielts != null) {
            if (ielts >= 8.0) {
                ieltsPoints = 5;
            } else if (ielts >= 7.5) {
                ieltsPoints = 4;
            } else if (ielts >= 7.0) {
                ieltsPoints = 3;
            } else if (ielts >= 6.5) {
                ieltsPoints = 2;
            } else if (ielts >= 6.0) {
                ieltsPoints = 1;
            }
        }

        if (toefl != null) {
            if (toefl >= 110) {
                toeflPoints = 5;
            } else if (toefl >= 100) {
                toeflPoints = 4;
            } else if (toefl >= 90) {
                toeflPoints = 3;
            } else if (toefl >= 80) {
                toeflPoints = 2;
            } else if (toefl >= 60) {
                toeflPoints = 1;
            }
        }

        double roundedCvPoints = Math.round(cvPoints);
        double roundedEssayPoints = Math.round(essayPoints);
        double roundedChatPoints = Math.round(chatPoints);

		int cf = codeforcesScore != null ? codeforcesScore : 0;
		int lc = leetcodeScore != null ? leetcodeScore : 0;

        int overall = (int) (roundedCvPoints + roundedEssayPoints + roundedChatPoints + untPoints + ieltsPoints + toeflPoints + cf + lc);

        return overall;
	}

	private static CriteriaScoresResponse averageRowCriteria(CVReview cv, EssayReview essay) {
		return new CriteriaScoresResponse(
				avgInt(cv.getLeadershipScore(), essay.getLeadershipScore()),
				avgInt(cv.getProactivenessScore(), essay.getProactivenessScore()),
				avgInt(cv.getEnergyScore(), essay.getEnergyScore()));
	}

	private static int avgInt(Integer a, Integer b) {
		if (a == null && b == null) {
			return 0;
		}
		if (a == null) {
			return b;
		}
		if (b == null) {
			return a;
		}
		return (int) Math.round((a + b) / 2.0);
	}

	private static CvReviewDetailResponse mapCvDetail(CVReview cv) {
		List<HighlightResponse> highlights = new ArrayList<>();
		List<String> strong = cv.getStrongEvidences();
		List<String> strongReasons = cv.getStrongEvidenceReasons();
		for (int i = 0; i < strong.size(); i++) {
			String line = strong.get(i);
			String reason =
					strongReasons != null && i < strongReasons.size() && strongReasons.get(i) != null
							? strongReasons.get(i)
							: "Strong evidence signal from automated CV review.";
			highlights.add(new HighlightResponse(
					line, reason, "positive"));
		}
		List<String> weak = cv.getWeakEvidences();
		List<String> weakReasons = cv.getWeakEvidenceReasons();
		for (int i = 0; i < weak.size(); i++) {
			String line = weak.get(i);
			String reason =
					weakReasons != null && i < weakReasons.size() && weakReasons.get(i) != null
							? weakReasons.get(i)
							: "Weak phrasing / low-specificity signal from automated CV review.";
			highlights.add(new HighlightResponse(
					line, reason, "warning"));
		}
		CriteriaScoresResponse cs = new CriteriaScoresResponse(
				cv.getLeadershipScore(), cv.getProactivenessScore(), cv.getEnergyScore());

		// Overall CV score: average of the three dimensions, rounded
		int overall = avgInt(
				cv.getLeadershipScore(),
				avgInt(cv.getProactivenessScore(), cv.getEnergyScore()));

		return new CvReviewDetailResponse(cv.getProfileSummary(), highlights, cs, overall);
	}

	private static EssayReviewDetailResponse mapEssayDetail(EssayReview essay) {
		List<HighlightResponse> highlights = new ArrayList<>();
		List<String> strong = essay.getStrongEvidences();
		List<String> strongReasons = essay.getStrongEvidenceReasons();
		for (int i = 0; i < strong.size(); i++) {
			String line = strong.get(i);
			String reason =
					strongReasons != null && i < strongReasons.size() && strongReasons.get(i) != null
							? strongReasons.get(i)
							: "Strong evidence signal from automated essay review.";
			highlights.add(new HighlightResponse(
					line, reason, "positive"));
		}
		List<String> weak = essay.getWeakEvidences();
		List<String> weakReasons = essay.getWeakEvidenceReasons();
		for (int i = 0; i < weak.size(); i++) {
			String line = weak.get(i);
			String reason =
					weakReasons != null && i < weakReasons.size() && weakReasons.get(i) != null
							? weakReasons.get(i)
							: "Weak phrasing signal from automated essay review.";
			highlights.add(new HighlightResponse(
					line, reason, "warning"));
		}
		boolean aiFlag = Boolean.TRUE.equals(essay.getPossibleAiGenerated());
		Integer confidence = aiFlag ? 75 : 15;
		CriteriaScoresResponse cs = new CriteriaScoresResponse(
				essay.getLeadershipScore(), essay.getProactivenessScore(), essay.getEnergyScore());
		int overall = avgInt(essay.getLeadershipScore(), avgInt(essay.getProactivenessScore(), essay.getEnergyScore()));
		return new EssayReviewDetailResponse(
				essay.getProfileSummary(), highlights, aiFlag, confidence, cs, overall);
	}

	private ChatbotAnalysisResponse mapChatbot(InterviewResult stored, InterviewSessionResponse parsed) {
		List<ChatbotTurnResponse> turns = new ArrayList<>();
		InterviewScoring sc = null;
		String summary = stored.getJurySessionSummary();

		if (parsed != null) {
			if (parsed.interview != null && parsed.interview.conversation != null) {
				for (ConversationTurn t : parsed.interview.conversation) {
					turns.add(new ChatbotTurnResponse(
							t.dimension,
							t.questionId,
							t.questionType,
							t.questionText,
							t.answerText));
				}
			}
			sc = parsed.scoring;
			if (sc == null && parsed.interview != null) {
				sc = parsed.interview.scoring;
			}
			if (parsed.jurySessionSummary != null && !parsed.jurySessionSummary.isBlank()) {
				summary = parsed.jurySessionSummary;
			} else if (parsed.interview != null
					&& parsed.interview.jurySessionSummary != null
					&& !parsed.interview.jurySessionSummary.isBlank()) {
				summary = parsed.interview.jurySessionSummary;
			}
			CriteriaSummariesResponse summ = buildSummaries(
					parsed.interview != null ? parsed.interview.conversation : List.of());
			CriteriaScoresResponse csDto = scoringToCriteria(sc, stored);
			int overall = overallChatbotScore(sc, stored);
			return new ChatbotAnalysisResponse(turns, csDto, overall, summary != null ? summary : "", summ);
		}

		CriteriaScoresResponse csDto = scoringToCriteria(null, stored);
		int overall = overallChatbotScore(null, stored);
		return new ChatbotAnalysisResponse(
				turns,
				csDto,
				overall,
				summary != null ? summary : "",
				new CriteriaSummariesResponse("", "", ""));
	}

	private static CriteriaSummariesResponse buildSummaries(List<ConversationTurn> conversation) {
		if (conversation == null) {
			conversation = List.of();
		}
		return new CriteriaSummariesResponse(
				lastDimensionFeedback(conversation, "leadership"),
				lastDimensionFeedback(conversation, "proactiveness"),
				lastDimensionFeedback(conversation, "energy"));
	}

	private static String lastDimensionFeedback(List<ConversationTurn> conversation, String dimension) {
		String dim = dimension.toLowerCase(Locale.ROOT);
		String last = "";
		for (ConversationTurn t : conversation) {
			if (t.dimension == null || !t.dimension.toLowerCase(Locale.ROOT).equals(dim)) {
				continue;
			}
			if (t.feedbackForJury == null) {
				continue;
			}
			if (t.feedbackForJury.weaknesses != null && !t.feedbackForJury.weaknesses.isBlank()) {
				last = t.feedbackForJury.weaknesses;
			} else if (t.feedbackForJury.strengths != null && !t.feedbackForJury.strengths.isBlank()) {
				last = t.feedbackForJury.strengths;
			}
		}
		return last;
	}

	private static CriteriaScoresResponse scoringToCriteria(InterviewScoring sc, InterviewResult stored) {
		if (sc != null) {
			return new CriteriaScoresResponse(
					toInt(sc.leadershipScore),
					toInt(sc.proactivenessScore),
					toInt(sc.energyScore));
		}
		return new CriteriaScoresResponse(
				toInt(stored.getLeadershipScore()),
				toInt(stored.getProactivenessScore()),
				toInt(stored.getEnergyScore()));
	}

	private static int overallChatbotScore(InterviewScoring sc, InterviewResult stored) {
		// Prefer explicit dimension scores if present
		if (sc != null) {
			return avgInt(
					toInt(sc.leadershipScore),
					avgInt(toInt(sc.proactivenessScore), toInt(sc.energyScore)));
		}
		// Fallback to scores stored on InterviewResult
		if (stored != null) {
			return avgInt(
					toInt(stored.getLeadershipScore()),
					avgInt(toInt(stored.getProactivenessScore()), toInt(stored.getEnergyScore())));
		}
		return 0;
	}

	private static int toInt(Double d) {
		if (d == null) {
			return 0;
		}
		return (int) Math.round(d);
	}
}
