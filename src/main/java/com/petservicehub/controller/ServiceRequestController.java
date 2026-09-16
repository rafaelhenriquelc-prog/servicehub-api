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

import com.petservicehub.dto.ServiceRequestInput;
import com.petservicehub.dto.ServiceRequestResponse;
import com.petservicehub.model.RequestStatus;
import com.petservicehub.service.ServiceRequestService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/service-requests")
@RequiredArgsConstructor
@Tag(name = "Solicitações", description = "CRUD de contratações de serviços feitas por clientes")
public class ServiceRequestController {

	private final ServiceRequestService serviceRequestService;

	@PostMapping
	@Operation(
			summary = "Cadastrar uma nova solicitação",
			description = "Contrata um serviço ativo em nome de um usuário CLIENT. O valor total é preenchido automaticamente com o preço do serviço. Se o status não for enviado, a solicitação nasce como PENDING.")
	@ApiResponses({
			@ApiResponse(responseCode = "201", description = "Solicitação cadastrada com sucesso"),
			@ApiResponse(responseCode = "400", description = "Dados inválidos, cliente incompatível ou serviço inativo"),
			@ApiResponse(responseCode = "404", description = "Serviço ou cliente não encontrado")
	})
	public ResponseEntity<ServiceRequestResponse> criar(@Valid @RequestBody ServiceRequestInput request) {
		ServiceRequestResponse criado = serviceRequestService.criar(request);
		URI location = ServletUriComponentsBuilder.fromCurrentRequest()
				.path("/{id}")
				.buildAndExpand(criado.id())
				.toUri();
		return ResponseEntity.created(location).body(criado);
	}

	@GetMapping
	@Operation(
			summary = "Listar solicitações",
			description = "Retorna as solicitações cadastradas. Os filtros opcionais permitem consultar por status, cliente e serviço.")
	@ApiResponse(responseCode = "200", description = "Lista de solicitações retornada com sucesso")
	public ResponseEntity<List<ServiceRequestResponse>> listar(
			@Parameter(description = "Status da solicitação: PENDING, ACCEPTED, IN_PROGRESS, COMPLETED ou CANCELLED")
			@RequestParam(required = false) RequestStatus status,
			@Parameter(description = "Identificador único do cliente. Quando informado, retorna somente as solicitações desse usuário")
			@RequestParam(required = false) Long clientId,
			@Parameter(description = "Identificador único do serviço. Quando informado, retorna somente as solicitações desse serviço")
			@RequestParam(required = false) Long serviceId) {
		return ResponseEntity.ok(serviceRequestService.listar(status, clientId, serviceId));
	}

	@GetMapping("/{id}")
	@Operation(
			summary = "Buscar solicitação por id",
			description = "Localiza uma solicitação específica pelo identificador único, incluindo serviço, cliente, status, valor total e datas.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Solicitação encontrada"),
			@ApiResponse(responseCode = "404", description = "Solicitação não encontrada")
	})
	public ResponseEntity<ServiceRequestResponse> buscarPorId(
			@Parameter(description = "Identificador único da solicitação cadastrada")
			@PathVariable Long id) {
		return ResponseEntity.ok(serviceRequestService.buscarPorId(id));
	}

	@PutMapping("/{id}")
	@Operation(
			summary = "Atualizar uma solicitação",
			description = "Atualiza serviço, cliente, data agendada, observações e status. O valor total é recalculado com o preço atual do serviço informado.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Solicitação atualizada com sucesso"),
			@ApiResponse(responseCode = "400", description = "Dados inválidos, cliente incompatível ou serviço inativo"),
			@ApiResponse(responseCode = "404", description = "Solicitação, serviço ou cliente não encontrado")
	})
	public ResponseEntity<ServiceRequestResponse> atualizar(
			@Parameter(description = "Identificador único da solicitação que será atualizada")
			@PathVariable Long id,
			@Valid @RequestBody ServiceRequestInput request) {
		return ResponseEntity.ok(serviceRequestService.atualizar(id, request));
	}

	@DeleteMapping("/{id}")
	@Operation(
			summary = "Excluir uma solicitação",
			description = "Remove a solicitação do cadastro. A exclusão é recusada quando já existe uma avaliação vinculada.")
	@ApiResponses({
			@ApiResponse(responseCode = "204", description = "Solicitação excluída com sucesso"),
			@ApiResponse(responseCode = "404", description = "Solicitação não encontrada"),
			@ApiResponse(responseCode = "409", description = "Solicitação possui avaliação vinculada")
	})
	public ResponseEntity<Void> excluir(
			@Parameter(description = "Identificador único da solicitação que será excluída")
			@PathVariable Long id) {
		serviceRequestService.excluir(id);
		return ResponseEntity.noContent().build();
	}
}
