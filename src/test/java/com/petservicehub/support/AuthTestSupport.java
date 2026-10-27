package com.petservicehub.support;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.petservicehub.model.User;
import com.petservicehub.model.UserRole;
import com.petservicehub.repository.UserRepository;

public final class AuthTestSupport {

	private AuthTestSupport() {
	}

	public static String registerJson(String nome, String email, String role) {
		return """
				{
					"fullName": "%s",
					"email": "%s",
					"password": "senha123",
					"phone": "11988887777",
					"role": "%s"
				}
				""".formatted(nome, email, role);
	}

	public static String loginJson(String email, String password) {
		return """
				{
					"email": "%s",
					"password": "%s"
				}
				""".formatted(email, password);
	}

	public static String registerAndLogin(MockMvc mockMvc, String nome, String email, String role)
			throws Exception {
		mockMvc.perform(post("/api/auth/register")
						.contentType(MediaType.APPLICATION_JSON)
						.content(registerJson(nome, email, role)))
				.andExpect(status().isCreated());
		return login(mockMvc, email, "senha123");
	}

	public static String login(MockMvc mockMvc, String email, String password) throws Exception {
		MvcResult result = mockMvc.perform(post("/api/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content(loginJson(email, password)))
				.andExpect(status().isOk())
				.andReturn();
		return extractToken(result.getResponse().getContentAsString());
	}

	private static String extractToken(String body) {
		String marker = "\"token\":\"";
		int start = body.indexOf(marker);
		if (start < 0) {
			throw new IllegalStateException("Token não encontrado na resposta de login");
		}
		start += marker.length();
		int end = body.indexOf('"', start);
		return body.substring(start, end);
	}

	public static User criarAdmin(UserRepository userRepository, PasswordEncoder passwordEncoder, String email) {
		return userRepository.save(User.builder()
				.fullName("Admin")
				.email(email.toLowerCase())
				.passwordHash(passwordEncoder.encode("senha123"))
				.phone("11900000000")
				.role(UserRole.ADMIN)
				.active(true)
				.build());
	}
}
