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
class PetControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private PetRepository petRepository;

	@Autowired
	private TutorRepository tutorRepository;

	@Autowired
	private AgendamentoRepository agendamentoRepository;

	private Tutor tutor;

	@BeforeEach
	void limparBase() {
		agendamentoRepository.deleteAll();
		petRepository.deleteAll();
		tutorRepository.deleteAll();
		tutor = salvarTutor("Ana Souza", "ana.souza@email.com");
	}

	@Test
	void deveCadastrarPet() throws Exception {
		mockMvc.perform(post("/api/pets")
						.contentType(MediaType.APPLICATION_JSON)
						.content(petJson("Thor", "Cachorro", "Labrador", 4, tutor.getId(), true)))
				.andExpect(status().isCreated())
				.andExpect(header().exists("Location"))
				.andExpect(jsonPath("$.id").exists())
				.andExpect(jsonPath("$.nome").value("Thor"))
				.andExpect(jsonPath("$.especie").value("Cachorro"))
				.andExpect(jsonPath("$.raca").value("Labrador"))
				.andExpect(jsonPath("$.idade").value(4))
				.andExpect(jsonPath("$.tutor.id").value(tutor.getId()))
				.andExpect(jsonPath("$.tutor.nome").value("Ana Souza"))
				.andExpect(jsonPath("$.ativo").value(true));
	}

	@Test
	void deveRetornarBadRequestQuandoDadosInvalidos() throws Exception {
		mockMvc.perform(post("/api/pets")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
									"nome": "",
									"especie": "",
									"idade": -1
								}
								"""))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.title").value("Erro de validação"))
				.andExpect(jsonPath("$.erros.nome").exists())
				.andExpect(jsonPath("$.erros.especie").exists())
				.andExpect(jsonPath("$.erros.idade").exists())
				.andExpect(jsonPath("$.erros.tutorId").exists());
	}

	@Test
	void deveRetornarNotFoundQuandoTutorNaoExiste() throws Exception {
		mockMvc.perform(post("/api/pets")
						.contentType(MediaType.APPLICATION_JSON)
						.content(petJson("Thor", "Cachorro", "Labrador", 4, 999L, true)))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.title").value("Recurso não encontrado"));
	}

	@Test
	void deveListarPets() throws Exception {
		salvarPet("Thor", true);
		salvarPet("Mimi", false);

		mockMvc.perform(get("/api/pets"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(2)));
	}

	@Test
	void deveFiltrarPetsAtivos() throws Exception {
		salvarPet("Thor", true);
		salvarPet("Mimi", false);

		mockMvc.perform(get("/api/pets").param("ativo", "true"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(1)))
				.andExpect(jsonPath("$[0].nome").value("Thor"));
	}

	@Test
	void deveFiltrarPetsPorTutor() throws Exception {
		Tutor outroTutor = salvarTutor("Carlos Lima", "carlos.lima@email.com");
		salvarPet("Thor", true);
		petRepository.save(Pet.builder()
				.nome("Luna")
				.especie("Gato")
				.raca("Siamês")
				.idade(2)
				.tutor(outroTutor)
				.ativo(true)
				.build());

		mockMvc.perform(get("/api/pets").param("tutorId", tutor.getId().toString()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(1)))
				.andExpect(jsonPath("$[0].nome").value("Thor"));
	}

	@Test
	void deveBuscarPetPorId() throws Exception {
		Pet pet = salvarPet("Thor", true);

		mockMvc.perform(get("/api/pets/{id}", pet.getId()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(pet.getId()))
				.andExpect(jsonPath("$.nome").value("Thor"))
				.andExpect(jsonPath("$.tutor.id").value(tutor.getId()));
	}

	@Test
	void deveRetornarNotFoundQuandoPetNaoExiste() throws Exception {
		mockMvc.perform(get("/api/pets/{id}", 999))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.title").value("Recurso não encontrado"));
	}

	@Test
	void deveAtualizarPet() throws Exception {
		Pet pet = salvarPet("Thor", true);
		Tutor novoTutor = salvarTutor("Carlos Lima", "carlos.lima@email.com");

		mockMvc.perform(put("/api/pets/{id}", pet.getId())
						.contentType(MediaType.APPLICATION_JSON)
						.content(petJson("Thor Atualizado", "Cachorro", "Golden Retriever", 5, novoTutor.getId(), false)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.nome").value("Thor Atualizado"))
				.andExpect(jsonPath("$.raca").value("Golden Retriever"))
				.andExpect(jsonPath("$.idade").value(5))
				.andExpect(jsonPath("$.tutor.id").value(novoTutor.getId()))
				.andExpect(jsonPath("$.ativo").value(false));
	}

	@Test
	void deveExcluirPet() throws Exception {
		Pet pet = salvarPet("Thor", true);

		mockMvc.perform(delete("/api/pets/{id}", pet.getId()))
				.andExpect(status().isNoContent());

		mockMvc.perform(get("/api/pets/{id}", pet.getId()))
				.andExpect(status().isNotFound());
	}

	@Test
	void deveRetornarNotFoundAoExcluirPetInexistente() throws Exception {
		mockMvc.perform(delete("/api/pets/{id}", 999))
				.andExpect(status().isNotFound());
	}

	@Test
	void deveExporDocumentacaoOpenApi() throws Exception {
		mockMvc.perform(get("/v3/api-docs"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.info.title").value("PetServiceHub API"))
				.andExpect(jsonPath("$.paths['/api/tutores']").exists())
				.andExpect(jsonPath("$.paths['/api/servicos']").exists())
				.andExpect(jsonPath("$.paths['/api/agendamentos']").exists());
	}

	private Tutor salvarTutor(String nome, String email) {
		return tutorRepository.save(Tutor.builder()
				.nome(nome)
				.email(email)
				.telefone("11988887777")
				.ativo(true)
				.build());
	}

	private Pet salvarPet(String nome, boolean ativo) {
		return petRepository.save(Pet.builder()
				.nome(nome)
				.especie("Cachorro")
				.raca("Labrador")
				.idade(3)
				.tutor(tutor)
				.ativo(ativo)
				.build());
	}

	private String petJson(String nome, String especie, String raca, int idade, Long tutorId, boolean ativo) {
		return """
				{
					"nome": "%s",
					"especie": "%s",
					"raca": "%s",
					"idade": %d,
					"tutorId": %d,
					"ativo": %s
				}
				""".formatted(nome, especie, raca, idade, tutorId, ativo);
	}
}
