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

import com.petservicehub.model.Pet;
import com.petservicehub.model.Tutor;
import com.petservicehub.repository.AgendamentoRepository;
import com.petservicehub.repository.PetRepository;
import com.petservicehub.repository.TutorRepository;

@SpringBootTest
@AutoConfigureMockMvc
class TutorControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private TutorRepository tutorRepository;

	@Autowired
	private PetRepository petRepository;

	@Autowired
	private AgendamentoRepository agendamentoRepository;

	@BeforeEach
	void limparBase() {
		agendamentoRepository.deleteAll();
		petRepository.deleteAll();
		tutorRepository.deleteAll();
	}

	@Test
	void deveCadastrarTutor() throws Exception {
		mockMvc.perform(post("/api/tutores")
						.contentType(MediaType.APPLICATION_JSON)
						.content(tutorJson("Ana Souza", "ana.souza@email.com", "11988887777", true)))
				.andExpect(status().isCreated())
				.andExpect(header().exists("Location"))
				.andExpect(jsonPath("$.id").exists())
				.andExpect(jsonPath("$.nome").value("Ana Souza"))
				.andExpect(jsonPath("$.email").value("ana.souza@email.com"))
				.andExpect(jsonPath("$.telefone").value("11988887777"))
				.andExpect(jsonPath("$.ativo").value(true));
	}

	@Test
	void deveRetornarBadRequestQuandoDadosInvalidos() throws Exception {
		mockMvc.perform(post("/api/tutores")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
									"nome": "",
									"email": "invalido",
									"telefone": ""
								}
								"""))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.title").value("Erro de validação"))
				.andExpect(jsonPath("$.erros.nome").exists())
				.andExpect(jsonPath("$.erros.email").exists())
				.andExpect(jsonPath("$.erros.telefone").exists());
	}

	@Test
	void deveRecusarEmailDuplicado() throws Exception {
		salvarTutor("Ana Souza", "ana.souza@email.com");

		mockMvc.perform(post("/api/tutores")
						.contentType(MediaType.APPLICATION_JSON)
						.content(tutorJson("Ana Outra", "ana.souza@email.com", "11911112222", true)))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.title").value("Conflito"));
	}

	@Test
	void deveListarTutores() throws Exception {
		salvarTutor("Ana Souza", "ana.souza@email.com");
		salvarTutor("Carlos Lima", "carlos.lima@email.com");

		mockMvc.perform(get("/api/tutores"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(2)));
	}

	@Test
	void deveFiltrarTutoresAtivos() throws Exception {
		salvarTutor("Ana Souza", "ana.souza@email.com");
		tutorRepository.save(Tutor.builder()
				.nome("Inativo")
				.email("inativo@email.com")
				.telefone("11900000000")
				.ativo(false)
				.build());

		mockMvc.perform(get("/api/tutores").param("ativo", "true"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(1)))
				.andExpect(jsonPath("$[0].nome").value("Ana Souza"));
	}

	@Test
	void deveBuscarTutorPorId() throws Exception {
		Tutor tutor = salvarTutor("Ana Souza", "ana.souza@email.com");

		mockMvc.perform(get("/api/tutores/{id}", tutor.getId()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(tutor.getId()))
				.andExpect(jsonPath("$.nome").value("Ana Souza"));
	}

	@Test
	void deveListarPetsDoTutor() throws Exception {
		Tutor tutor = salvarTutor("Ana Souza", "ana.souza@email.com");
		petRepository.save(Pet.builder()
				.nome("Thor")
				.especie("Cachorro")
				.raca("Labrador")
				.idade(4)
				.tutor(tutor)
				.ativo(true)
				.build());

		mockMvc.perform(get("/api/tutores/{id}/pets", tutor.getId()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(1)))
				.andExpect(jsonPath("$[0].nome").value("Thor"));
	}

	@Test
	void deveRetornarNotFoundQuandoTutorNaoExiste() throws Exception {
		mockMvc.perform(get("/api/tutores/{id}", 999))
				.andExpect(status().isNotFound());
	}

	@Test
	void deveAtualizarTutor() throws Exception {
		Tutor tutor = salvarTutor("Ana Souza", "ana.souza@email.com");

		mockMvc.perform(put("/api/tutores/{id}", tutor.getId())
						.contentType(MediaType.APPLICATION_JSON)
						.content(tutorJson("Ana Atualizada", "ana.atualizada@email.com", "11977776666", false)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.nome").value("Ana Atualizada"))
				.andExpect(jsonPath("$.email").value("ana.atualizada@email.com"))
				.andExpect(jsonPath("$.telefone").value("11977776666"))
				.andExpect(jsonPath("$.ativo").value(false));
	}

	@Test
	void deveExcluirTutor() throws Exception {
		Tutor tutor = salvarTutor("Ana Souza", "ana.souza@email.com");

		mockMvc.perform(delete("/api/tutores/{id}", tutor.getId()))
				.andExpect(status().isNoContent());

		mockMvc.perform(get("/api/tutores/{id}", tutor.getId()))
				.andExpect(status().isNotFound());
	}

	@Test
	void deveRecusarExclusaoQuandoTutorPossuiPets() throws Exception {
		Tutor tutor = salvarTutor("Ana Souza", "ana.souza@email.com");
		petRepository.save(Pet.builder()
				.nome("Thor")
				.especie("Cachorro")
				.idade(4)
				.tutor(tutor)
				.ativo(true)
				.build());

		mockMvc.perform(delete("/api/tutores/{id}", tutor.getId()))
				.andExpect(status().isConflict());
	}

	private Tutor salvarTutor(String nome, String email) {
		return tutorRepository.save(Tutor.builder()
				.nome(nome)
				.email(email)
				.telefone("11988887777")
				.ativo(true)
				.build());
	}

	private String tutorJson(String nome, String email, String telefone, boolean ativo) {
		return """
				{
					"nome": "%s",
					"email": "%s",
					"telefone": "%s",
					"ativo": %s
				}
				""".formatted(nome, email, telefone, ativo);
	}
}
