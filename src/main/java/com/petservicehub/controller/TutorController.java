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

import com.petservicehub.dto.PetResponse;
import com.petservicehub.dto.TutorRequest;
import com.petservicehub.dto.TutorResponse;
import com.petservicehub.service.PetService;
import com.petservicehub.service.TutorService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/tutores")
@RequiredArgsConstructor
@Tag(name = "Tutores", description = "CRUD de tutores responsáveis pelos pets")
public class TutorController {

	private final TutorService tutorService;
	private final PetService petService;

	@PostMapping
	@Operation(
			summary = "Cadastrar um novo tutor",
			description = "Cria um tutor com nome, e-mail, telefone e status ativo. O e-mail deve ser único. Se o campo ativo não for enviado, o cadastro é gravado como ativo.")
	@ApiResponses({
			@ApiResponse(responseCode = "201", description = "Tutor cadastrado com sucesso"),
			@ApiResponse(responseCode = "400", description = "Dados inválidos"),
			@ApiResponse(responseCode = "409", description = "E-mail já cadastrado")
	})
	public ResponseEntity<TutorResponse> criar(@Valid @RequestBody TutorRequest request) {
		TutorResponse criado = tutorService.criar(request);
		URI location = ServletUriComponentsBuilder.fromCurrentRequest()
				.path("/{id}")
				.buildAndExpand(criado.id())
				.toUri();
		return ResponseEntity.created(location).body(criado);
	}

	@GetMapping
	@Operation(
			summary = "Listar tutores",
			description = "Retorna todos os tutores cadastrados. O parâmetro opcional ativo permite restringir o resultado a tutores ativos ou inativos.")
	@ApiResponse(responseCode = "200", description = "Lista de tutores retornada com sucesso")
	public ResponseEntity<List<TutorResponse>> listar(
			@Parameter(description = "Quando informado, filtra apenas tutores ativos (true) ou inativos (false)")
			@RequestParam(required = false) Boolean ativo) {
		return ResponseEntity.ok(tutorService.listar(ativo));
	}

	@GetMapping("/{id}")
	@Operation(
			summary = "Buscar tutor por id",
			description = "Localiza um tutor específico pelo identificador único gerado no cadastro.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Tutor encontrado"),
			@ApiResponse(responseCode = "404", description = "Tutor não encontrado")
	})
	public ResponseEntity<TutorResponse> buscarPorId(
			@Parameter(description = "Identificador único do tutor cadastrado")
			@PathVariable Long id) {
		return ResponseEntity.ok(tutorService.buscarPorId(id));
	}

	@GetMapping("/{id}/pets")
	@Operation(
			summary = "Listar pets de um tutor",
			description = "Retorna todos os pets vinculados ao tutor informado. O tutor precisa existir no cadastro.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Lista de pets do tutor"),
			@ApiResponse(responseCode = "404", description = "Tutor não encontrado")
	})
	public ResponseEntity<List<PetResponse>> listarPets(
			@Parameter(description = "Identificador único do tutor cujos pets serão listados")
			@PathVariable Long id) {
		tutorService.buscarPorId(id);
		return ResponseEntity.ok(petService.listar(null, id));
	}

	@PutMapping("/{id}")
	@Operation(
			summary = "Atualizar um tutor",
			description = "Substitui os dados do tutor informado. O e-mail continua precisando ser único entre os tutores cadastrados.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Tutor atualizado com sucesso"),
			@ApiResponse(responseCode = "400", description = "Dados inválidos"),
			@ApiResponse(responseCode = "404", description = "Tutor não encontrado"),
			@ApiResponse(responseCode = "409", description = "E-mail já cadastrado")
	})
	public ResponseEntity<TutorResponse> atualizar(
			@Parameter(description = "Identificador único do tutor que será atualizado")
			@PathVariable Long id,
			@Valid @RequestBody TutorRequest request) {
		return ResponseEntity.ok(tutorService.atualizar(id, request));
	}

	@DeleteMapping("/{id}")
	@Operation(
			summary = "Excluir um tutor",
			description = "Remove o tutor do cadastro. A exclusão é recusada quando existem pets ou agendamentos vinculados a esse tutor.")
	@ApiResponses({
			@ApiResponse(responseCode = "204", description = "Tutor excluído com sucesso"),
			@ApiResponse(responseCode = "404", description = "Tutor não encontrado"),
			@ApiResponse(responseCode = "409", description = "Tutor possui pets ou agendamentos vinculados")
	})
	public ResponseEntity<Void> excluir(
			@Parameter(description = "Identificador único do tutor que será excluído")
			@PathVariable Long id) {
		tutorService.excluir(id);
		return ResponseEntity.noContent().build();
	}
}
