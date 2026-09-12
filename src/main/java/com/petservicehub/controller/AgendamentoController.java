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

import com.petservicehub.dto.AgendamentoRequest;
import com.petservicehub.dto.AgendamentoResponse;
import com.petservicehub.model.StatusAgendamento;
import com.petservicehub.service.AgendamentoService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/agendamentos")
@RequiredArgsConstructor
@Tag(name = "Agendamentos", description = "CRUD de agendamentos de serviços para pets")
public class AgendamentoController {

	private final AgendamentoService agendamentoService;

	@PostMapping
	@Operation(
			summary = "Cadastrar um novo agendamento",
			description = "Cria um agendamento associando um pet, o tutor responsável e um serviço. O pet precisa pertencer ao tutor informado e os três cadastros devem estar ativos. Se o status não for enviado, o agendamento é gravado como AGENDADO.")
	@ApiResponses({
			@ApiResponse(responseCode = "201", description = "Agendamento cadastrado com sucesso"),
			@ApiResponse(responseCode = "400", description = "Dados inválidos ou pet não pertence ao tutor"),
			@ApiResponse(responseCode = "404", description = "Pet, tutor ou serviço não encontrado")
	})
	public ResponseEntity<AgendamentoResponse> criar(@Valid @RequestBody AgendamentoRequest request) {
		AgendamentoResponse criado = agendamentoService.criar(request);
		URI location = ServletUriComponentsBuilder.fromCurrentRequest()
				.path("/{id}")
				.buildAndExpand(criado.id())
				.toUri();
		return ResponseEntity.created(location).body(criado);
	}

	@GetMapping
	@Operation(
			summary = "Listar agendamentos",
			description = "Retorna os agendamentos cadastrados. Os filtros opcionais permitem consultar por status, tutor, pet ou serviço.")
	@ApiResponse(responseCode = "200", description = "Lista de agendamentos retornada com sucesso")
	public ResponseEntity<List<AgendamentoResponse>> listar(
			@Parameter(description = "Status do agendamento: AGENDADO, CONCLUIDO ou CANCELADO")
			@RequestParam(required = false) StatusAgendamento status,
			@Parameter(description = "Identificador único do tutor. Quando informado, retorna somente os agendamentos desse tutor")
			@RequestParam(required = false) Long tutorId,
			@Parameter(description = "Identificador único do pet. Quando informado, retorna somente os agendamentos desse pet")
			@RequestParam(required = false) Long petId,
			@Parameter(description = "Identificador único do serviço. Quando informado, retorna somente os agendamentos desse serviço")
			@RequestParam(required = false) Long servicoId) {
		return ResponseEntity.ok(agendamentoService.listar(status, tutorId, petId, servicoId));
	}

	@GetMapping("/{id}")
	@Operation(
			summary = "Buscar agendamento por id",
			description = "Localiza um agendamento específico pelo identificador único e devolve pet, tutor, serviço, data e hora, observação e status.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Agendamento encontrado"),
			@ApiResponse(responseCode = "404", description = "Agendamento não encontrado")
	})
	public ResponseEntity<AgendamentoResponse> buscarPorId(
			@Parameter(description = "Identificador único do agendamento cadastrado")
			@PathVariable Long id) {
		return ResponseEntity.ok(agendamentoService.buscarPorId(id));
	}

	@PutMapping("/{id}")
	@Operation(
			summary = "Atualizar um agendamento",
			description = "Atualiza pet, tutor, serviço, data e hora, observação e status do agendamento informado. O pet continua precisando pertencer ao tutor enviado na requisição.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Agendamento atualizado com sucesso"),
			@ApiResponse(responseCode = "400", description = "Dados inválidos ou pet não pertence ao tutor"),
			@ApiResponse(responseCode = "404", description = "Agendamento, pet, tutor ou serviço não encontrado")
	})
	public ResponseEntity<AgendamentoResponse> atualizar(
			@Parameter(description = "Identificador único do agendamento que será atualizado")
			@PathVariable Long id,
			@Valid @RequestBody AgendamentoRequest request) {
		return ResponseEntity.ok(agendamentoService.atualizar(id, request));
	}

	@DeleteMapping("/{id}")
	@Operation(
			summary = "Excluir um agendamento",
			description = "Remove o agendamento do cadastro a partir do identificador informado.")
	@ApiResponses({
			@ApiResponse(responseCode = "204", description = "Agendamento excluído com sucesso"),
			@ApiResponse(responseCode = "404", description = "Agendamento não encontrado")
	})
	public ResponseEntity<Void> excluir(
			@Parameter(description = "Identificador único do agendamento que será excluído")
			@PathVariable Long id) {
		agendamentoService.excluir(id);
		return ResponseEntity.noContent().build();
	}
}
