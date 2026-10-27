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

import com.petservicehub.model.OfferedService;
import com.petservicehub.model.ServiceRequest;
import com.petservicehub.repository.OfferedServiceRepository;
import com.petservicehub.repository.ReviewRepository;
import com.petservicehub.repository.ServiceRequestRepository;
import com.petservicehub.repository.UserRepository;
import com.petservicehub.support.AuthTestSupport;

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

	private String clientToken;
	private String providerToken;
	private OfferedService service;

	@BeforeEach
	void limparBase() throws Exception {
		reviewRepository.deleteAll();
		serviceRequestRepository.deleteAll();
		offeredServiceRepository.deleteAll();
		userRepository.deleteAll();
		providerToken = AuthTestSupport.registerAndLogin(mockMvc, "Carlos Lima", "carlos.lima@email.com", "PROVIDER");
		clientToken = AuthTestSupport.registerAndLogin(mockMvc, "Ana Souza", "ana.souza@email.com", "CLIENT");
		mockMvc.perform(post("/api/services")
						.header("Authorization", "Bearer " + providerToken)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
									"title": "Montagem de móveis",
									"description": "Montagem residencial",
									"price": 150.00,
									"category": "Marcenaria",
									"active": true
								}
								"""))
				.andExpect(status().isCreated());
		service = offeredServiceRepository.findAll().getFirst();
	}

	@Test
	void deveCadastrarSolicitacaoComPrecoDoServico() throws Exception {
		mockMvc.perform(post("/api/service-requests")
						.header("Authorization", "Bearer " + clientToken)
						.contentType(MediaType.APPLICATION_JSON)
						.content(requestJson(service.getId(), null, "2026-09-20T14:30:00")))
				.andExpect(status().isCreated())
				.andExpect(header().exists("Location"))
				.andExpect(jsonPath("$.status").value("PENDING"))
				.andExpect(jsonPath("$.totalPrice").value(150.00))
				.andExpect(jsonPath("$.client.email").value("ana.souza@email.com"));
	}

	@Test
	void deveRecusarContratacaoPorPrestador() throws Exception {
		mockMvc.perform(post("/api/service-requests")
						.header("Authorization", "Bearer " + providerToken)
						.contentType(MediaType.APPLICATION_JSON)
						.content(requestJson(service.getId(), "PENDING", "2026-09-20T14:30:00")))
				.andExpect(status().isForbidden());
	}

	@Test
	void deveRetornarBadRequestQuandoDadosInvalidos() throws Exception {
		mockMvc.perform(post("/api/service-requests")
						.header("Authorization", "Bearer " + clientToken)
						.contentType(MediaType.APPLICATION_JSON)
						.content("{}"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.erros.serviceId").exists());
	}

	@Test
	void deveListarSomenteSolicitacoesDoCliente() throws Exception {
		criarSolicitacao();
		mockMvc.perform(get("/api/service-requests")
						.header("Authorization", "Bearer " + clientToken))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(1)));
	}

	@Test
	void deveBuscarSolicitacaoPorId() throws Exception {
		criarSolicitacao();
		ServiceRequest request = serviceRequestRepository.findAll().getFirst();

		mockMvc.perform(get("/api/service-requests/{id}", request.getId())
						.header("Authorization", "Bearer " + clientToken))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.client.fullName").value("Ana Souza"));
	}

	@Test
	void deveRetornarNotFoundQuandoSolicitacaoNaoExiste() throws Exception {
		mockMvc.perform(get("/api/service-requests/{id}", 999)
						.header("Authorization", "Bearer " + clientToken))
				.andExpect(status().isNotFound());
	}

	@Test
	void deveAtualizarSolicitacaoParaConcluidaComoProvider() throws Exception {
		criarSolicitacao();
		ServiceRequest request = serviceRequestRepository.findAll().getFirst();

		mockMvc.perform(put("/api/service-requests/{id}", request.getId())
						.header("Authorization", "Bearer " + providerToken)
						.contentType(MediaType.APPLICATION_JSON)
						.content(requestJson(service.getId(), "COMPLETED", "2026-09-21T10:00:00")))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("COMPLETED"));
	}

	@Test
	void deveExcluirSolicitacao() throws Exception {
		criarSolicitacao();
		ServiceRequest request = serviceRequestRepository.findAll().getFirst();

		mockMvc.perform(delete("/api/service-requests/{id}", request.getId())
						.header("Authorization", "Bearer " + clientToken))
				.andExpect(status().isNoContent());
	}

	private void criarSolicitacao() throws Exception {
		mockMvc.perform(post("/api/service-requests")
						.header("Authorization", "Bearer " + clientToken)
						.contentType(MediaType.APPLICATION_JSON)
						.content(requestJson(service.getId(), null, "2026-09-20T14:30:00")))
				.andExpect(status().isCreated());
	}

	private String requestJson(Long serviceId, String status, String scheduledAt) {
		String statusJson = status == null ? "" : "\"status\": \"%s\",".formatted(status);
		return """
				{
					"serviceId": %d,
					%s
					"scheduledAt": "%s",
					"notes": "Apartamento no 3º andar"
				}
				""".formatted(serviceId, statusJson, scheduledAt);
	}
}
