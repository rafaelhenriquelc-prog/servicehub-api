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
import org.springframework.test.web.servlet.MockMvc;

import com.petservicehub.model.User;
import com.petservicehub.model.UserRole;
import com.petservicehub.repository.OfferedServiceRepository;
import com.petservicehub.repository.ReviewRepository;
import com.petservicehub.repository.ServiceRequestRepository;
import com.petservicehub.repository.UserRepository;
import com.petservicehub.util.PasswordHasher;

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

	@BeforeEach
	void limparBase() {
		reviewRepository.deleteAll();
		serviceRequestRepository.deleteAll();
		offeredServiceRepository.deleteAll();
		userRepository.deleteAll();
	}

	@Test
	void deveCadastrarUsuario() throws Exception {
		mockMvc.perform(post("/api/users")
						.contentType(MediaType.APPLICATION_JSON)
						.content(userJson("Ana Souza", "ana.souza@email.com", "CLIENT", true)))
				.andExpect(status().isCreated())
				.andExpect(header().exists("Location"))
				.andExpect(jsonPath("$.id").exists())
				.andExpect(jsonPath("$.fullName").value("Ana Souza"))
				.andExpect(jsonPath("$.email").value("ana.souza@email.com"))
				.andExpect(jsonPath("$.role").value("CLIENT"))
				.andExpect(jsonPath("$.active").value(true))
				.andExpect(jsonPath("$.password").doesNotExist())
				.andExpect(jsonPath("$.passwordHash").doesNotExist());
	}

	@Test
	void deveRetornarBadRequestQuandoDadosInvalidos() throws Exception {
		mockMvc.perform(post("/api/users")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
									"fullName": "",
									"email": "invalido",
									"password": "123"
								}
								"""))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.title").value("Erro de validação"))
				.andExpect(jsonPath("$.erros.fullName").exists())
				.andExpect(jsonPath("$.erros.email").exists())
				.andExpect(jsonPath("$.erros.password").exists())
				.andExpect(jsonPath("$.erros.role").exists());
	}

	@Test
	void deveRecusarEmailDuplicado() throws Exception {
		salvarUsuario("Ana Souza", "ana.souza@email.com", UserRole.CLIENT);

		mockMvc.perform(post("/api/users")
						.contentType(MediaType.APPLICATION_JSON)
						.content(userJson("Ana Outra", "ana.souza@email.com", "CLIENT", true)))
				.andExpect(status().isConflict());
	}

	@Test
	void deveListarUsuariosPorPerfil() throws Exception {
		salvarUsuario("Ana Souza", "ana.souza@email.com", UserRole.CLIENT);
		salvarUsuario("Carlos Lima", "carlos.lima@email.com", UserRole.PROVIDER);

		mockMvc.perform(get("/api/users").param("role", "PROVIDER"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(1)))
				.andExpect(jsonPath("$[0].fullName").value("Carlos Lima"));
	}

	@Test
	void deveBuscarUsuarioPorId() throws Exception {
		User user = salvarUsuario("Ana Souza", "ana.souza@email.com", UserRole.CLIENT);

		mockMvc.perform(get("/api/users/{id}", user.getId()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(user.getId()))
				.andExpect(jsonPath("$.fullName").value("Ana Souza"));
	}

	@Test
	void deveRetornarNotFoundQuandoUsuarioNaoExiste() throws Exception {
		mockMvc.perform(get("/api/users/{id}", 999))
				.andExpect(status().isNotFound());
	}

	@Test
	void deveAtualizarUsuario() throws Exception {
		User user = salvarUsuario("Ana Souza", "ana.souza@email.com", UserRole.CLIENT);

		mockMvc.perform(put("/api/users/{id}", user.getId())
						.contentType(MediaType.APPLICATION_JSON)
						.content(userJson("Ana Atualizada", "ana.atualizada@email.com", "CLIENT", false)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.fullName").value("Ana Atualizada"))
				.andExpect(jsonPath("$.email").value("ana.atualizada@email.com"))
				.andExpect(jsonPath("$.active").value(false));
	}

	@Test
	void deveExcluirUsuario() throws Exception {
		User user = salvarUsuario("Ana Souza", "ana.souza@email.com", UserRole.CLIENT);

		mockMvc.perform(delete("/api/users/{id}", user.getId()))
				.andExpect(status().isNoContent());

		mockMvc.perform(get("/api/users/{id}", user.getId()))
				.andExpect(status().isNotFound());
	}

	@Test
	void deveExporDocumentacaoOpenApi() throws Exception {
		mockMvc.perform(get("/v3/api-docs"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.info.title").value("PetServiceHub API"))
				.andExpect(jsonPath("$.paths['/api/users']").exists())
				.andExpect(jsonPath("$.paths['/api/services']").exists())
				.andExpect(jsonPath("$.paths['/api/service-requests']").exists())
				.andExpect(jsonPath("$.paths['/api/reviews']").exists());
	}

	private User salvarUsuario(String nome, String email, UserRole role) {
		return userRepository.save(User.builder()
				.fullName(nome)
				.email(email)
				.passwordHash(PasswordHasher.hash("senha123"))
				.phone("11988887777")
				.role(role)
				.active(true)
				.build());
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
