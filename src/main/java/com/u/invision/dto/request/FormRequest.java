package com.u.invision.dto.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.multipart.MultipartFile;

@Data
public class FormRequest {

	@NotBlank
	private String fullName;

	@NotBlank
	@Email(message = "must be a valid email, e.g. name@example.com")
	private String email;

	private String phone;

	@NotNull
	@DateTimeFormat(pattern = "MM/dd/yyyy")
	private LocalDate dateOfBirth;

	@NotBlank
	private String city;

	@NotBlank
	private String schoolUniversity;

	@NotNull
	@DecimalMin("0.0")
	@DecimalMax("5.0")
	private BigDecimal gpa;

	@NotBlank
	private String fieldOfStudy;

	@NotNull
	private MultipartFile cv;

	@NotNull
	private MultipartFile motivationEssay;

	@NotNull
	private MultipartFile introductionVideo;
}
