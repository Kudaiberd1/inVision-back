package com.u.invision.service;

import com.u.invision.dto.response.AuthResponse;
import com.u.invision.dto.request.LoginRequest;
import com.u.invision.repository.UserRepository;
import com.u.invision.security.JwtService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthService {

	private final UserRepository userRepository;
	private final JwtService jwtService;

	public AuthService(UserRepository userRepository, JwtService jwtService) {
		this.userRepository = userRepository;
		this.jwtService = jwtService;
	}

	public AuthResponse login(LoginRequest request) {
		var user = userRepository
				.findByEmail(request.username())
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));
		if (!user.getPassword().equals(request.password())) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
		}
		String token = jwtService.generateToken(user.getFullName());
		return new AuthResponse(token);
	}
}
