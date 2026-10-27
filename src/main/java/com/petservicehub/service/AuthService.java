package com.petservicehub.service;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.petservicehub.dto.EntityMapper;
import com.petservicehub.dto.LoginRequest;
import com.petservicehub.dto.LoginResponse;
import com.petservicehub.dto.RegisterRequest;
import com.petservicehub.dto.UserResponse;
import com.petservicehub.model.User;
import com.petservicehub.repository.UserRepository;
import com.petservicehub.security.JwtService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

	private static final String CREDENCIAIS_INVALIDAS = "Credenciais inválidas";

	private final UserService userService;
	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtService jwtService;

	@Transactional
	public UserResponse register(RegisterRequest request) {
		return userService.registrar(request);
	}

	@Transactional(readOnly = true)
	public LoginResponse login(LoginRequest request) {
		String email = UserService.normalizeEmail(request.email());
		User user = userRepository.findByEmailIgnoreCase(email)
				.orElseThrow(() -> new BadCredentialsException(CREDENCIAIS_INVALIDAS));
		if (!Boolean.TRUE.equals(user.getActive()) || !passwordEncoder.matches(request.password(), user.getPasswordHash())) {
			throw new BadCredentialsException(CREDENCIAIS_INVALIDAS);
		}
		String token = jwtService.generateToken(user);
		return new LoginResponse(token, "Bearer", jwtService.expirationMs(), EntityMapper.toResponse(user));
	}
}
