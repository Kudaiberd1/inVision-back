package com.u.invision.controller;

import com.u.invision.dto.request.FormRequest;
import com.u.invision.dto.response.FormResponse;
import com.u.invision.service.FormService;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/forms")
public class FormController {

	private final FormService formService;

	public FormController(FormService formService) {
		this.formService = formService;
	}

	@PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<FormResponse> submit(@Valid @ModelAttribute FormRequest request) {
		FormResponse body = formService.submit(request);
		return ResponseEntity.status(201).body(body);
	}
}
