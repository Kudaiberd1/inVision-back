package com.u.invision.controller;

import com.u.invision.dto.request.CreateDraftFormRequest;
import com.u.invision.dto.request.FormRequest;
import com.u.invision.dto.response.FormResponse;
import com.u.invision.service.FormService;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/forms")
public class FormController {

	private final FormService formService;

	public FormController(FormService formService) {
		this.formService = formService;
	}
	@PostMapping(value = "/draft", consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<FormResponse> createDraft(@Valid @RequestBody CreateDraftFormRequest body) {
		FormResponse created = formService.createDraft(body);
		return ResponseEntity.status(201).body(created);
	}

	@PostMapping(value = "/{id}/submit", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<FormResponse> submitDraft(
			@PathVariable("id") Long formId, @Valid @ModelAttribute FormRequest request) {
		FormResponse body = formService.submitDraft(formId, request);
		return ResponseEntity.status(200).body(body);
	}
}
