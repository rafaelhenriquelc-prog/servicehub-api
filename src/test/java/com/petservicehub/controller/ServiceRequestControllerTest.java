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
import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.petservicehub.model.OfferedService;
import com.petservicehub.model.RequestStatus;
import com.petservicehub.model.ServiceRequest;
import com.petservicehub.model.User;
import com.petservicehub.model.UserRole;
import com.petservicehub.repository.OfferedServiceRepository;
import com.petservicehub.repository.ReviewRepository;
import com.petservicehub.repository.ServiceRequestRepository;
import com.petservicehub.repository.UserRepository;
import com.petservicehub.util.PasswordHasher;

@SpringBootTest
@AutoConfigureMockMvc
class ServiceRequestControllerTest {

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
	private User client;
	private OfferedService service;

	@BeforeEach
	void limparBase() {
		reviewRepository.deleteAll();
		serviceRequestRepository.deleteAll();
		offeredServiceRepository.deleteAll();
		userRepository.deleteAll();
		provider = salvarUsuario("Carlos Lima", "carlos.lima@email.com", UserRole.PROVIDER);
		client = salvarUsuario("Ana Souza", "ana.souza@email.com", UserRole.CLIENT);
		service = salvarServico(true);
	}

	@Test
	void deveCadastrarSolicitacaoComPrecoDoServico() throws Exception {
		mockMvc.perform(post("/api/service-requests")
						.contentType(MediaType.APPLICATION_JSON)
						.content(requestJson(service.getId(), client.getId(), null, "2026-09-20T14:30:00")))
				.andExpect(status().isCreated())
				.andExpect(header().exists("Location"))
				.andExpect(jsonPath("$.status").value("PENDING"))
				.andExpect(jsonPath("$.totalPrice").value(150.00))
				.andExpect(jsonPath("$.client.id").value(client.getId()))
				.andExpect(jsonPath("$.service.id").value(service.getId()));
	}

	@Test
	void deveRecusarContratacaoPorPrestador() throws Exception {
		mockMvc.perform(post("/api/service-requests")
						.contentType(MediaType.APPLICATION_JSON)
						.content(requestJson(service.getId(), provider.getId(), "PENDING", "2026-09-20T14:30:00")))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.detail").value("Somente um usuário CLIENT pode contratar um serviço"));
	}

	@Test
	void deveRecusarServicoInativo() throws Exception {
		OfferedService inativo = salvarServico(false);

		mockMvc.perform(post("/api/service-requests")
						.contentType(MediaType.APPLICATION_JSON)
						.content(requestJson(inativo.getId(), client.getId(), "PENDING", "2026-09-20T14:30:00")))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.detail").value("O serviço informado está inativo"));
	}

	@Test
	void deveRetornarBadRequestQuandoDadosInvalidos() throws Exception {
		mockMvc.perform(post("/api/service-requests")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{}"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.erros.serviceId").exists())
				.andExpect(jsonPath("$.erros.clientId").exists());
	}

	@Test
	void deveListarSolicitacoesPorStatus() throws Exception {
		salvarSolicitacao(RequestStatus.PENDING);
		salvarSolicitacao(RequestStatus.COMPLETED);

		mockMvc.perform(get("/api/service-requests").param("status", "COMPLETED"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(1)))
				.andExpect(jsonPath("$[0].status").value("COMPLETED"));
	}

	@Test
	void deveBuscarSolicitacaoPorId() throws Exception {
		ServiceRequest request = salvarSolicitacao(RequestStatus.PENDING);

		mockMvc.perform(get("/api/service-requests/{id}", request.getId()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(request.getId()))
				.andExpect(jsonPath("$.client.fullName").value("Ana Souza"));
	}

	@Test
	void deveRetornarNotFoundQuandoSolicitacaoNaoExiste() throws Exception {
		mockMvc.perform(get("/api/service-requests/{id}", 999))
				.andExpect(status().isNotFound());
	}

	@Test
	void deveAtualizarSolicitacaoParaConcluida() throws Exception {
		ServiceRequest request = salvarSolicitacao(RequestStatus.PENDING);

		mockMvc.perform(put("/api/service-requests/{id}", request.getId())
						.contentType(MediaType.APPLICATION_JSON)
						.content(requestJson(service.getId(), client.getId(), "COMPLETED", "2026-09-21T10:00:00")))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("COMPLETED"))
				.andExpect(jsonPath("$.scheduledAt").value("2026-09-21T10:00:00"));
	}

	@Test
	void deveExcluirSolicitacao() throws Exception {
		ServiceRequest request = salvarSolicitacao(RequestStatus.PENDING);

		mockMvc.perform(delete("/api/service-requests/{id}", request.getId()))
				.andExpect(status().isNoContent());

		mockMvc.perform(get("/api/service-requests/{id}", request.getId()))
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

	private OfferedService salvarServico(boolean ativo) {
		return offeredServiceRepository.save(OfferedService.builder()
				.title("Montagem de móveis")
				.description("Montagem residencial")
				.price(new BigDecimal("150.00"))
				.category("Marcenaria")
				.provider(provider)
				.active(ativo)
				.build());
	}

	private ServiceRequest salvarSolicitacao(RequestStatus status) {
		return serviceRequestRepository.save(ServiceRequest.builder()
				.service(service)
				.client(client)
				.status(status)
				.scheduledAt(LocalDateTime.of(2026, 9, 20, 14, 30))
				.notes("Observação")
				.totalPrice(service.getPrice())
				.build());
	}

	private String requestJson(Long serviceId, Long clientId, String status, String scheduledAt) {
		String statusJson = status == null ? "" : "\"status\": \"%s\",".formatted(status);
		return """
				{
					"serviceId": %d,
					"clientId": %d,
					%s
					"scheduledAt": "%s",
					"notes": "Apartamento no 3º andar"
				}
				""".formatted(serviceId, clientId, statusJson, scheduledAt);
	}
}
