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

import com.petservicehub.dto.OfferedServiceRequest;
import com.petservicehub.dto.OfferedServiceResponse;
import com.petservicehub.service.OfferedServiceService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/services")
@RequiredArgsConstructor
@Tag(name = "Serviços", description = "CRUD de serviços oferecidos pelos prestadores")
public class OfferedServiceController {

	private final OfferedServiceService offeredServiceService;

	@PostMapping
	@Operation(
			summary = "Cadastrar um novo serviço",
			description = "Publica um serviço vinculado a um usuário PROVIDER ativo. Clientes não podem ser associados como prestadores. Se active não for enviado, o serviço fica ativo.")
	@ApiResponses({
			@ApiResponse(responseCode = "201", description = "Serviço cadastrado com sucesso"),
			@ApiResponse(responseCode = "400", description = "Dados inválidos ou prestador incompatível"),
			@ApiResponse(responseCode = "404", description = "Prestador não encontrado")
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
			description = "Retorna os serviços cadastrados. Os filtros opcionais permitem consultar por status ativo, prestador e categoria.")
	@ApiResponse(responseCode = "200", description = "Lista de serviços retornada com sucesso")
	public ResponseEntity<List<OfferedServiceResponse>> listar(
			@Parameter(description = "Quando informado, filtra serviços ativos (true) ou inativos (false)")
			@RequestParam(required = false) Boolean active,
			@Parameter(description = "Identificador único do prestador. Quando informado, retorna somente os serviços desse usuário")
			@RequestParam(required = false) Long providerId,
			@Parameter(description = "Categoria do serviço, por exemplo Marcenaria")
			@RequestParam(required = false) String category) {
		return ResponseEntity.ok(offeredServiceService.listar(active, providerId, category));
	}

	@GetMapping("/{id}")
	@Operation(
			summary = "Buscar serviço por id",
			description = "Localiza um serviço específico pelo identificador único e devolve também os dados resumidos do prestador.")
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
	@Operation(
			summary = "Atualizar um serviço",
			description = "Atualiza título, descrição, preço, categoria, prestador e status do serviço informado. O prestador precisa continuar sendo um usuário PROVIDER ativo.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Serviço atualizado com sucesso"),
			@ApiResponse(responseCode = "400", description = "Dados inválidos ou prestador incompatível"),
			@ApiResponse(responseCode = "404", description = "Serviço ou prestador não encontrado")
	})
	public ResponseEntity<OfferedServiceResponse> atualizar(
			@Parameter(description = "Identificador único do serviço que será atualizado")
			@PathVariable Long id,
			@Valid @RequestBody OfferedServiceRequest request) {
		return ResponseEntity.ok(offeredServiceService.atualizar(id, request));
	}

	@DeleteMapping("/{id}")
	@Operation(
			summary = "Excluir um serviço",
			description = "Remove o serviço do catálogo. A exclusão é recusada quando existem solicitações vinculadas a esse serviço.")
	@ApiResponses({
			@ApiResponse(responseCode = "204", description = "Serviço excluído com sucesso"),
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
