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
import com.petservicehub.model.Review;
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

	private User client;
	private User provider;
	private OfferedService service;
	private ServiceRequest completedRequest;

	@BeforeEach
	void limparBase() {
		reviewRepository.deleteAll();
		serviceRequestRepository.deleteAll();
		offeredServiceRepository.deleteAll();
		userRepository.deleteAll();
		provider = salvarUsuario("Carlos Lima", "carlos.lima@email.com", UserRole.PROVIDER);
		client = salvarUsuario("Ana Souza", "ana.souza@email.com", UserRole.CLIENT);
		service = offeredServiceRepository.save(OfferedService.builder()
				.title("Montagem de móveis")
				.description("Montagem residencial")
				.price(new BigDecimal("150.00"))
				.category("Marcenaria")
				.provider(provider)
				.active(true)
				.build());
		completedRequest = salvarSolicitacao(RequestStatus.COMPLETED);
	}

	@Test
	void deveCadastrarAvaliacao() throws Exception {
		mockMvc.perform(post("/api/reviews")
						.contentType(MediaType.APPLICATION_JSON)
						.content(reviewJson(completedRequest.getId(), client.getId(), 5, "Excelente")))
				.andExpect(status().isCreated())
				.andExpect(header().exists("Location"))
				.andExpect(jsonPath("$.rating").value(5))
				.andExpect(jsonPath("$.comment").value("Excelente"))
				.andExpect(jsonPath("$.requestId").value(completedRequest.getId()))
				.andExpect(jsonPath("$.reviewer.id").value(client.getId()));
	}

	@Test
	void deveRecusarAvaliacaoDeSolicitacaoNaoConcluida() throws Exception {
		ServiceRequest pending = salvarSolicitacao(RequestStatus.PENDING);

		mockMvc.perform(post("/api/reviews")
						.contentType(MediaType.APPLICATION_JSON)
						.content(reviewJson(pending.getId(), client.getId(), 5, "Excelente")))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.detail").value("A avaliação só pode ser cadastrada para uma solicitação COMPLETED"));
	}

	@Test
	void deveRecusarAvaliadorQueNaoEOcliente() throws Exception {
		mockMvc.perform(post("/api/reviews")
						.contentType(MediaType.APPLICATION_JSON)
						.content(reviewJson(completedRequest.getId(), provider.getId(), 5, "Excelente")))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.detail").value("O avaliador deve ser o cliente da solicitação"));
	}

	@Test
	void deveRecusarSegundaAvaliacaoNaMesmaSolicitacao() throws Exception {
		salvarAvaliacao();

		mockMvc.perform(post("/api/reviews")
						.contentType(MediaType.APPLICATION_JSON)
						.content(reviewJson(completedRequest.getId(), client.getId(), 4, "Outra")))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.detail").value("Esta solicitação já possui uma avaliação"));
	}

	@Test
	void deveRetornarBadRequestQuandoNotaInvalida() throws Exception {
		mockMvc.perform(post("/api/reviews")
						.contentType(MediaType.APPLICATION_JSON)
						.content(reviewJson(completedRequest.getId(), client.getId(), 6, "Nota alta")))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.erros.rating").exists());
	}

	@Test
	void deveListarAvaliacoes() throws Exception {
		salvarAvaliacao();

		mockMvc.perform(get("/api/reviews"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(1)));
	}

	@Test
	void deveBuscarAvaliacaoPorId() throws Exception {
		Review review = salvarAvaliacao();

		mockMvc.perform(get("/api/reviews/{id}", review.getId()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(review.getId()))
				.andExpect(jsonPath("$.rating").value(5));
	}

	@Test
	void deveRetornarNotFoundQuandoAvaliacaoNaoExiste() throws Exception {
		mockMvc.perform(get("/api/reviews/{id}", 999))
				.andExpect(status().isNotFound());
	}

	@Test
	void deveAtualizarAvaliacao() throws Exception {
		Review review = salvarAvaliacao();

		mockMvc.perform(put("/api/reviews/{id}", review.getId())
						.contentType(MediaType.APPLICATION_JSON)
						.content(reviewJson(completedRequest.getId(), client.getId(), 4, "Atualizado")))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.rating").value(4))
				.andExpect(jsonPath("$.comment").value("Atualizado"));
	}

	@Test
	void deveExcluirAvaliacao() throws Exception {
		Review review = salvarAvaliacao();

		mockMvc.perform(delete("/api/reviews/{id}", review.getId()))
				.andExpect(status().isNoContent());

		mockMvc.perform(get("/api/reviews/{id}", review.getId()))
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

	private ServiceRequest salvarSolicitacao(RequestStatus status) {
		return serviceRequestRepository.save(ServiceRequest.builder()
				.service(service)
				.client(client)
				.status(status)
				.scheduledAt(LocalDateTime.of(2026, 9, 20, 14, 30))
				.totalPrice(service.getPrice())
				.build());
	}

	private Review salvarAvaliacao() {
		return reviewRepository.save(Review.builder()
				.request(completedRequest)
				.reviewer(client)
				.rating(5)
				.comment("Excelente")
				.build());
	}

	private String reviewJson(Long requestId, Long reviewerId, int rating, String comment) {
		return """
				{
					"requestId": %d,
					"reviewerId": %d,
					"rating": %d,
					"comment": "%s"
				}
				""".formatted(requestId, reviewerId, rating, comment);
	}
}
