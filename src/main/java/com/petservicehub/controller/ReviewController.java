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

import com.petservicehub.dto.ReviewRequest;
import com.petservicehub.dto.ReviewResponse;
import com.petservicehub.service.ReviewService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
@Tag(name = "Avaliações", description = "CRUD de avaliações de solicitações concluídas")
public class ReviewController {

	private final ReviewService reviewService;

	@PostMapping
	@Operation(
			summary = "Cadastrar uma nova avaliação",
			description = "Registra a avaliação de uma solicitação COMPLETED. O avaliador precisa ser o cliente da solicitação e cada solicitação aceita somente uma avaliação. A nota deve estar entre 1 e 5.")
	@ApiResponses({
			@ApiResponse(responseCode = "201", description = "Avaliação cadastrada com sucesso"),
			@ApiResponse(responseCode = "400", description = "Dados inválidos, solicitação não concluída ou avaliador incompatível"),
			@ApiResponse(responseCode = "404", description = "Solicitação ou avaliador não encontrado"),
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
	@Operation(
			summary = "Listar avaliações",
			description = "Retorna as avaliações cadastradas. Os filtros opcionais permitem consultar por avaliador e por solicitação.")
	@ApiResponse(responseCode = "200", description = "Lista de avaliações retornada com sucesso")
	public ResponseEntity<List<ReviewResponse>> listar(
			@Parameter(description = "Identificador único do cliente avaliador. Quando informado, retorna somente as avaliações desse usuário")
			@RequestParam(required = false) Long reviewerId,
			@Parameter(description = "Identificador único da solicitação. Quando informado, retorna somente a avaliação dessa solicitação")
			@RequestParam(required = false) Long requestId) {
		return ResponseEntity.ok(reviewService.listar(reviewerId, requestId));
	}

	@GetMapping("/{id}")
	@Operation(
			summary = "Buscar avaliação por id",
			description = "Localiza uma avaliação específica pelo identificador único, incluindo nota, comentário, avaliador e a solicitação correspondente.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Avaliação encontrada"),
			@ApiResponse(responseCode = "404", description = "Avaliação não encontrada")
	})
	public ResponseEntity<ReviewResponse> buscarPorId(
			@Parameter(description = "Identificador único da avaliação cadastrada")
			@PathVariable Long id) {
		return ResponseEntity.ok(reviewService.buscarPorId(id));
	}

	@PutMapping("/{id}")
	@Operation(
			summary = "Atualizar uma avaliação",
			description = "Atualiza a solicitação, o avaliador, a nota e o comentário. As regras de solicitação COMPLETED, avaliador igual ao cliente e avaliação única por solicitação continuam valendo.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Avaliação atualizada com sucesso"),
			@ApiResponse(responseCode = "400", description = "Dados inválidos, solicitação não concluída ou avaliador incompatível"),
			@ApiResponse(responseCode = "404", description = "Avaliação, solicitação ou avaliador não encontrado"),
			@ApiResponse(responseCode = "409", description = "A solicitação já possui uma avaliação")
	})
	public ResponseEntity<ReviewResponse> atualizar(
			@Parameter(description = "Identificador único da avaliação que será atualizada")
			@PathVariable Long id,
			@Valid @RequestBody ReviewRequest request) {
		return ResponseEntity.ok(reviewService.atualizar(id, request));
	}

	@DeleteMapping("/{id}")
	@Operation(
			summary = "Excluir uma avaliação",
			description = "Remove a avaliação do cadastro a partir do identificador informado.")
	@ApiResponses({
			@ApiResponse(responseCode = "204", description = "Avaliação excluída com sucesso"),
			@ApiResponse(responseCode = "404", description = "Avaliação não encontrada")
	})
	public ResponseEntity<Void> excluir(
			@Parameter(description = "Identificador único da avaliação que será excluída")
			@PathVariable Long id) {
		reviewService.excluir(id);
		return ResponseEntity.noContent().build();
	}
}
