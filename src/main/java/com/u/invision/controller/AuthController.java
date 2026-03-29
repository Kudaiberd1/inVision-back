package com.u.invision.controller;

import com.u.invision.dto.response.AuthResponse;
import com.u.invision.dto.request.LoginRequest;
import com.u.invision.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

	private final AuthService authService;

	@PostMapping("/login")
	public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
	}
}
