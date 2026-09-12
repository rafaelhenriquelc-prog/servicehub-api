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

import com.petservicehub.model.Agendamento;
import com.petservicehub.model.Pet;
import com.petservicehub.model.Servico;
import com.petservicehub.model.StatusAgendamento;
import com.petservicehub.model.Tutor;
import com.petservicehub.repository.AgendamentoRepository;
import com.petservicehub.repository.PetRepository;
import com.petservicehub.repository.ServicoRepository;
import com.petservicehub.repository.TutorRepository;

@SpringBootTest
@AutoConfigureMockMvc
class AgendamentoControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private AgendamentoRepository agendamentoRepository;

	@Autowired
	private PetRepository petRepository;

	@Autowired
	private TutorRepository tutorRepository;

	@Autowired
	private ServicoRepository servicoRepository;

	private Tutor tutor;
	private Pet pet;
	private Servico servico;

	@BeforeEach
	void limparBase() {
		agendamentoRepository.deleteAll();
		petRepository.deleteAll();
		servicoRepository.deleteAll();
		tutorRepository.deleteAll();

		tutor = tutorRepository.save(Tutor.builder()
				.nome("Ana Souza")
				.email("ana.souza@email.com")
				.telefone("11988887777")
				.ativo(true)
				.build());
		pet = petRepository.save(Pet.builder()
				.nome("Thor")
				.especie("Cachorro")
				.raca("Labrador")
				.idade(4)
				.tutor(tutor)
				.ativo(true)
				.build());
		servico = servicoRepository.save(Servico.builder()
				.nome("Banho e tosa")
				.descricao("Banho completo")
				.preco(new BigDecimal("89.90"))
				.duracaoMinutos(60)
				.ativo(true)
				.build());
	}

	@Test
	void deveCadastrarAgendamento() throws Exception {
		mockMvc.perform(post("/api/agendamentos")
						.contentType(MediaType.APPLICATION_JSON)
						.content(agendamentoJson(pet.getId(), tutor.getId(), servico.getId(),
								"2026-12-15T14:30:00", "Pet fica nervoso", "AGENDADO")))
				.andExpect(status().isCreated())
				.andExpect(header().exists("Location"))
				.andExpect(jsonPath("$.id").exists())
				.andExpect(jsonPath("$.pet.id").value(pet.getId()))
				.andExpect(jsonPath("$.tutor.id").value(tutor.getId()))
				.andExpect(jsonPath("$.servico.id").value(servico.getId()))
				.andExpect(jsonPath("$.dataHora").value("2026-12-15T14:30:00"))
				.andExpect(jsonPath("$.observacao").value("Pet fica nervoso"))
				.andExpect(jsonPath("$.status").value("AGENDADO"));
	}

	@Test
	void deveAssumirStatusAgendadoQuandoNaoInformado() throws Exception {
		mockMvc.perform(post("/api/agendamentos")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
									"petId": %d,
									"tutorId": %d,
									"servicoId": %d,
									"dataHora": "2026-12-15T14:30:00"
								}
								""".formatted(pet.getId(), tutor.getId(), servico.getId())))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.status").value("AGENDADO"));
	}

	@Test
	void deveRetornarBadRequestQuandoDadosInvalidos() throws Exception {
		mockMvc.perform(post("/api/agendamentos")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{}"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.title").value("Erro de validação"))
				.andExpect(jsonPath("$.erros.petId").exists())
				.andExpect(jsonPath("$.erros.tutorId").exists())
				.andExpect(jsonPath("$.erros.servicoId").exists())
				.andExpect(jsonPath("$.erros.dataHora").exists());
	}

	@Test
	void deveRecusarAgendamentoQuandoPetNaoPertenceAoTutor() throws Exception {
		Tutor outroTutor = tutorRepository.save(Tutor.builder()
				.nome("Carlos Lima")
				.email("carlos.lima@email.com")
				.telefone("11911112222")
				.ativo(true)
				.build());

		mockMvc.perform(post("/api/agendamentos")
						.contentType(MediaType.APPLICATION_JSON)
						.content(agendamentoJson(pet.getId(), outroTutor.getId(), servico.getId(),
								"2026-12-15T14:30:00", null, "AGENDADO")))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.detail").value("O pet informado não pertence ao tutor informado"));
	}

	@Test
	void deveListarAgendamentos() throws Exception {
		salvarAgendamento(StatusAgendamento.AGENDADO);
		salvarAgendamento(StatusAgendamento.CONCLUIDO);

		mockMvc.perform(get("/api/agendamentos"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(2)));
	}

	@Test
	void deveFiltrarAgendamentosPorStatus() throws Exception {
		salvarAgendamento(StatusAgendamento.AGENDADO);
		salvarAgendamento(StatusAgendamento.CANCELADO);

		mockMvc.perform(get("/api/agendamentos").param("status", "AGENDADO"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(1)))
				.andExpect(jsonPath("$[0].status").value("AGENDADO"));
	}

	@Test
	void deveBuscarAgendamentoPorId() throws Exception {
		Agendamento agendamento = salvarAgendamento(StatusAgendamento.AGENDADO);

		mockMvc.perform(get("/api/agendamentos/{id}", agendamento.getId()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(agendamento.getId()))
				.andExpect(jsonPath("$.pet.nome").value("Thor"))
				.andExpect(jsonPath("$.tutor.nome").value("Ana Souza"))
				.andExpect(jsonPath("$.servico.nome").value("Banho e tosa"));
	}

	@Test
	void deveRetornarNotFoundQuandoAgendamentoNaoExiste() throws Exception {
		mockMvc.perform(get("/api/agendamentos/{id}", 999))
				.andExpect(status().isNotFound());
	}

	@Test
	void deveAtualizarAgendamento() throws Exception {
		Agendamento agendamento = salvarAgendamento(StatusAgendamento.AGENDADO);

		mockMvc.perform(put("/api/agendamentos/{id}", agendamento.getId())
						.contentType(MediaType.APPLICATION_JSON)
						.content(agendamentoJson(pet.getId(), tutor.getId(), servico.getId(),
								"2026-12-20T09:00:00", "Atualizado", "CONCLUIDO")))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.dataHora").value("2026-12-20T09:00:00"))
				.andExpect(jsonPath("$.observacao").value("Atualizado"))
				.andExpect(jsonPath("$.status").value("CONCLUIDO"));
	}

	@Test
	void deveExcluirAgendamento() throws Exception {
		Agendamento agendamento = salvarAgendamento(StatusAgendamento.AGENDADO);

		mockMvc.perform(delete("/api/agendamentos/{id}", agendamento.getId()))
				.andExpect(status().isNoContent());

		mockMvc.perform(get("/api/agendamentos/{id}", agendamento.getId()))
				.andExpect(status().isNotFound());
	}

	@Test
	void deveRetornarNotFoundAoExcluirAgendamentoInexistente() throws Exception {
		mockMvc.perform(delete("/api/agendamentos/{id}", 999))
				.andExpect(status().isNotFound());
	}

	private Agendamento salvarAgendamento(StatusAgendamento status) {
		return agendamentoRepository.save(Agendamento.builder()
				.pet(pet)
				.tutor(tutor)
				.servico(servico)
				.dataHora(LocalDateTime.of(2026, 12, 15, 14, 30))
				.observacao("Observação")
				.status(status)
				.build());
	}

	private String agendamentoJson(Long petId, Long tutorId, Long servicoId, String dataHora, String observacao, String status) {
		return """
				{
					"petId": %d,
					"tutorId": %d,
					"servicoId": %d,
					"dataHora": "%s",
					"observacao": %s,
					"status": "%s"
				}
				""".formatted(
				petId,
				tutorId,
				servicoId,
				dataHora,
				observacao == null ? "null" : "\"" + observacao + "\"",
				status);
	}
}
