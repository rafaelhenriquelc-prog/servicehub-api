package com.petservicehub.controller;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.petservicehub.dto.PetRequest;
import com.petservicehub.dto.PetResponse;
import com.petservicehub.service.PetService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/pets")
@RequiredArgsConstructor
@Tag(name = "Pets", description = "CRUD de animais de estimação")
public class PetController {

	private final PetService petService;

	@PostMapping
	@Operation(
			summary = "Cadastrar um novo pet",
			description = "Cria um pet vinculado a um tutor existente e ativo. É necessário informar nome, espécie, idade e o identificador do tutor. Se o campo ativo não for enviado, o cadastro é gravado como ativo.")
	@ApiResponses({
			@ApiResponse(responseCode = "201", description = "Pet cadastrado com sucesso"),
			@ApiResponse(responseCode = "400", description = "Dados inválidos ou tutor inativo"),
			@ApiResponse(responseCode = "404", description = "Tutor não encontrado")
	})
	public ResponseEntity<PetResponse> criar(@Valid @RequestBody PetRequest request) {
		PetResponse criado = petService.criar(request);
		URI location = ServletUriComponentsBuilder.fromCurrentRequest()
				.path("/{id}")
				.buildAndExpand(criado.id())
				.toUri();
		return ResponseEntity.created(location).body(criado);
	}

	@GetMapping
	@Operation(
			summary = "Listar pets",
			description = "Retorna os pets cadastrados. Os filtros opcionais permitem restringir o resultado por status ativo e pelo identificador do tutor responsável.")
	@ApiResponse(responseCode = "200", description = "Lista de pets retornada com sucesso")
	public ResponseEntity<List<PetResponse>> listar(
			@Parameter(description = "Quando informado, filtra apenas pets ativos (true) ou inativos (false)")
			@RequestParam(required = false) Boolean ativo,
			@Parameter(description = "Identificador único do tutor. Quando informado, retorna somente os pets vinculados a esse tutor")
			@RequestParam(required = false) Long tutorId) {
		return ResponseEntity.ok(petService.listar(ativo, tutorId));
	}

	@GetMapping("/{id}")
	@Operation(
			summary = "Buscar pet por id",
			description = "Localiza um pet específico pelo identificador único e devolve também os dados resumidos do tutor responsável.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Pet encontrado"),
			@ApiResponse(responseCode = "404", description = "Pet não encontrado")
	})
	public ResponseEntity<PetResponse> buscarPorId(
			@Parameter(description = "Identificador único do pet cadastrado")
			@PathVariable Long id) {
		return ResponseEntity.ok(petService.buscarPorId(id));
	}

	@PutMapping("/{id}")
	@Operation(
			summary = "Atualizar um pet",
			description = "Atualiza os dados do pet informado, inclusive o tutor responsável. O novo tutor precisa existir e estar ativo.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Pet atualizado com sucesso"),
			@ApiResponse(responseCode = "400", description = "Dados inválidos ou tutor inativo"),
			@ApiResponse(responseCode = "404", description = "Pet ou tutor não encontrado")
	})
	public ResponseEntity<PetResponse> atualizar(
			@Parameter(description = "Identificador único do pet que será atualizado")
			@PathVariable Long id,
			@Valid @RequestBody PetRequest request) {
		return ResponseEntity.ok(petService.atualizar(id, request));
	}

	@DeleteMapping("/{id}")
	@Operation(
			summary = "Excluir um pet",
			description = "Remove o pet do cadastro. A exclusão é recusada quando existem agendamentos vinculados a esse pet.")
	@ApiResponses({
			@ApiResponse(responseCode = "204", description = "Pet excluído com sucesso"),
			@ApiResponse(responseCode = "404", description = "Pet não encontrado"),
			@ApiResponse(responseCode = "409", description = "Pet possui agendamentos vinculados")
	})
	public ResponseEntity<Void> excluir(
			@Parameter(description = "Identificador único do pet que será excluído")
			@PathVariable Long id) {
		petService.excluir(id);
		return ResponseEntity.noContent().build();
	}
}
