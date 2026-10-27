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

import com.petservicehub.dto.UserRequest;
import com.petservicehub.dto.UserResponse;
import com.petservicehub.model.UserRole;
import com.petservicehub.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearer-jwt")
@Tag(name = "Usuários", description = "Administração de usuários. O cadastro público fica em /api/auth/register")
public class UserController {

	private final UserService userService;

	@PostMapping
	@PreAuthorize("hasRole('ADMIN')")
	@Operation(summary = "Cadastrar um usuário (ADMIN)", description = "Permite ao ADMIN criar CLIENT, PROVIDER ou ADMIN. A senha é gravada com BCrypt e nunca é devolvida.")
	@ApiResponses({
			@ApiResponse(responseCode = "201", description = "Usuário cadastrado com sucesso"),
			@ApiResponse(responseCode = "400", description = "Dados inválidos"),
			@ApiResponse(responseCode = "401", description = "Não autenticado"),
			@ApiResponse(responseCode = "403", description = "Acesso negado"),
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
	@PreAuthorize("hasRole('ADMIN')")
	@Operation(summary = "Listar usuários", description = "Somente ADMIN lista todos os usuários da plataforma.")
	@ApiResponse(responseCode = "200", description = "Lista de usuários retornada com sucesso")
	public ResponseEntity<List<UserResponse>> listar(
			@Parameter(description = "Perfil do usuário: ADMIN, CLIENT ou PROVIDER")
			@RequestParam(required = false) UserRole role,
			@Parameter(description = "Quando informado, filtra usuários ativos (true) ou inativos (false)")
			@RequestParam(required = false) Boolean active) {
		return ResponseEntity.ok(userService.listar(role, active));
	}

	@GetMapping("/{id}")
	@PreAuthorize("isAuthenticated()")
	@Operation(summary = "Buscar usuário por id", description = "ADMIN consulta qualquer usuário. Os demais consultam apenas o próprio cadastro. A senha nunca é devolvida.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Usuário encontrado"),
			@ApiResponse(responseCode = "403", description = "Acesso a dados de outro usuário"),
			@ApiResponse(responseCode = "404", description = "Usuário não encontrado")
	})
	public ResponseEntity<UserResponse> buscarPorId(
			@Parameter(description = "Identificador único do usuário cadastrado")
			@PathVariable Long id) {
		return ResponseEntity.ok(userService.buscarPorId(id));
	}

	@PutMapping("/{id}")
	@PreAuthorize("isAuthenticated()")
	@Operation(summary = "Atualizar um usuário", description = "ADMIN atualiza qualquer cadastro. Os demais atualizam apenas o próprio, sem mudar o papel.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Usuário atualizado com sucesso"),
			@ApiResponse(responseCode = "400", description = "Dados inválidos"),
			@ApiResponse(responseCode = "403", description = "Acesso negado"),
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
	@PreAuthorize("hasRole('ADMIN')")
	@Operation(summary = "Excluir um usuário", description = "Somente ADMIN remove um usuário. A exclusão é recusada quando existem vínculos.")
	@ApiResponses({
			@ApiResponse(responseCode = "204", description = "Usuário excluído com sucesso"),
			@ApiResponse(responseCode = "403", description = "Acesso negado"),
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
