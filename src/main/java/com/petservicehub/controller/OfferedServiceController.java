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

import com.petservicehub.dto.OfferedServiceRequest;
import com.petservicehub.dto.OfferedServiceResponse;
import com.petservicehub.service.OfferedServiceService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/services")
@RequiredArgsConstructor
@Tag(name = "Serviços", description = "Catálogo público e gestão de serviços pelo prestador autenticado")
public class OfferedServiceController {

	private final OfferedServiceService offeredServiceService;

	@PostMapping
	@PreAuthorize("hasRole('PROVIDER')")
	@SecurityRequirement(name = "bearer-jwt")
	@Operation(
			summary = "Cadastrar um novo serviço",
			description = "O prestador autenticado publica o serviço. O providerId não é aceito no corpo: o dono é sempre o usuário do token.")
	@ApiResponses({
			@ApiResponse(responseCode = "201", description = "Serviço cadastrado com sucesso"),
			@ApiResponse(responseCode = "400", description = "Dados inválidos"),
			@ApiResponse(responseCode = "401", description = "Não autenticado"),
			@ApiResponse(responseCode = "403", description = "Somente PROVIDER pode criar serviços")
	})
	public ResponseEntity<OfferedServiceResponse> criar(@Valid @RequestBody OfferedServiceRequest request) {
		OfferedServiceResponse criado = offeredServiceService.criar(request);
		URI location = ServletUriComponentsBuilder.fromCurrentRequest()
				.path("/{id}")
				.buildAndExpand(criado.id())
				.toUri();
		return ResponseEntity.created(location).body(criado);
	}

	@GetMapping
	@Operation(
			summary = "Listar serviços",
			description = "Consulta pública do catálogo. Filtros opcionais por status ativo, prestador e categoria.")
	@ApiResponse(responseCode = "200", description = "Lista de serviços retornada com sucesso")
	public ResponseEntity<List<OfferedServiceResponse>> listar(
			@Parameter(description = "Quando informado, filtra serviços ativos (true) ou inativos (false)")
			@RequestParam(required = false) Boolean active,
			@Parameter(description = "Identificador único do prestador")
			@RequestParam(required = false) Long providerId,
			@Parameter(description = "Categoria do serviço, por exemplo Marcenaria")
			@RequestParam(required = false) String category) {
		return ResponseEntity.ok(offeredServiceService.listar(active, providerId, category));
	}

	@GetMapping("/{id}")
	@Operation(summary = "Buscar serviço por id", description = "Consulta pública de um serviço pelo identificador.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Serviço encontrado"),
			@ApiResponse(responseCode = "404", description = "Serviço não encontrado")
	})
	public ResponseEntity<OfferedServiceResponse> buscarPorId(
			@Parameter(description = "Identificador único do serviço cadastrado")
			@PathVariable Long id) {
		return ResponseEntity.ok(offeredServiceService.buscarPorId(id));
	}

	@PutMapping("/{id}")
	@PreAuthorize("hasAnyRole('PROVIDER', 'ADMIN')")
	@SecurityRequirement(name = "bearer-jwt")
	@Operation(
			summary = "Atualizar um serviço",
			description = "PROVIDER altera somente o próprio serviço. ADMIN pode alterar qualquer serviço.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Serviço atualizado com sucesso"),
			@ApiResponse(responseCode = "403", description = "Serviço de outro prestador"),
			@ApiResponse(responseCode = "404", description = "Serviço não encontrado")
	})
	public ResponseEntity<OfferedServiceResponse> atualizar(
			@Parameter(description = "Identificador único do serviço que será atualizado")
			@PathVariable Long id,
			@Valid @RequestBody OfferedServiceRequest request) {
		return ResponseEntity.ok(offeredServiceService.atualizar(id, request));
	}

	@DeleteMapping("/{id}")
	@PreAuthorize("hasAnyRole('PROVIDER', 'ADMIN')")
	@SecurityRequirement(name = "bearer-jwt")
	@Operation(
			summary = "Excluir um serviço",
			description = "PROVIDER exclui somente o próprio serviço. A exclusão é recusada quando há solicitações vinculadas.")
	@ApiResponses({
			@ApiResponse(responseCode = "204", description = "Serviço excluído com sucesso"),
			@ApiResponse(responseCode = "403", description = "Serviço de outro prestador"),
			@ApiResponse(responseCode = "404", description = "Serviço não encontrado"),
			@ApiResponse(responseCode = "409", description = "Serviço possui solicitações vinculadas")
	})
	public ResponseEntity<Void> excluir(
			@Parameter(description = "Identificador único do serviço que será excluído")
			@PathVariable Long id) {
		offeredServiceService.excluir(id);
		return ResponseEntity.noContent().build();
	}
}
