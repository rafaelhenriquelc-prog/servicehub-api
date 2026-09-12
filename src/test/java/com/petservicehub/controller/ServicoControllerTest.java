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

import com.petservicehub.model.Servico;
import com.petservicehub.repository.AgendamentoRepository;
import com.petservicehub.repository.ServicoRepository;

@SpringBootTest
@AutoConfigureMockMvc
class ServicoControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ServicoRepository servicoRepository;

	@Autowired
	private AgendamentoRepository agendamentoRepository;

	@BeforeEach
	void limparBase() {
		agendamentoRepository.deleteAll();
		servicoRepository.deleteAll();
	}

	@Test
	void deveCadastrarServico() throws Exception {
		mockMvc.perform(post("/api/servicos")
						.contentType(MediaType.APPLICATION_JSON)
						.content(servicoJson("Banho e tosa", "Banho completo", "89.90", 60, true)))
				.andExpect(status().isCreated())
				.andExpect(header().exists("Location"))
				.andExpect(jsonPath("$.id").exists())
				.andExpect(jsonPath("$.nome").value("Banho e tosa"))
				.andExpect(jsonPath("$.descricao").value("Banho completo"))
				.andExpect(jsonPath("$.preco").value(89.90))
				.andExpect(jsonPath("$.duracaoMinutos").value(60))
				.andExpect(jsonPath("$.ativo").value(true));
	}

	@Test
	void deveRetornarBadRequestQuandoDadosInvalidos() throws Exception {
		mockMvc.perform(post("/api/servicos")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
									"nome": "",
									"preco": -10,
									"duracaoMinutos": 0
								}
								"""))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.title").value("Erro de validação"))
				.andExpect(jsonPath("$.erros.nome").exists())
				.andExpect(jsonPath("$.erros.preco").exists())
				.andExpect(jsonPath("$.erros.duracaoMinutos").exists());
	}

	@Test
	void deveListarServicos() throws Exception {
		salvarServico("Banho e tosa", true);
		salvarServico("Consulta", false);

		mockMvc.perform(get("/api/servicos"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(2)));
	}

	@Test
	void deveFiltrarServicosAtivos() throws Exception {
		salvarServico("Banho e tosa", true);
		salvarServico("Consulta", false);

		mockMvc.perform(get("/api/servicos").param("ativo", "true"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(1)))
				.andExpect(jsonPath("$[0].nome").value("Banho e tosa"));
	}

	@Test
	void deveBuscarServicoPorId() throws Exception {
		Servico servico = salvarServico("Banho e tosa", true);

		mockMvc.perform(get("/api/servicos/{id}", servico.getId()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(servico.getId()))
				.andExpect(jsonPath("$.nome").value("Banho e tosa"));
	}

	@Test
	void deveRetornarNotFoundQuandoServicoNaoExiste() throws Exception {
		mockMvc.perform(get("/api/servicos/{id}", 999))
				.andExpect(status().isNotFound());
	}

	@Test
	void deveAtualizarServico() throws Exception {
		Servico servico = salvarServico("Banho e tosa", true);

		mockMvc.perform(put("/api/servicos/{id}", servico.getId())
						.contentType(MediaType.APPLICATION_JSON)
						.content(servicoJson("Tosa completa", "Tosa da raça", "120.00", 90, false)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.nome").value("Tosa completa"))
				.andExpect(jsonPath("$.descricao").value("Tosa da raça"))
				.andExpect(jsonPath("$.preco").value(120.00))
				.andExpect(jsonPath("$.duracaoMinutos").value(90))
				.andExpect(jsonPath("$.ativo").value(false));
	}

	@Test
	void deveExcluirServico() throws Exception {
		Servico servico = salvarServico("Banho e tosa", true);

		mockMvc.perform(delete("/api/servicos/{id}", servico.getId()))
				.andExpect(status().isNoContent());

		mockMvc.perform(get("/api/servicos/{id}", servico.getId()))
				.andExpect(status().isNotFound());
	}

	@Test
	void deveRetornarNotFoundAoExcluirServicoInexistente() throws Exception {
		mockMvc.perform(delete("/api/servicos/{id}", 999))
				.andExpect(status().isNotFound());
	}

	private Servico salvarServico(String nome, boolean ativo) {
		return servicoRepository.save(Servico.builder()
				.nome(nome)
				.descricao("Descrição")
				.preco(new BigDecimal("80.00"))
				.duracaoMinutos(45)
				.ativo(ativo)
				.build());
	}

	private String servicoJson(String nome, String descricao, String preco, int duracaoMinutos, boolean ativo) {
		return """
				{
					"nome": "%s",
					"descricao": "%s",
					"preco": %s,
					"duracaoMinutos": %d,
					"ativo": %s
				}
				""".formatted(nome, descricao, preco, duracaoMinutos, ativo);
	}
}
