package com.u.invision.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "interview_results")
@Getter
@Setter
@NoArgsConstructor
public class InterviewResult {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "session_id", nullable = false, unique = true, length = 128)
	private String sessionId;

	@Column(name = "candidate_id")
	private String candidateId;

	@Column(name = "candidate_stage")
	private String candidateStage;

	@Column(nullable = false)
	private boolean interviewCompleted;

	private Integer questionsAsked;

	private Integer maxQuestions;

	private Double leadershipScore;

	private Double proactivenessScore;

	private Double energyScore;

	private Double chatbotScore;

	@Column(columnDefinition = "text")
	private String jurySessionSummary;

	/** Full final API payload (JSON) for future use / new fields without migration. */
	@Column(columnDefinition = "text")
	private String responseJson;

	@Column(nullable = false)
	private Instant completedAt;
}
