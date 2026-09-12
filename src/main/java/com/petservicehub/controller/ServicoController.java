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

import com.petservicehub.dto.ServicoRequest;
import com.petservicehub.dto.ServicoResponse;
import com.petservicehub.service.ServicoService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/servicos")
@RequiredArgsConstructor
@Tag(name = "Serviços", description = "CRUD de serviços oferecidos pelo PetServiceHub")
public class ServicoController {

	private final ServicoService servicoService;

	@PostMapping
	@Operation(
			summary = "Cadastrar um novo serviço",
			description = "Cria um serviço com nome, descrição, preço e duração em minutos. Se o campo ativo não for enviado, o cadastro é gravado como ativo.")
	@ApiResponses({
			@ApiResponse(responseCode = "201", description = "Serviço cadastrado com sucesso"),
			@ApiResponse(responseCode = "400", description = "Dados inválidos")
	})
	public ResponseEntity<ServicoResponse> criar(@Valid @RequestBody ServicoRequest request) {
		ServicoResponse criado = servicoService.criar(request);
		URI location = ServletUriComponentsBuilder.fromCurrentRequest()
				.path("/{id}")
				.buildAndExpand(criado.id())
				.toUri();
		return ResponseEntity.created(location).body(criado);
	}

	@GetMapping
	@Operation(
			summary = "Listar serviços",
			description = "Retorna todos os serviços cadastrados. O parâmetro opcional ativo permite restringir o resultado a serviços ativos ou inativos.")
	@ApiResponse(responseCode = "200", description = "Lista de serviços retornada com sucesso")
	public ResponseEntity<List<ServicoResponse>> listar(
			@Parameter(description = "Quando informado, filtra apenas serviços ativos (true) ou inativos (false)")
			@RequestParam(required = false) Boolean ativo) {
		return ResponseEntity.ok(servicoService.listar(ativo));
	}

	@GetMapping("/{id}")
	@Operation(
			summary = "Buscar serviço por id",
			description = "Localiza um serviço específico pelo identificador único gerado no cadastro.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Serviço encontrado"),
			@ApiResponse(responseCode = "404", description = "Serviço não encontrado")
	})
	public ResponseEntity<ServicoResponse> buscarPorId(
			@Parameter(description = "Identificador único do serviço cadastrado")
			@PathVariable Long id) {
		return ResponseEntity.ok(servicoService.buscarPorId(id));
	}

	@PutMapping("/{id}")
	@Operation(
			summary = "Atualizar um serviço",
			description = "Substitui os dados do serviço informado, incluindo nome, descrição, preço, duração e status ativo.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Serviço atualizado com sucesso"),
			@ApiResponse(responseCode = "400", description = "Dados inválidos"),
			@ApiResponse(responseCode = "404", description = "Serviço não encontrado")
	})
	public ResponseEntity<ServicoResponse> atualizar(
			@Parameter(description = "Identificador único do serviço que será atualizado")
			@PathVariable Long id,
			@Valid @RequestBody ServicoRequest request) {
		return ResponseEntity.ok(servicoService.atualizar(id, request));
	}

	@DeleteMapping("/{id}")
	@Operation(
			summary = "Excluir um serviço",
			description = "Remove o serviço do cadastro. A exclusão é recusada quando existem agendamentos vinculados a esse serviço.")
	@ApiResponses({
			@ApiResponse(responseCode = "204", description = "Serviço excluído com sucesso"),
			@ApiResponse(responseCode = "404", description = "Serviço não encontrado"),
			@ApiResponse(responseCode = "409", description = "Serviço possui agendamentos vinculados")
	})
	public ResponseEntity<Void> excluir(
			@Parameter(description = "Identificador único do serviço que será excluído")
			@PathVariable Long id) {
		servicoService.excluir(id);
		return ResponseEntity.noContent().build();
	}
}
