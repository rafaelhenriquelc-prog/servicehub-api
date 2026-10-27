package com.petservicehub.controller;

import java.net.URI;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.petservicehub.dto.LoginRequest;
import com.petservicehub.dto.LoginResponse;
import com.petservicehub.dto.RegisterRequest;
import com.petservicehub.dto.UserResponse;
import com.petservicehub.service.AuthService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Autenticação", description = "Registro público e login com JWT")
public class AuthController {

	private final AuthService authService;

	@PostMapping("/register")
	@Operation(
			summary = "Registrar cliente ou prestador",
			description = "Cria um usuário CLIENT ou PROVIDER. ADMIN não é aceito. O e-mail é gravado em minúsculas e a senha somente como BCrypt. A senha nunca é devolvida.")
	@ApiResponses({
			@ApiResponse(responseCode = "201", description = "Usuário registrado com sucesso"),
			@ApiResponse(responseCode = "400", description = "Dados inválidos ou tentativa de cadastrar ADMIN"),
			@ApiResponse(responseCode = "409", description = "E-mail já cadastrado")
	})
	public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterRequest request) {
		UserResponse criado = authService.register(request);
		URI location = ServletUriComponentsBuilder.fromCurrentContextPath()
				.path("/api/users/{id}")
				.buildAndExpand(criado.id())
				.toUri();
		return ResponseEntity.created(location).body(criado);
	}

	@PostMapping("/login")
	@Operation(
			summary = "Autenticar usuário",
			description = "Valida e-mail e senha e devolve um JWT. Credenciais inválidas ou usuário inativo resultam em 401 genérico.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Autenticação realizada com sucesso"),
			@ApiResponse(responseCode = "401", description = "Credenciais inválidas")
	})
	public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
		return ResponseEntity.ok(authService.login(request));
	}
}
