package com.u.invision.service;

import com.u.invision.dto.request.FormRequest;
import com.u.invision.dto.ai.EvaluatePdfApiModels.ReviewSection;
import com.u.invision.dto.response.FormResponse;
import com.u.invision.entity.ApplicationStatus;
import com.u.invision.entity.CVReview;
import com.u.invision.entity.EssayReview;
import com.u.invision.entity.Form;
import java.io.IOException;
import java.time.Instant;
import java.util.Locale;
import java.util.Objects;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@Service
public class FormService {

	private static final long MAX_PDF_BYTES = 5L * 1024 * 1024;
	private static final long MAX_VIDEO_BYTES = 50L * 1024 * 1024;

	private final S3Service s3Service;
	private final FormPersistenceService formPersistenceService;
	private final AISummarizeService aiSummarizeService;

	public FormService(
			S3Service s3Service,
			FormPersistenceService formPersistenceService,
			AISummarizeService aiSummarizeService) {
		this.s3Service = s3Service;
		this.formPersistenceService = formPersistenceService;
		this.aiSummarizeService = aiSummarizeService;
	}

	public FormResponse submit(FormRequest request) {
		validatePdf(request.getCv(), "CV");
		validatePdf(request.getMotivationEssay(), "Motivation essay");
		validateMp4(request.getIntroductionVideo(), "Introduction video");

		try {
			byte[] cvBytes = request.getCv().getBytes();
			byte[] essayBytes = request.getMotivationEssay().getBytes();
			String userId = request.getFullName().trim();

			ReviewSection cvSection =
					aiSummarizeService.evaluatePdf(cvBytes, request.getCv().getOriginalFilename(), request.getCv().getContentType(), "cv", userId);
			ReviewSection essaySection = aiSummarizeService.evaluatePdf(
					essayBytes,
					request.getMotivationEssay().getOriginalFilename(),
					request.getMotivationEssay().getContentType(),
					"essay",
					userId);

			String cvUrl = s3Service.uploadBytes(
					cvBytes, request.getCv().getOriginalFilename(), request.getCv().getContentType(), "cv");
			String essayUrl = s3Service.uploadBytes(
					essayBytes,
					request.getMotivationEssay().getOriginalFilename(),
					request.getMotivationEssay().getContentType(),
					"essay");
			String videoUrl = s3Service.uploadFile(request.getIntroductionVideo(), "videos");

			Form form = new Form();
			form.setFullName(request.getFullName().trim());
			form.setEmail(request.getEmail().trim().toLowerCase(Locale.ROOT));
			form.setPhone(trimToNull(request.getPhone()));
			form.setDateOfBirth(request.getDateOfBirth());
			form.setCity(request.getCity().trim());
			form.setSchoolUniversity(request.getSchoolUniversity().trim());
			form.setGpa(request.getGpa());
			form.setFieldOfStudy(request.getFieldOfStudy().trim());
			form.setCvUrl(cvUrl);
			form.setMotivationEssayUrl(essayUrl);
			form.setVideoUrl(videoUrl);
			form.setCreatedAt(Instant.now());
			form.setStatus(ApplicationStatus.PENDING);

			CVReview cvReview = aiSummarizeService.toCvReview(form, cvSection);
			EssayReview essayReview = aiSummarizeService.toEssayReview(form, essaySection);

			return formPersistenceService.saveFormWithReviews(form, cvReview, essayReview);
		} catch (IOException e) {
			throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Failed to read uploaded files", e);
		}
	}

	private static void validatePdf(MultipartFile file, String label) {
		requireFile(file, label);
		String name = Objects.requireNonNullElse(file.getOriginalFilename(), "").toLowerCase(Locale.ROOT);
		if (!name.endsWith(".pdf")) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, label + " must be a PDF");
		}
		if (file.getSize() > MAX_PDF_BYTES) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, label + " must be at most 5 MB");
		}
	}

	private static void validateMp4(MultipartFile file, String label) {
		requireFile(file, label);
		String name = Objects.requireNonNullElse(file.getOriginalFilename(), "").toLowerCase(Locale.ROOT);
		if (!name.endsWith(".mp4")) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, label + " must be MP4");
		}
		if (file.getSize() > MAX_VIDEO_BYTES) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, label + " must be at most 50 MB");
		}
	}

	private static void requireFile(MultipartFile file, String label) {
		if (file == null || file.isEmpty()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, label + " is required");
		}
	}

	private static String trimToNull(String s) {
		if (s == null) {
			return null;
		}
		String t = s.trim();
		return t.isEmpty() ? null : t;
	}
}
