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

import com.petservicehub.dto.UserRequest;
import com.petservicehub.dto.UserResponse;
import com.petservicehub.model.UserRole;
import com.petservicehub.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "Usuários", description = "CRUD de clientes e prestadores da plataforma")
public class UserController {

	private final UserService userService;

	@PostMapping
	@Operation(
			summary = "Cadastrar um novo usuário",
			description = "Cria um cliente (CLIENT) ou um prestador (PROVIDER). O e-mail deve ser único. A senha é armazenada somente como hash. Se active não for enviado, o cadastro fica ativo.")
	@ApiResponses({
			@ApiResponse(responseCode = "201", description = "Usuário cadastrado com sucesso"),
			@ApiResponse(responseCode = "400", description = "Dados inválidos"),
			@ApiResponse(responseCode = "409", description = "E-mail já cadastrado")
	})
	public ResponseEntity<UserResponse> criar(@Valid @RequestBody UserRequest request) {
		UserResponse criado = userService.criar(request);
		URI location = ServletUriComponentsBuilder.fromCurrentRequest()
				.path("/{id}")
				.buildAndExpand(criado.id())
				.toUri();
		return ResponseEntity.created(location).body(criado);
	}

	@GetMapping
	@Operation(
			summary = "Listar usuários",
			description = "Retorna os usuários cadastrados. Os filtros opcionais permitem restringir por perfil (CLIENT ou PROVIDER) e por status ativo.")
	@ApiResponse(responseCode = "200", description = "Lista de usuários retornada com sucesso")
	public ResponseEntity<List<UserResponse>> listar(
			@Parameter(description = "Perfil do usuário: CLIENT ou PROVIDER")
			@RequestParam(required = false) UserRole role,
			@Parameter(description = "Quando informado, filtra usuários ativos (true) ou inativos (false)")
			@RequestParam(required = false) Boolean active) {
		return ResponseEntity.ok(userService.listar(role, active));
	}

	@GetMapping("/{id}")
	@Operation(
			summary = "Buscar usuário por id",
			description = "Localiza um usuário específico pelo identificador único. A senha nunca é devolvida.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Usuário encontrado"),
			@ApiResponse(responseCode = "404", description = "Usuário não encontrado")
	})
	public ResponseEntity<UserResponse> buscarPorId(
			@Parameter(description = "Identificador único do usuário cadastrado")
			@PathVariable Long id) {
		return ResponseEntity.ok(userService.buscarPorId(id));
	}

	@PutMapping("/{id}")
	@Operation(
			summary = "Atualizar um usuário",
			description = "Substitui os dados do usuário informado. O e-mail continua precisando ser único e a nova senha é gravada como hash.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Usuário atualizado com sucesso"),
			@ApiResponse(responseCode = "400", description = "Dados inválidos"),
			@ApiResponse(responseCode = "404", description = "Usuário não encontrado"),
			@ApiResponse(responseCode = "409", description = "E-mail já cadastrado")
	})
	public ResponseEntity<UserResponse> atualizar(
			@Parameter(description = "Identificador único do usuário que será atualizado")
			@PathVariable Long id,
			@Valid @RequestBody UserRequest request) {
		return ResponseEntity.ok(userService.atualizar(id, request));
	}

	@DeleteMapping("/{id}")
	@Operation(
			summary = "Excluir um usuário",
			description = "Remove o usuário do cadastro. A exclusão é recusada quando existem serviços, solicitações ou avaliações vinculadas.")
	@ApiResponses({
			@ApiResponse(responseCode = "204", description = "Usuário excluído com sucesso"),
			@ApiResponse(responseCode = "404", description = "Usuário não encontrado"),
			@ApiResponse(responseCode = "409", description = "Usuário possui vínculos que impedem a exclusão")
	})
	public ResponseEntity<Void> excluir(
			@Parameter(description = "Identificador único do usuário que será excluído")
			@PathVariable Long id) {
		userService.excluir(id);
		return ResponseEntity.noContent().build();
	}
}
