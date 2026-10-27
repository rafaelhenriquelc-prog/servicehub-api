package com.petservicehub.controller;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/service-requests")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearer-jwt")
@Tag(name = "Solicitações", description = "Contratações autenticadas. O cliente vem do JWT, não do corpo.")
public class ServiceRequestController {

	private final ServiceRequestService serviceRequestService;

	@PostMapping
	@PreAuthorize("hasRole('CLIENT')")
	@Operation(
			summary = "Cadastrar uma nova solicitação",
			description = "O CLIENT autenticado contrata um serviço ativo. O clientId não é aceito no corpo. O valor total copia o preço do serviço e o status inicial é PENDING.")
	@ApiResponses({
			@ApiResponse(responseCode = "201", description = "Solicitação cadastrada com sucesso"),
			@ApiResponse(responseCode = "400", description = "Dados inválidos ou serviço inativo"),
			@ApiResponse(responseCode = "403", description = "Somente CLIENT pode contratar")
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
	@PreAuthorize("isAuthenticated()")
	@Operation(
			summary = "Listar solicitações",
			description = "CLIENT vê apenas as próprias. PROVIDER vê as dos seus serviços. ADMIN vê todas.")
	@ApiResponse(responseCode = "200", description = "Lista de solicitações retornada com sucesso")
	public ResponseEntity<List<ServiceRequestResponse>> listar(
			@Parameter(description = "Status da solicitação")
			@RequestParam(required = false) RequestStatus status,
			@Parameter(description = "Identificador do cliente (respeitado somente para ADMIN)")
			@RequestParam(required = false) Long clientId,
			@Parameter(description = "Identificador do serviço")
			@RequestParam(required = false) Long serviceId) {
		return ResponseEntity.ok(serviceRequestService.listar(status, clientId, serviceId));
	}

	@GetMapping("/{id}")
	@PreAuthorize("isAuthenticated()")
	@Operation(summary = "Buscar solicitação por id", description = "CLIENT acessa a própria. PROVIDER acessa se o serviço for dele. ADMIN acessa qualquer uma.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Solicitação encontrada"),
			@ApiResponse(responseCode = "403", description = "Solicitação de outro usuário"),
			@ApiResponse(responseCode = "404", description = "Solicitação não encontrada")
	})
	public ResponseEntity<ServiceRequestResponse> buscarPorId(
			@Parameter(description = "Identificador único da solicitação cadastrada")
			@PathVariable Long id) {
		return ResponseEntity.ok(serviceRequestService.buscarPorId(id));
	}

	@PutMapping("/{id}")
	@PreAuthorize("isAuthenticated()")
	@Operation(summary = "Atualizar uma solicitação", description = "CLIENT altera a própria (inclusive cancelar). PROVIDER atualiza o status das solicitações dos seus serviços.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Solicitação atualizada com sucesso"),
			@ApiResponse(responseCode = "403", description = "Solicitação de outro usuário"),
			@ApiResponse(responseCode = "404", description = "Solicitação não encontrada")
	})
	public ResponseEntity<ServiceRequestResponse> atualizar(
			@Parameter(description = "Identificador único da solicitação que será atualizada")
			@PathVariable Long id,
			@Valid @RequestBody ServiceRequestInput request) {
		return ResponseEntity.ok(serviceRequestService.atualizar(id, request));
	}

	@DeleteMapping("/{id}")
	@PreAuthorize("isAuthenticated()")
	@Operation(summary = "Excluir uma solicitação", description = "Somente quem tem acesso à solicitação pode excluí-la, se não houver avaliação.")
	@ApiResponses({
			@ApiResponse(responseCode = "204", description = "Solicitação excluída com sucesso"),
			@ApiResponse(responseCode = "403", description = "Solicitação de outro usuário"),
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
