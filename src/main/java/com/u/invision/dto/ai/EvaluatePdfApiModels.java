package com.u.invision.dto.ai;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class EvaluatePdfApiModels {

	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class EvaluatePdfResponse {
		@JsonProperty("user_id")
		public String userId;

		public Meta meta;

		public ReviewSection evaluation;

		public ReviewSection cv;
		public ReviewSection essay;

		public Scores scores;
		public Evidence evidence;
		@JsonProperty("evidence_comments")
		public EvidenceComments evidenceComments;
		public List<String> keywords;
		@JsonProperty("profile_summary")
		public String profileSummary;
		public String recommendation;
		public Flags flags;

		public ReviewSection effectiveCvSection() {
			if (evaluation != null) {
				return evaluation;
			}
			if (cv != null) {
				return cv;
			}
			return rootAsReviewSectionIfPresent();
		}

		public ReviewSection effectiveEssaySection() {
			if (evaluation != null) {
				return evaluation;
			}
			if (essay != null) {
				return essay;
			}
			return rootAsReviewSectionIfPresent();
		}

		private ReviewSection rootAsReviewSectionIfPresent() {
			if (scores == null) {
				return null;
			}
			ReviewSection r = new ReviewSection();
			r.scores = scores;
			r.evidence = evidence;
			r.evidenceComments = evidenceComments;
			r.keywords = keywords;
			r.profileSummary = profileSummary;
			r.recommendation = recommendation;
			r.flags = flags;
			return r;
		}
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class Meta {
		public String filename;
		public String mode;

		@JsonProperty("extracted_chars")
		public Integer extractedChars;

		@JsonProperty("text_truncated")
		public Boolean textTruncated;
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class ReviewSection {
		public Scores scores;
		public Evidence evidence;
		@JsonProperty("evidence_comments")
		public EvidenceComments evidenceComments;
		public List<String> keywords;
		@JsonProperty("profile_summary")
		public String profileSummary;

		public String recommendation;
		public Flags flags;
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class Scores {
		// New schema: leadership_score / proactiveness_score / energy_score
		// (older payloads with "leadership" etc. still map because of ignoreUnknown=true elsewhere)
		@JsonProperty("leadership_score")
		public Integer leadership;

		@JsonProperty("proactiveness_score")
		public Integer proactiveness;

		@JsonProperty("energy_score")
		public Integer energy;

		@JsonProperty("core_score")
		public Double coreScore;

		public Integer motivation;

		@JsonProperty("growth_potential")
		public Integer growthPotential;

		@JsonProperty("experience_signals")
		public Integer experienceSignals;

		@JsonProperty("final_score")
		public Double finalScore;
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class Evidence {
		@JsonProperty("strong_evidence")
		public List<String> strongEvidence = new ArrayList<>();

		@JsonProperty("weak_phrases")
		public List<String> weakPhrases = new ArrayList<>();
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class EvidenceComments {
		@JsonProperty("strong")
		public List<EvidenceComment> strong = new ArrayList<>();

		@JsonProperty("weak")
		public List<EvidenceComment> weak = new ArrayList<>();
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class EvidenceComment {
		public String quote;
		public String comment;
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class Flags {
		@JsonProperty("possible_ai_generated")
		public Boolean possibleAiGenerated;

		@JsonProperty("needs_human_review")
		public Boolean needsHumanReview;
	}
}
