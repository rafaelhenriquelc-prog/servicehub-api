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
import com.petservicehub.repository.OfferedServiceRepository;
import com.petservicehub.repository.ReviewRepository;
import com.petservicehub.repository.ServiceRequestRepository;
import com.petservicehub.repository.UserRepository;
import com.petservicehub.support.AuthTestSupport;

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

	private String providerToken;

	@BeforeEach
	void limparBase() throws Exception {
		reviewRepository.deleteAll();
		serviceRequestRepository.deleteAll();
		offeredServiceRepository.deleteAll();
		userRepository.deleteAll();
		providerToken = AuthTestSupport.registerAndLogin(mockMvc, "Carlos Lima", "carlos.lima@email.com", "PROVIDER");
	}

	@Test
	void deveCadastrarServicoComoProvider() throws Exception {
		mockMvc.perform(post("/api/services")
						.header("Authorization", "Bearer " + providerToken)
						.contentType(MediaType.APPLICATION_JSON)
						.content(serviceJson("Montagem de móveis", "Montagem residencial", "150.00", "Marcenaria", true)))
				.andExpect(status().isCreated())
				.andExpect(header().exists("Location"))
				.andExpect(jsonPath("$.title").value("Montagem de móveis"))
				.andExpect(jsonPath("$.provider.email").value("carlos.lima@email.com"))
				.andExpect(jsonPath("$.active").value(true));
	}

	@Test
	void deveRetornarBadRequestQuandoDadosInvalidos() throws Exception {
		mockMvc.perform(post("/api/services")
						.header("Authorization", "Bearer " + providerToken)
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
				.andExpect(jsonPath("$.erros.category").exists());
	}

	@Test
	void deveListarServicosPublicamente() throws Exception {
		criarServicoViaApi("Montagem de móveis", "Marcenaria");
		criarServicoViaApi("Instalação elétrica", "Elétrica");

		mockMvc.perform(get("/api/services").param("category", "Marcenaria"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(1)))
				.andExpect(jsonPath("$[0].title").value("Montagem de móveis"));
	}

	@Test
	void deveBuscarServicoPorIdSemToken() throws Exception {
		criarServicoViaApi("Montagem de móveis", "Marcenaria");
		OfferedService service = offeredServiceRepository.findAll().getFirst();

		mockMvc.perform(get("/api/services/{id}", service.getId()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.title").value("Montagem de móveis"));
	}

	@Test
	void deveRetornarNotFoundQuandoServicoNaoExiste() throws Exception {
		mockMvc.perform(get("/api/services/{id}", 999))
				.andExpect(status().isNotFound());
	}

	@Test
	void deveAtualizarServicoProprio() throws Exception {
		criarServicoViaApi("Montagem de móveis", "Marcenaria");
		OfferedService service = offeredServiceRepository.findAll().getFirst();

		mockMvc.perform(put("/api/services/{id}", service.getId())
						.header("Authorization", "Bearer " + providerToken)
						.contentType(MediaType.APPLICATION_JSON)
						.content(serviceJson("Montagem completa", "Montagem e nivelamento", "180.00", "Marcenaria", false)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.title").value("Montagem completa"))
				.andExpect(jsonPath("$.active").value(false));
	}

	@Test
	void deveExcluirServicoProprio() throws Exception {
		criarServicoViaApi("Montagem de móveis", "Marcenaria");
		OfferedService service = offeredServiceRepository.findAll().getFirst();

		mockMvc.perform(delete("/api/services/{id}", service.getId())
						.header("Authorization", "Bearer " + providerToken))
				.andExpect(status().isNoContent());
	}

	private void criarServicoViaApi(String titulo, String categoria) throws Exception {
		mockMvc.perform(post("/api/services")
						.header("Authorization", "Bearer " + providerToken)
						.contentType(MediaType.APPLICATION_JSON)
						.content(serviceJson(titulo, "Descrição", "150.00", categoria, true)))
				.andExpect(status().isCreated());
	}

	private String serviceJson(String title, String description, String price, String category, boolean active) {
		return """
				{
					"title": "%s",
					"description": "%s",
					"price": %s,
					"category": "%s",
					"active": %s
				}
				""".formatted(title, description, price, category, active);
	}
}
