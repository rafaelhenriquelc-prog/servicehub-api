package com.petservicehub.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
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
class UserControllerTest {

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

	@BeforeEach
	void limparBase() throws Exception {
		reviewRepository.deleteAll();
		serviceRequestRepository.deleteAll();
		offeredServiceRepository.deleteAll();
		userRepository.deleteAll();
		AuthTestSupport.criarAdmin(userRepository, passwordEncoder, "admin@email.com");
		adminToken = AuthTestSupport.login(mockMvc, "admin@email.com", "senha123");
	}

	@Test
	void deveCadastrarUsuarioComoAdmin() throws Exception {
		mockMvc.perform(post("/api/users")
						.header("Authorization", "Bearer " + adminToken)
						.contentType(MediaType.APPLICATION_JSON)
						.content(userJson("Ana Souza", "ana.souza@email.com", "CLIENT", true)))
				.andExpect(status().isCreated())
				.andExpect(header().exists("Location"))
				.andExpect(jsonPath("$.fullName").value("Ana Souza"))
				.andExpect(jsonPath("$.email").value("ana.souza@email.com"))
				.andExpect(jsonPath("$.role").value("CLIENT"))
				.andExpect(jsonPath("$.password").doesNotExist())
				.andExpect(jsonPath("$.passwordHash").doesNotExist());
	}

	@Test
	void deveRetornarBadRequestQuandoDadosInvalidos() throws Exception {
		mockMvc.perform(post("/api/users")
						.header("Authorization", "Bearer " + adminToken)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
									"fullName": "",
									"email": "invalido",
									"password": "123"
								}
								"""))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.erros.fullName").exists())
				.andExpect(jsonPath("$.erros.email").exists())
				.andExpect(jsonPath("$.erros.password").exists())
				.andExpect(jsonPath("$.erros.role").exists());
	}

	@Test
	void deveRecusarEmailDuplicado() throws Exception {
		AuthTestSupport.registerAndLogin(mockMvc, "Ana Souza", "ana.souza@email.com", "CLIENT");

		mockMvc.perform(post("/api/users")
						.header("Authorization", "Bearer " + adminToken)
						.contentType(MediaType.APPLICATION_JSON)
						.content(userJson("Ana Outra", "ANA.SOUZA@email.com", "CLIENT", true)))
				.andExpect(status().isConflict());
	}

	@Test
	void deveListarUsuariosComoAdmin() throws Exception {
		AuthTestSupport.registerAndLogin(mockMvc, "Ana Souza", "ana.souza@email.com", "CLIENT");
		AuthTestSupport.registerAndLogin(mockMvc, "Carlos Lima", "carlos.lima@email.com", "PROVIDER");

		mockMvc.perform(get("/api/users").param("role", "PROVIDER")
						.header("Authorization", "Bearer " + adminToken))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(1)))
				.andExpect(jsonPath("$[0].fullName").value("Carlos Lima"));
	}

	@Test
	void deveBuscarUsuarioPorId() throws Exception {
		User admin = userRepository.findByEmailIgnoreCase("admin@email.com").orElseThrow();

		mockMvc.perform(get("/api/users/{id}", admin.getId())
						.header("Authorization", "Bearer " + adminToken))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.email").value("admin@email.com"));
	}

	@Test
	void deveRetornarNotFoundQuandoUsuarioNaoExiste() throws Exception {
		mockMvc.perform(get("/api/users/{id}", 999)
						.header("Authorization", "Bearer " + adminToken))
				.andExpect(status().isNotFound());
	}

	@Test
	void deveAtualizarUsuario() throws Exception {
		User admin = userRepository.findByEmailIgnoreCase("admin@email.com").orElseThrow();

		mockMvc.perform(put("/api/users/{id}", admin.getId())
						.header("Authorization", "Bearer " + adminToken)
						.contentType(MediaType.APPLICATION_JSON)
						.content(userJson("Admin Atualizado", "admin.atualizado@email.com", "ADMIN", true)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.fullName").value("Admin Atualizado"))
				.andExpect(jsonPath("$.email").value("admin.atualizado@email.com"));
	}

	@Test
	void deveExcluirUsuario() throws Exception {
		mockMvc.perform(post("/api/users")
						.header("Authorization", "Bearer " + adminToken)
						.contentType(MediaType.APPLICATION_JSON)
						.content(userJson("Temp", "temp@email.com", "CLIENT", true)))
				.andExpect(status().isCreated());
		User temp = userRepository.findByEmailIgnoreCase("temp@email.com").orElseThrow();

		mockMvc.perform(delete("/api/users/{id}", temp.getId())
						.header("Authorization", "Bearer " + adminToken))
				.andExpect(status().isNoContent());
	}

	@Test
	void deveExporDocumentacaoOpenApi() throws Exception {
		mockMvc.perform(get("/v3/api-docs"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.info.title").value("ServiceHub API"))
				.andExpect(jsonPath("$.components.securitySchemes['bearer-jwt']").exists())
				.andExpect(jsonPath("$.paths['/api/auth/login']").exists())
				.andExpect(jsonPath("$.paths['/api/auth/register']").exists());
	}

	private String userJson(String nome, String email, String role, boolean active) {
		return """
				{
					"fullName": "%s",
					"email": "%s",
					"password": "senha123",
					"phone": "11988887777",
					"role": "%s",
					"active": %s
				}
				""".formatted(nome, email, role, active);
	}
}
