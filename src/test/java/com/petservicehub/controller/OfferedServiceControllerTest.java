package com.petservicehub.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.petservicehub.model.OfferedService;
import com.petservicehub.model.User;
import com.petservicehub.model.UserRole;
import com.petservicehub.repository.OfferedServiceRepository;
import com.petservicehub.repository.ReviewRepository;
import com.petservicehub.repository.ServiceRequestRepository;
import com.petservicehub.repository.UserRepository;
import com.petservicehub.util.PasswordHasher;

@SpringBootTest
@AutoConfigureMockMvc
class OfferedServiceControllerTest {

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

	private User provider;

	@BeforeEach
	void limparBase() {
		reviewRepository.deleteAll();
		serviceRequestRepository.deleteAll();
		offeredServiceRepository.deleteAll();
		userRepository.deleteAll();
		provider = salvarUsuario("Carlos Lima", "carlos.lima@email.com", UserRole.PROVIDER);
	}

	@Test
	void deveCadastrarServico() throws Exception {
		mockMvc.perform(post("/api/services")
						.contentType(MediaType.APPLICATION_JSON)
						.content(serviceJson("Montagem de móveis", "Montagem residencial", "150.00", "Marcenaria", provider.getId(), true)))
				.andExpect(status().isCreated())
				.andExpect(header().exists("Location"))
				.andExpect(jsonPath("$.title").value("Montagem de móveis"))
				.andExpect(jsonPath("$.price").value(150.00))
				.andExpect(jsonPath("$.category").value("Marcenaria"))
				.andExpect(jsonPath("$.provider.id").value(provider.getId()))
				.andExpect(jsonPath("$.active").value(true));
	}

	@Test
	void deveRecusarServicoComClienteComoPrestador() throws Exception {
		User client = salvarUsuario("Ana Souza", "ana.souza@email.com", UserRole.CLIENT);

		mockMvc.perform(post("/api/services")
						.contentType(MediaType.APPLICATION_JSON)
						.content(serviceJson("Montagem de móveis", "Montagem residencial", "150.00", "Marcenaria", client.getId(), true)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.detail").value("Somente um usuário PROVIDER pode ser associado como prestador"));
	}

	@Test
	void deveRetornarBadRequestQuandoDadosInvalidos() throws Exception {
		mockMvc.perform(post("/api/services")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
									"title": "",
									"price": -10
								}
								"""))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.erros.title").exists())
				.andExpect(jsonPath("$.erros.description").exists())
				.andExpect(jsonPath("$.erros.price").exists())
				.andExpect(jsonPath("$.erros.category").exists())
				.andExpect(jsonPath("$.erros.providerId").exists());
	}

	@Test
	void deveListarServicosPorCategoria() throws Exception {
		salvarServico("Montagem de móveis", "Marcenaria", true);
		salvarServico("Instalação elétrica", "Elétrica", true);

		mockMvc.perform(get("/api/services").param("category", "Marcenaria"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(1)))
				.andExpect(jsonPath("$[0].title").value("Montagem de móveis"));
	}

	@Test
	void deveBuscarServicoPorId() throws Exception {
		OfferedService service = salvarServico("Montagem de móveis", "Marcenaria", true);

		mockMvc.perform(get("/api/services/{id}", service.getId()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(service.getId()))
				.andExpect(jsonPath("$.provider.fullName").value("Carlos Lima"));
	}

	@Test
	void deveRetornarNotFoundQuandoServicoNaoExiste() throws Exception {
		mockMvc.perform(get("/api/services/{id}", 999))
				.andExpect(status().isNotFound());
	}

	@Test
	void deveAtualizarServico() throws Exception {
		OfferedService service = salvarServico("Montagem de móveis", "Marcenaria", true);

		mockMvc.perform(put("/api/services/{id}", service.getId())
						.contentType(MediaType.APPLICATION_JSON)
						.content(serviceJson("Montagem completa", "Montagem e nivelamento", "180.00", "Marcenaria", provider.getId(), false)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.title").value("Montagem completa"))
				.andExpect(jsonPath("$.price").value(180.00))
				.andExpect(jsonPath("$.active").value(false));
	}

	@Test
	void deveExcluirServico() throws Exception {
		OfferedService service = salvarServico("Montagem de móveis", "Marcenaria", true);

		mockMvc.perform(delete("/api/services/{id}", service.getId()))
				.andExpect(status().isNoContent());

		mockMvc.perform(get("/api/services/{id}", service.getId()))
				.andExpect(status().isNotFound());
	}

	private User salvarUsuario(String nome, String email, UserRole role) {
		return userRepository.save(User.builder()
				.fullName(nome)
				.email(email)
				.passwordHash(PasswordHasher.hash("senha123"))
				.role(role)
				.active(true)
				.build());
	}

	private OfferedService salvarServico(String titulo, String categoria, boolean ativo) {
		return offeredServiceRepository.save(OfferedService.builder()
				.title(titulo)
				.description("Descrição")
				.price(new BigDecimal("150.00"))
				.category(categoria)
				.provider(provider)
				.active(ativo)
				.build());
	}

	private String serviceJson(String title, String description, String price, String category, Long providerId, boolean active) {
		return """
				{
					"title": "%s",
					"description": "%s",
					"price": %s,
					"category": "%s",
					"providerId": %d,
					"active": %s
				}
				""".formatted(title, description, price, category, providerId, active);
	}
}
