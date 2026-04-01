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
import com.u.invision.entity.EssayReview;
import com.u.invision.entity.Form;
import com.u.invision.entity.InterviewResult;
import com.u.invision.repository.CVReviewRepository;
import com.u.invision.repository.EssayReviewRepository;
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
			Integer aiScore = computeAggregateAiScore(cv, essay, chat, form.getUnt_score(), Double.valueOf(String.valueOf(form.getIELTS())), form.getTOEFL());
			out.add(new CandidateSummaryResponse(
					form.getId(),
					form.getFullName(),
					form.getEmail(),
					form.getFieldOfStudy(),
					programSlug(form.getFieldOfStudy()),
					form.getCreatedAt(),
					aiScore,
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

		if (cv != null && cv.getFinalScore() != null) {
			cvLeadershipPoints = ((double) cv.getLeadershipScore() / 100) * 3;
			cvProactivenessPoints = ((double) cv.getProactivenessScore() / 100) * 3;
			cvEnergyPoints = ((double) cv.getEnergyScore() / 100) * 3;
			cvPoints = cvLeadershipPoints + cvProactivenessPoints + cvEnergyPoints;
		}
		if (essay != null && essay.getFinalScore() != null) {
			essayLeadershipPoints = ((double) essay.getLeadershipScore() / 100) * 3;
			essayProactivenessPoints = ((double) essay.getProactivenessScore() / 100) * 3;
			essayEnergyPoints = ((double) essay.getEnergyScore() / 100) * 3;
			essayPoints = essayLeadershipPoints + essayProactivenessPoints + essayEnergyPoints;
		}
		if (chat != null && chat.getChatbotScore() != null) {
			chatLeadershipPoints = (chat.getLeadershipScore() / 100.0) * 3;
			chatProactivenessPoints = (chat.getProactivenessScore() / 100.0) * 3;
			chatEnergyPoints = (chat.getEnergyScore() / 100.0) * 3;
			chatPoints = chatLeadershipPoints + chatProactivenessPoints + chatEnergyPoints;
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

		int overall = computeAggregateAiScore(
				cv,
				essay,
				chat,
				unt,
				form.getIELTS() != null ? form.getIELTS().doubleValue() : null,
				toefl);

		return new ScoreOverviewResponse(
				overall,
				cvPoints,
				essayPoints,
				chatPoints,
				cvLeadershipPoints,
				cvProactivenessPoints,
				cvEnergyPoints,
				essayLeadershipPoints,
				essayProactivenessPoints,
				essayEnergyPoints,
				chatLeadershipPoints,
				chatProactivenessPoints,
				chatEnergyPoints,
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

	private static Integer computeAggregateAiScore(CVReview cv, EssayReview essay, InterviewResult chat, Integer unt, Double ielts, Integer toefl) {
		double sum = 0;
		if (cv != null && cv.getFinalScore() != null) {
			sum += ((double) cv.getLeadershipScore() /100)*3;
            sum += ((double) cv.getProactivenessScore() /100)*3;
            sum += ((double) cv.getEnergyScore() /100)*3;
		}
		if (essay != null && essay.getFinalScore() != null) {
			sum += ((double) essay.getLeadershipScore() /100)*3;
            sum += ((double) essay.getProactivenessScore() /100)*3;
            sum += ((double) essay.getEnergyScore() /100)*3;
		}
		if (chat != null && chat.getChatbotScore() != null) {
			sum += (chat.getLeadershipScore()/100)*3;
            sum += (chat.getProactivenessScore()/100)*3;
            sum += (chat.getEnergyScore()/100)*3;
		}
        if(unt != null) {
            if(unt >= 120){
                sum+=5;
            }else if(unt >= 110){
                sum+=4;
            }else if(unt >= 100){
                sum+=3;
            }else if(unt >= 90){
                sum+=2;
            }else if(unt >= 80){
                sum+=1;
            }
        }
        if(ielts != null) {
            if(ielts >= 8.0){
                sum += 5;
            } else if(ielts >= 7.5){
                sum += 4;
            } else if(ielts >= 7.0){
                sum += 3;
            } else if(ielts >= 6.5){
                sum += 2;
            } else if(ielts >= 6.0){
                sum += 1;
            }
        }
        if(toefl != null) {
            if(toefl >= 110){
                sum += 5;
            } else if(toefl >= 100){
                sum += 4;
            } else if(toefl >= 90){
                sum += 3;
            } else if(toefl >= 80){
                sum += 2;
            } else if(toefl >= 60){
                sum += 1;
            }
        }

		return (int) Math.round(sum);
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
		for (String line : cv.getStrongEvidences()) {
			highlights.add(new HighlightResponse(
					line,
					"Strong evidence signal from automated CV review.",
					"positive"));
		}
		for (String line : cv.getWeakEvidences()) {
			highlights.add(new HighlightResponse(
					line,
					"Weak phrasing / low-specificity signal from automated CV review.",
					"warning"));
		}
		CriteriaScoresResponse cs = new CriteriaScoresResponse(
				cv.getLeadershipScore(), cv.getProactivenessScore(), cv.getEnergyScore());
		int overall = cv.getFinalScore() != null ? (int) Math.round(cv.getFinalScore()) : 0;
		return new CvReviewDetailResponse(cv.getProfileSummary(), highlights, cs, overall);
	}

	private static EssayReviewDetailResponse mapEssayDetail(EssayReview essay) {
		List<HighlightResponse> highlights = new ArrayList<>();
		for (String line : essay.getStrongEvidences()) {
			highlights.add(new HighlightResponse(
					line,
					"Strong evidence signal from automated essay review.",
					"positive"));
		}
		for (String line : essay.getWeakEvidences()) {
			highlights.add(new HighlightResponse(
					line,
					"Weak phrasing signal from automated essay review.",
					"warning"));
		}
		boolean aiFlag = Boolean.TRUE.equals(essay.getPossibleAiGenerated());
		Integer confidence = aiFlag ? 75 : 15;
		CriteriaScoresResponse cs = new CriteriaScoresResponse(
				essay.getLeadershipScore(), essay.getProactivenessScore(), essay.getEnergyScore());
		int overall = essay.getFinalScore() != null ? (int) Math.round(essay.getFinalScore()) : 0;
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
		if (sc != null && sc.chatbotScore != null) {
			return (int) Math.round(sc.chatbotScore);
		}
		if (stored.getChatbotScore() != null) {
			return (int) Math.round(stored.getChatbotScore());
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
