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
import com.petservicehub.model.Review;
import com.petservicehub.model.ServiceRequest;
import com.petservicehub.repository.OfferedServiceRepository;
import com.petservicehub.repository.ReviewRepository;
import com.petservicehub.repository.ServiceRequestRepository;
import com.petservicehub.repository.UserRepository;
import com.petservicehub.support.AuthTestSupport;

@SpringBootTest
@AutoConfigureMockMvc
class ReviewControllerTest {

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
	private ServiceRequest completedRequest;

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
		OfferedService service = offeredServiceRepository.findAll().getFirst();
		mockMvc.perform(post("/api/service-requests")
						.header("Authorization", "Bearer " + clientToken)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
									"serviceId": %d,
									"scheduledAt": "2026-09-20T14:30:00"
								}
								""".formatted(service.getId())))
				.andExpect(status().isCreated());
		completedRequest = serviceRequestRepository.findAll().getFirst();
		mockMvc.perform(put("/api/service-requests/{id}", completedRequest.getId())
						.header("Authorization", "Bearer " + providerToken)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
									"serviceId": %d,
									"status": "COMPLETED"
								}
								""".formatted(service.getId())))
				.andExpect(status().isOk());
		completedRequest = serviceRequestRepository.findById(completedRequest.getId()).orElseThrow();
	}

	@Test
	void deveCadastrarAvaliacao() throws Exception {
		mockMvc.perform(post("/api/reviews")
						.header("Authorization", "Bearer " + clientToken)
						.contentType(MediaType.APPLICATION_JSON)
						.content(reviewJson(completedRequest.getId(), 5, "Excelente")))
				.andExpect(status().isCreated())
				.andExpect(header().exists("Location"))
				.andExpect(jsonPath("$.rating").value(5))
				.andExpect(jsonPath("$.reviewer.email").value("ana.souza@email.com"));
	}

	@Test
	void deveRecusarAvaliacaoDeSolicitacaoNaoConcluida() throws Exception {
		OfferedService service = offeredServiceRepository.findAll().getFirst();
		mockMvc.perform(post("/api/service-requests")
						.header("Authorization", "Bearer " + clientToken)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"serviceId": %d, "scheduledAt": "2026-09-21T10:00:00"}
								""".formatted(service.getId())))
				.andExpect(status().isCreated());
		ServiceRequest pending = serviceRequestRepository.findAll().stream()
				.filter(item -> item.getId() != completedRequest.getId())
				.findFirst()
				.orElseThrow();

		mockMvc.perform(post("/api/reviews")
						.header("Authorization", "Bearer " + clientToken)
						.contentType(MediaType.APPLICATION_JSON)
						.content(reviewJson(pending.getId(), 5, "Excelente")))
				.andExpect(status().isBadRequest());
	}

	@Test
	void deveRecusarSegundaAvaliacaoNaMesmaSolicitacao() throws Exception {
		criarAvaliacao();
		mockMvc.perform(post("/api/reviews")
						.header("Authorization", "Bearer " + clientToken)
						.contentType(MediaType.APPLICATION_JSON)
						.content(reviewJson(completedRequest.getId(), 4, "Outra")))
				.andExpect(status().isConflict());
	}

	@Test
	void deveRetornarBadRequestQuandoNotaInvalida() throws Exception {
		mockMvc.perform(post("/api/reviews")
						.header("Authorization", "Bearer " + clientToken)
						.contentType(MediaType.APPLICATION_JSON)
						.content(reviewJson(completedRequest.getId(), 6, "Nota alta")))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.erros.rating").exists());
	}

	@Test
	void deveListarAvaliacoes() throws Exception {
		criarAvaliacao();
		mockMvc.perform(get("/api/reviews")
						.header("Authorization", "Bearer " + clientToken))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(1)));
	}

	@Test
	void deveBuscarAvaliacaoPorId() throws Exception {
		criarAvaliacao();
		Review review = reviewRepository.findAll().getFirst();
		mockMvc.perform(get("/api/reviews/{id}", review.getId())
						.header("Authorization", "Bearer " + clientToken))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.rating").value(5));
	}

	@Test
	void deveRetornarNotFoundQuandoAvaliacaoNaoExiste() throws Exception {
		mockMvc.perform(get("/api/reviews/{id}", 999)
						.header("Authorization", "Bearer " + clientToken))
				.andExpect(status().isNotFound());
	}

	@Test
	void deveAtualizarAvaliacao() throws Exception {
		criarAvaliacao();
		Review review = reviewRepository.findAll().getFirst();
		mockMvc.perform(put("/api/reviews/{id}", review.getId())
						.header("Authorization", "Bearer " + clientToken)
						.contentType(MediaType.APPLICATION_JSON)
						.content(reviewJson(completedRequest.getId(), 4, "Atualizado")))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.rating").value(4));
	}

	@Test
	void deveExcluirAvaliacao() throws Exception {
		criarAvaliacao();
		Review review = reviewRepository.findAll().getFirst();
		mockMvc.perform(delete("/api/reviews/{id}", review.getId())
						.header("Authorization", "Bearer " + clientToken))
				.andExpect(status().isNoContent());
	}

	private void criarAvaliacao() throws Exception {
		mockMvc.perform(post("/api/reviews")
						.header("Authorization", "Bearer " + clientToken)
						.contentType(MediaType.APPLICATION_JSON)
						.content(reviewJson(completedRequest.getId(), 5, "Excelente")))
				.andExpect(status().isCreated());
	}

	private String reviewJson(Long requestId, int rating, String comment) {
		return """
				{
					"requestId": %d,
					"rating": %d,
					"comment": "%s"
				}
				""".formatted(requestId, rating, comment);
	}
}
