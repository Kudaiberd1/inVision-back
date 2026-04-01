package com.u.invision.service;

import com.u.invision.dto.ai.EvaluatePdfApiModels.EvaluatePdfResponse;
import com.u.invision.dto.ai.EvaluatePdfApiModels.Evidence;
import com.u.invision.dto.ai.EvaluatePdfApiModels.EvidenceComment;
import com.u.invision.dto.ai.EvaluatePdfApiModels.EvidenceComments;
import com.u.invision.dto.ai.EvaluatePdfApiModels.ReviewSection;
import com.u.invision.dto.ai.EvaluatePdfApiModels.Scores;
import com.u.invision.entity.CVReview;
import com.u.invision.entity.EssayReview;
import com.u.invision.entity.Form;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

@Service
@Slf4j
public class AISummarizeService {

	private final RestTemplate aiRestTemplate;
	private final String evaluatePdfUrl;

	public AISummarizeService(
			@Qualifier("aiEvaluatorRestTemplate") RestTemplate aiRestTemplate,
			@Value("${ai.evaluator.base-url:http://127.0.0.1:8000}") String baseUrl) {
		this.aiRestTemplate = aiRestTemplate;
		String base = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
		this.evaluatePdfUrl = base + "/evaluate/pdf";
	}

	public ReviewSection evaluatePdf(byte[] pdfBytes, String filename, String contentType, String mode, String userId) {
		String name = filename != null && !filename.isBlank() ? filename : "document.pdf";

		ByteArrayResource filePart =
				new ByteArrayResource(pdfBytes) {
					@Override
					public String getFilename() {
						return name;
					}

					@Override
					public long contentLength() {
						return pdfBytes.length;
					}
				};

		MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
		body.add("file", filePart);
		body.add("mode", mode);
		body.add("user_id", userId != null ? userId : "");

		try {
			EvaluatePdfResponse response =
					aiRestTemplate.postForObject(evaluatePdfUrl, body, EvaluatePdfResponse.class);
			return extractSection(Objects.requireNonNull(response, "Empty AI response"), mode);
		} catch (ResponseStatusException e) {
			throw e;
		} catch (HttpStatusCodeException e) {
			String detail = e.getResponseBodyAsString();
			throw new ResponseStatusException(
					HttpStatus.BAD_GATEWAY,
					"AI evaluator HTTP " + e.getStatusCode().value() + ": " + (detail.isBlank() ? e.getMessage() : detail),
					e);
		} catch (Exception e) {
			throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "AI evaluator request failed: " + e.getMessage(), e);
		}
	}

	public CVReview toCvReview(Form form, ReviewSection section) {
		return mapToCvReview(form, section);
	}

	public EssayReview toEssayReview(Form form, ReviewSection section) {
		return mapToEssayReview(form, section);
	}

	private static ReviewSection extractSection(EvaluatePdfResponse body, String mode) {
		if ("cv".equalsIgnoreCase(mode)) {
			ReviewSection section = body.effectiveCvSection();
			if (section == null) {
				throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "AI response missing cv block");
			}
			return section;
		}
		if ("essay".equalsIgnoreCase(mode)) {
			ReviewSection section = body.effectiveEssaySection();
			if (section == null) {
				throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "AI response missing essay block");
			}
			return section;
		}
		throw new IllegalArgumentException("mode must be cv or essay");
	}

	private static CVReview mapToCvReview(Form form, ReviewSection s) {
		CVReview r = new CVReview();
		r.setForm(form);
		applyScores(r, s.scores);
		r.setProfileSummary(s.profileSummary != null ? s.profileSummary : "");
		r.setRecommendation(s.recommendation);
		if (s.flags != null) {
			r.setPossibleAiGenerated(boolOrNull(s.flags.possibleAiGenerated));
			r.setNeedsHumanReview(boolOrNull(s.flags.needsHumanReview));
		}
		r.setStrongEvidences(copyEvidenceStrong(s.evidence));
		r.setStrongEvidenceReasons(copyEvidenceStrongReasons(s.evidenceComments));
		r.setWeakEvidences(copyEvidenceWeak(s.evidence));
		r.setWeakEvidenceReasons(copyEvidenceWeakReasons(s.evidenceComments));
		r.setKeywords(copyKeywords(s.keywords));
		return r;
	}

	private static EssayReview mapToEssayReview(Form form, ReviewSection s) {
		EssayReview r = new EssayReview();
		r.setForm(form);
		applyScoresEssay(r, s.scores);
		r.setProfileSummary(s.profileSummary != null ? s.profileSummary : "");
		r.setRecommendation(s.recommendation);
		if (s.flags != null) {
			r.setPossibleAiGenerated(boolOrNull(s.flags.possibleAiGenerated));
			r.setNeedsHumanReview(boolOrNull(s.flags.needsHumanReview));
		}
		r.setStrongEvidences(copyEvidenceStrong(s.evidence));
		r.setStrongEvidenceReasons(copyEvidenceStrongReasons(s.evidenceComments));
		r.setWeakEvidences(copyEvidenceWeak(s.evidence));
		r.setWeakEvidenceReasons(copyEvidenceWeakReasons(s.evidenceComments));
		r.setKeywords(copyKeywords(s.keywords));
		return r;
	}

	private static void applyScores(CVReview r, Scores sc) {
		requireScores(sc, "cv");
		r.setLeadershipScore(sc.leadership);
		r.setProactivenessScore(sc.proactiveness);
		r.setEnergyScore(sc.energy);
		r.setCoreScore(sc.coreScore);
		r.setMotivation(sc.motivation);
		r.setGrowthPotential(sc.growthPotential);
		r.setExperienceSignals(sc.experienceSignals);
		r.setFinalScore(sc.finalScore);
	}

	private static void applyScoresEssay(EssayReview r, Scores sc) {
		requireScores(sc, "essay");
		r.setLeadershipScore(sc.leadership);
		r.setProactivenessScore(sc.proactiveness);
		r.setEnergyScore(sc.energy);
		r.setCoreScore(sc.coreScore);
		r.setMotivation(sc.motivation);
		r.setGrowthPotential(sc.growthPotential);
		r.setExperienceSignals(sc.experienceSignals);
		r.setFinalScore(sc.finalScore);
	}

	private static void requireScores(Scores sc, String label) {
		if (sc == null
				|| sc.leadership == null
				|| sc.proactiveness == null
				|| sc.energy == null) {
			throw new ResponseStatusException(
					HttpStatus.BAD_GATEWAY, "AI response missing score fields for " + label);
		}
	}

	private static Boolean boolOrNull(Boolean b) {
		return b != null ? b : null;
	}

	private static List<String> copyEvidenceStrong(Evidence ev) {
		if (ev == null || ev.strongEvidence == null) {
			return new ArrayList<>();
		}
		return new ArrayList<>(ev.strongEvidence);
	}

	private static List<String> copyEvidenceWeak(Evidence ev) {
		if (ev == null || ev.weakPhrases == null) {
			return new ArrayList<>();
		}
		return new ArrayList<>(ev.weakPhrases);
	}

	private static List<String> copyEvidenceStrongReasons(EvidenceComments comments) {
		List<String> out = new ArrayList<>();
		if (comments == null || comments.strong == null) {
			return out;
		}
		for (EvidenceComment c : comments.strong) {
			if (c == null) {
				continue;
			}
			String quote = c.quote != null ? c.quote : "";
			String comment = c.comment != null ? c.comment : "";
			out.add(comment.isBlank() ? "" : comment);
		}
		return out;
	}

	private static List<String> copyEvidenceWeakReasons(EvidenceComments comments) {
		List<String> out = new ArrayList<>();
		if (comments == null || comments.weak == null) {
			return out;
		}
		for (EvidenceComment c : comments.weak) {
			if (c == null) {
				continue;
			}
			String comment = c.comment != null ? c.comment : "";
			out.add(comment.isBlank() ? "" : comment);
		}
		return out;
	}

	private static List<String> copyKeywords(List<String> k) {
		if (k == null) {
			return new ArrayList<>();
		}
		return new ArrayList<>(k);
	}
}
