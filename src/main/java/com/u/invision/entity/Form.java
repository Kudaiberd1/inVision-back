package com.u.invision.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Instant;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "application_forms")
@Getter
@Setter
@NoArgsConstructor
public class Form {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	/** Null while {@link ApplicationStatus#DRAFT}. DB column must allow NULL (see {@code scripts/postgres-allow-draft-nulls.sql}). */
	@Column(nullable = true)
	private String fullName;

	/** Null while {@link ApplicationStatus#DRAFT}. */
	@Column(nullable = true)
	private String email;

	private String phone;

	/** Null while {@link ApplicationStatus#DRAFT}. */
	@Column(nullable = true)
	private LocalDate dateOfBirth;

	/** Null while {@link ApplicationStatus#DRAFT}. */
	@Column(nullable = true)
	private String city;

	/** Null while {@link ApplicationStatus#DRAFT}. */
	@Column(nullable = true)
	private String schoolUniversity;

	/** Null while {@link ApplicationStatus#DRAFT}. */
	@Column(nullable = true, precision = 4, scale = 2)
	private BigDecimal gpa;

	/** Set on draft creation (chosen program). May be updated on final submit. */
	@Column(nullable = false)
	private String fieldOfStudy;

	private String cvUrl;
	private String motivationEssayUrl;
	private String videoUrl;

	@Column(nullable = false)
	private Instant createdAt;

	@Enumerated(EnumType.STRING)
	@Column(length = 16)
	private ApplicationStatus status;

	@PrePersist
	void prePersistDefaults() {
		if (status == null) {
			status = ApplicationStatus.DRAFT;
		}
	}
}
