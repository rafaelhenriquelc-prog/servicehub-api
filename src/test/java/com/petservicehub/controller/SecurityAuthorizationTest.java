package com.petservicehub.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import com.petservicehub.model.OfferedService;
import com.petservicehub.model.User;
import com.petservicehub.repository.OfferedServiceRepository;
import com.petservicehub.repository.ReviewRepository;
import com.petservicehub.repository.ServiceRequestRepository;
import com.petservicehub.repository.UserRepository;
import com.petservicehub.support.AuthTestSupport;

@SpringBootTest
@AutoConfigureMockMvc
class SecurityAuthorizationTest {

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

	private String adminToken;
	private String clientToken;
	private String providerToken;

	@BeforeEach
	void limparBase() throws Exception {
		reviewRepository.deleteAll();
		serviceRequestRepository.deleteAll();
		offeredServiceRepository.deleteAll();
		userRepository.deleteAll();
		AuthTestSupport.criarAdmin(userRepository, passwordEncoder, "admin@email.com");
		adminToken = AuthTestSupport.login(mockMvc, "admin@email.com", "senha123");
		providerToken = AuthTestSupport.registerAndLogin(mockMvc, "Carlos Lima", "carlos.lima@email.com", "PROVIDER");
		clientToken = AuthTestSupport.registerAndLogin(mockMvc, "Ana Souza", "ana.souza@email.com", "CLIENT");
	}

	@Test
	void endpointSemTokenRetorna401() throws Exception {
		mockMvc.perform(get("/api/users"))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void tokenInvalidoRetorna401() throws Exception {
		mockMvc.perform(get("/api/users")
						.header("Authorization", "Bearer token-invalido"))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void papelIncorretoRetorna403() throws Exception {
		mockMvc.perform(get("/api/users")
						.header("Authorization", "Bearer " + clientToken))
				.andExpect(status().isForbidden());
	}

	@Test
	void providerCriaServico() throws Exception {
		mockMvc.perform(post("/api/services")
						.header("Authorization", "Bearer " + providerToken)
						.contentType(MediaType.APPLICATION_JSON)
						.content(serviceJson()))
				.andExpect(status().isCreated());
	}

	@Test
	void clientNaoCriaServico() throws Exception {
		mockMvc.perform(post("/api/services")
						.header("Authorization", "Bearer " + clientToken)
						.contentType(MediaType.APPLICATION_JSON)
						.content(serviceJson()))
				.andExpect(status().isForbidden());
	}

	@Test
	void usuarioNaoAcessaRecursoDeOutroUsuario() throws Exception {
		mockMvc.perform(post("/api/services")
						.header("Authorization", "Bearer " + providerToken)
						.contentType(MediaType.APPLICATION_JSON)
						.content(serviceJson()))
				.andExpect(status().isCreated());
		OfferedService service = offeredServiceRepository.findAll().getFirst();

		String outroProvider = AuthTestSupport.registerAndLogin(
				mockMvc, "Outro Provider", "outro.provider@email.com", "PROVIDER");

		mockMvc.perform(put("/api/services/{id}", service.getId())
						.header("Authorization", "Bearer " + outroProvider)
						.contentType(MediaType.APPLICATION_JSON)
						.content(serviceJson()))
				.andExpect(status().isForbidden());

		User client = userRepository.findByEmailIgnoreCase("ana.souza@email.com").orElseThrow();
		mockMvc.perform(get("/api/users/{id}", client.getId())
						.header("Authorization", "Bearer " + providerToken))
				.andExpect(status().isForbidden());
	}

	@Test
	void adminAcessaUsuarios() throws Exception {
		mockMvc.perform(get("/api/users")
						.header("Authorization", "Bearer " + adminToken))
				.andExpect(status().isOk());
	}

	@Test
	void tokenDeUsuarioInativoRetorna401() throws Exception {
		User client = userRepository.findByEmailIgnoreCase("ana.souza@email.com").orElseThrow();
		client.setActive(false);
		userRepository.save(client);

		mockMvc.perform(get("/api/users/{id}", client.getId())
						.header("Authorization", "Bearer " + clientToken))
				.andExpect(status().isUnauthorized());
	}

	private String serviceJson() {
		return """
				{
					"title": "Montagem de móveis",
					"description": "Montagem residencial",
					"price": 150.00,
					"category": "Marcenaria",
					"active": true
				}
				""";
	}
}
