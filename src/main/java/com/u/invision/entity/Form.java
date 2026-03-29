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

	@Column(nullable = false)
	private String fullName;

	@Column(nullable = false)
	private String email;

	private String phone;

	@Column(nullable = false)
	private LocalDate dateOfBirth;

	@Column(nullable = false)
	private String city;

	@Column(nullable = false)
	private String schoolUniversity;

	@Column(nullable = false, precision = 4, scale = 2)
	private BigDecimal gpa;

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
			status = ApplicationStatus.PENDING;
		}
	}
}
