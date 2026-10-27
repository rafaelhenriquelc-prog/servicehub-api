package com.petservicehub.security;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JsonAuthHandlers implements AuthenticationEntryPoint, AccessDeniedHandler {

	@Override
	public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception)
			throws IOException {
		write(response, HttpStatus.UNAUTHORIZED, "Não autenticado",
				"Token ausente, inválido, expirado ou usuário inativo");
	}

	@Override
	public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException exception)
			throws IOException {
		write(response, HttpStatus.FORBIDDEN, "Acesso negado", "Você não tem permissão para acessar este recurso");
	}

	private void write(HttpServletResponse response, HttpStatus status, String title, String detail) throws IOException {
		response.setStatus(status.value());
		response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
		response.setCharacterEncoding(StandardCharsets.UTF_8.name());
		response.getWriter().write("""
				{"title":"%s","status":%d,"detail":"%s"}
				""".formatted(title, status.value(), detail));
	}
}
