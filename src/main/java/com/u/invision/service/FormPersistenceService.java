package com.u.invision.service;

import com.u.invision.dto.response.FormResponse;
import com.u.invision.entity.CVReview;
import com.u.invision.entity.EssayReview;
import com.u.invision.entity.Form;
import com.u.invision.repository.CVReviewRepository;
import com.u.invision.repository.EssayReviewRepository;
import com.u.invision.repository.FormRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FormPersistenceService {

	private final FormRepository formRepository;
	private final CVReviewRepository cvReviewRepository;
	private final EssayReviewRepository essayReviewRepository;

	public FormPersistenceService(
			FormRepository formRepository,
			CVReviewRepository cvReviewRepository,
			EssayReviewRepository essayReviewRepository) {
		this.formRepository = formRepository;
		this.cvReviewRepository = cvReviewRepository;
		this.essayReviewRepository = essayReviewRepository;
	}

	@Transactional
	public FormResponse saveFormWithReviews(Form form, CVReview cvReview, EssayReview essayReview) {
		Form saved = formRepository.saveAndFlush(form);
		cvReview.setForm(saved);
		essayReview.setForm(saved);
		cvReviewRepository.save(cvReview);
		essayReviewRepository.save(essayReview);
		return new FormResponse(saved.getId());
	}
}
