package com.u.invision.dto.interview;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class InterviewSessionResponse {

	@JsonProperty("session_id")
	public String sessionId;

	@JsonProperty("interview_completed")
	public Boolean interviewCompleted;

	@JsonProperty("questions_asked")
	public Integer questionsAsked;

	@JsonProperty("max_questions")
	public Integer maxQuestions;

	public QuestionPayload question;

	@JsonProperty("dimensions_covered")
	public DimensionsCovered dimensionsCovered;

	public InterviewScoring scoring;

	@JsonProperty("jury_session_summary")
	public String jurySessionSummary;

	public InterviewDetail interview;

	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class QuestionPayload {
		@JsonProperty("question_id")
		public Integer questionId;

		public String dimension;
		@JsonProperty("question_type")
		public String questionType;
		@JsonProperty("question_text")
		public String questionText;
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class DimensionsCovered {
		public Boolean leadership;
		public Boolean proactiveness;
		public Boolean energy;
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class InterviewScoring {
		@JsonProperty("leadership_score")
		public Double leadershipScore;
		@JsonProperty("proactiveness_score")
		public Double proactivenessScore;
		@JsonProperty("energy_score")
		public Double energyScore;
		@JsonProperty("chatbot_score")
		public Double chatbotScore;
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class InterviewDetail {
		@JsonProperty("candidate_id")
		public String candidateId;
		@JsonProperty("candidate_stage")
		public String candidateStage;
		@JsonProperty("session_id")
		public String sessionId;
		@JsonProperty("interview_completed")
		public Boolean interviewCompleted;
		@JsonProperty("total_questions_asked")
		public Integer totalQuestionsAsked;
		@JsonProperty("dimensions_covered")
		public DimensionsCovered dimensionsCovered;
		@JsonProperty("leadership_score")
		public Double leadershipScore;
		@JsonProperty("proactiveness_score")
		public Double proactivenessScore;
		@JsonProperty("energy_score")
		public Double energyScore;
		@JsonProperty("chatbot_score")
		public Double chatbotScore;
		public InterviewScoring scoring;
		@JsonProperty("jury_session_summary")
		public String jurySessionSummary;
		public List<ConversationTurn> conversation = new ArrayList<>();
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class ConversationTurn {
		@JsonProperty("question_id")
		public Integer questionId;
		public String dimension;
		@JsonProperty("question_type")
		public String questionType;
		@JsonProperty("question_text")
		public String questionText;
		@JsonProperty("answer_text")
		public String answerText;
		@JsonProperty("answer_quality")
		public String answerQuality;
		@JsonProperty("score_0_to_100")
		public Integer score0To100;
		@JsonProperty("feedback_for_jury")
		public FeedbackForJury feedbackForJury;
		@JsonProperty("follow_up_used")
		public Boolean followUpUsed;
		public String timestamp;
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class FeedbackForJury {
		public String strengths;
		public String weaknesses;
		@JsonProperty("how_to_improve")
		public String howToImprove;
	}
}
