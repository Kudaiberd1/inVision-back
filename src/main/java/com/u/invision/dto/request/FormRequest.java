package com.u.invision.dto.request;

import jakarta.persistence.Column;
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
	@DecimalMin("0")
	@DecimalMax("140")
    private Integer unt_score;

    @NotNull
    @DecimalMin("0.0")
    @DecimalMax("9.0")
    private BigDecimal IELTS;

    @NotNull
    @DecimalMin("0")
    @DecimalMax("120")
    private Integer TOEFL;

    private String codeforces;
    private String leetcode;
    private String github;
    private String linkedin;

	@NotBlank
	private String fieldOfStudy;

	@NotNull
	private MultipartFile cv;

	@NotNull
	private MultipartFile motivationEssay;

	@NotNull
	private MultipartFile introductionVideo;
}
