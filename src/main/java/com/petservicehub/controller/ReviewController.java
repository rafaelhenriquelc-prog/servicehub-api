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

import com.petservicehub.dto.ReviewRequest;
import com.petservicehub.dto.ReviewResponse;
import com.petservicehub.service.ReviewService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearer-jwt")
@Tag(name = "Avaliações", description = "Avaliações autenticadas. O avaliador vem do JWT.")
public class ReviewController {

	private final ReviewService reviewService;

	@PostMapping
	@PreAuthorize("hasRole('CLIENT')")
	@Operation(
			summary = "Cadastrar uma nova avaliação",
			description = "O CLIENT autenticado avalia somente a própria solicitação COMPLETED. O reviewerId não é aceito no corpo.")
	@ApiResponses({
			@ApiResponse(responseCode = "201", description = "Avaliação cadastrada com sucesso"),
			@ApiResponse(responseCode = "400", description = "Solicitação não concluída"),
			@ApiResponse(responseCode = "403", description = "Avaliação de solicitação de outro cliente"),
			@ApiResponse(responseCode = "409", description = "A solicitação já possui uma avaliação")
	})
	public ResponseEntity<ReviewResponse> criar(@Valid @RequestBody ReviewRequest request) {
		ReviewResponse criado = reviewService.criar(request);
		URI location = ServletUriComponentsBuilder.fromCurrentRequest()
				.path("/{id}")
				.buildAndExpand(criado.id())
				.toUri();
		return ResponseEntity.created(location).body(criado);
	}

	@GetMapping
	@PreAuthorize("isAuthenticated()")
	@Operation(summary = "Listar avaliações", description = "CLIENT vê as próprias. PROVIDER vê as dos seus serviços. ADMIN vê todas.")
	@ApiResponse(responseCode = "200", description = "Lista de avaliações retornada com sucesso")
	public ResponseEntity<List<ReviewResponse>> listar(
			@Parameter(description = "Identificador do avaliador (respeitado somente para ADMIN)")
			@RequestParam(required = false) Long reviewerId,
			@Parameter(description = "Identificador da solicitação")
			@RequestParam(required = false) Long requestId) {
		return ResponseEntity.ok(reviewService.listar(reviewerId, requestId));
	}

	@GetMapping("/{id}")
	@PreAuthorize("isAuthenticated()")
	@Operation(summary = "Buscar avaliação por id", description = "Acesso restrito ao avaliador, ao prestador do serviço ou ao ADMIN.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Avaliação encontrada"),
			@ApiResponse(responseCode = "403", description = "Avaliação de outro usuário"),
			@ApiResponse(responseCode = "404", description = "Avaliação não encontrada")
	})
	public ResponseEntity<ReviewResponse> buscarPorId(
			@Parameter(description = "Identificador único da avaliação cadastrada")
			@PathVariable Long id) {
		return ResponseEntity.ok(reviewService.buscarPorId(id));
	}

	@PutMapping("/{id}")
	@PreAuthorize("hasAnyRole('CLIENT', 'ADMIN')")
	@Operation(summary = "Atualizar uma avaliação", description = "CLIENT atualiza somente a própria avaliação.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Avaliação atualizada com sucesso"),
			@ApiResponse(responseCode = "403", description = "Avaliação de outro usuário"),
			@ApiResponse(responseCode = "404", description = "Avaliação não encontrada")
	})
	public ResponseEntity<ReviewResponse> atualizar(
			@Parameter(description = "Identificador único da avaliação que será atualizada")
			@PathVariable Long id,
			@Valid @RequestBody ReviewRequest request) {
		return ResponseEntity.ok(reviewService.atualizar(id, request));
	}

	@DeleteMapping("/{id}")
	@PreAuthorize("hasAnyRole('CLIENT', 'ADMIN')")
	@Operation(summary = "Excluir uma avaliação", description = "CLIENT exclui somente a própria avaliação.")
	@ApiResponses({
			@ApiResponse(responseCode = "204", description = "Avaliação excluída com sucesso"),
			@ApiResponse(responseCode = "403", description = "Avaliação de outro usuário"),
			@ApiResponse(responseCode = "404", description = "Avaliação não encontrada")
	})
	public ResponseEntity<Void> excluir(
			@Parameter(description = "Identificador único da avaliação que será excluída")
			@PathVariable Long id) {
		reviewService.excluir(id);
		return ResponseEntity.noContent().build();
	}
}
