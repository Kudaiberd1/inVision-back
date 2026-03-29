package com.u.invision.entity;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "cv_reviews")
@Getter
@Setter
@NoArgsConstructor
public class CVReview {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "form_id", nullable = false, unique = true)
	private Form form;

	@Column(nullable = false)
	private Integer leadershipScore;

	@Column(nullable = false)
	private Integer proactivenessScore;

	@Column(nullable = false)
	private Integer energyScore;

	@Column(nullable = false)
	private Double coreScore;

	@Column(nullable = false)
	private Integer motivation;

	@Column(nullable = false)
	private Integer growthPotential;

	@Column(nullable = false)
	private Integer experienceSignals;

	@Column(nullable = false)
	private Double finalScore;

	@Column(nullable = false, columnDefinition = "text")
	private String profileSummary;

	@Column(length = 64)
	private String recommendation;

	private Boolean possibleAiGenerated;

	private Boolean needsHumanReview;

	@ElementCollection(fetch = FetchType.LAZY)
	@CollectionTable(name = "cv_review_strong_evidence", joinColumns = @JoinColumn(name = "cv_review_id"))
	@Column(name = "line_text", columnDefinition = "text")
	private List<String> strongEvidences = new ArrayList<>();

	@ElementCollection(fetch = FetchType.LAZY)
	@CollectionTable(name = "cv_review_weak_phrases", joinColumns = @JoinColumn(name = "cv_review_id"))
	@Column(name = "line_text", columnDefinition = "text")
	private List<String> weakEvidences = new ArrayList<>();

	@ElementCollection(fetch = FetchType.LAZY)
	@CollectionTable(name = "cv_review_keywords", joinColumns = @JoinColumn(name = "cv_review_id"))
	@Column(name = "keyword", length = 512)
	private List<String> keywords = new ArrayList<>();
}
