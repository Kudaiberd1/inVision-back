package com.u.invision.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "coding_platform_reviews")
@Getter
@Setter
@NoArgsConstructor
public class CodingPlatformReview {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "form_id", nullable = false)
	private Form form;

	@Column(nullable = false, length = 32)
	private String platform; // "codeforces", "leetcode"

	@Column(nullable = false, length = 128)
	private String handle;

	@Column(nullable = false, columnDefinition = "text")
	private String rawResponseJson;

	private Integer finalScore;

	@Column(nullable = false)
	private Instant createdAt;
}

