package com.petservicehub.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import com.petservicehub.model.User;
import com.petservicehub.repository.OfferedServiceRepository;
import com.petservicehub.repository.ReviewRepository;
import com.petservicehub.repository.ServiceRequestRepository;
import com.petservicehub.repository.UserRepository;
import com.petservicehub.support.AuthTestSupport;

@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private OfferedServiceRepository offeredServiceRepository;

	@Autowired
	private ServiceRequestRepository serviceRequestRepository;

	@Autowired
	private ReviewRepository reviewRepository;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@BeforeEach
	void limparBase() {
		reviewRepository.deleteAll();
		serviceRequestRepository.deleteAll();
		offeredServiceRepository.deleteAll();
		userRepository.deleteAll();
	}

	@Test
	void deveRegistrarComSenhaBcrypt() throws Exception {
		mockMvc.perform(post("/api/auth/register")
						.contentType(MediaType.APPLICATION_JSON)
						.content(AuthTestSupport.registerJson("Ana Souza", "Ana.Souza@email.com", "CLIENT")))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.email").value("ana.souza@email.com"))
				.andExpect(jsonPath("$.role").value("CLIENT"))
				.andExpect(jsonPath("$.password").doesNotExist())
				.andExpect(jsonPath("$.passwordHash").doesNotExist());

		User salvo = userRepository.findByEmailIgnoreCase("ana.souza@email.com").orElseThrow();
		assertThat(salvo.getPasswordHash()).startsWith("$2");
		assertThat(passwordEncoder.matches("senha123", salvo.getPasswordHash())).isTrue();
	}

	@Test
	void deveRejeitarCadastroAdmin() throws Exception {
		mockMvc.perform(post("/api/auth/register")
						.contentType(MediaType.APPLICATION_JSON)
						.content(AuthTestSupport.registerJson("Hacker", "hacker@email.com", "ADMIN")))
				.andExpect(status().isBadRequest());
	}

	@Test
	void deveAutenticarComLoginValido() throws Exception {
		mockMvc.perform(post("/api/auth/register")
						.contentType(MediaType.APPLICATION_JSON)
						.content(AuthTestSupport.registerJson("Ana Souza", "ana.souza@email.com", "CLIENT")))
				.andExpect(status().isCreated());

		mockMvc.perform(post("/api/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content(AuthTestSupport.loginJson("ana.souza@email.com", "senha123")))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.token").isNotEmpty())
				.andExpect(jsonPath("$.tokenType").value("Bearer"))
				.andExpect(jsonPath("$.user.email").value("ana.souza@email.com"))
				.andExpect(jsonPath("$.user.passwordHash").doesNotExist());
	}

	@Test
	void deveRejeitarSenhaErrada() throws Exception {
		mockMvc.perform(post("/api/auth/register")
						.contentType(MediaType.APPLICATION_JSON)
						.content(AuthTestSupport.registerJson("Ana Souza", "ana.souza@email.com", "CLIENT")))
				.andExpect(status().isCreated());

		mockMvc.perform(post("/api/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content(AuthTestSupport.loginJson("ana.souza@email.com", "errada")))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.detail").value("Credenciais inválidas"));
	}

	@Test
	void usuarioInativoNaoAutentica() throws Exception {
		mockMvc.perform(post("/api/auth/register")
						.contentType(MediaType.APPLICATION_JSON)
						.content(AuthTestSupport.registerJson("Ana Souza", "ana.souza@email.com", "CLIENT")))
				.andExpect(status().isCreated());
		User user = userRepository.findByEmailIgnoreCase("ana.souza@email.com").orElseThrow();
		user.setActive(false);
		userRepository.save(user);

		mockMvc.perform(post("/api/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content(AuthTestSupport.loginJson("ana.souza@email.com", "senha123")))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.detail").value("Credenciais inválidas"));
	}
}
