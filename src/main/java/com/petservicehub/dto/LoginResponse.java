package com.petservicehub.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Resposta de autenticação com JWT e dados públicos do usuário")
public record LoginResponse(
		@Schema(description = "Token JWT", example = "eyJhbGciOiJIUzI1NiJ9...")
		String token,
		@Schema(description = "Tipo do token", example = "Bearer")
		String tokenType,
		@Schema(description = "Validade do token em milissegundos", example = "3600000")
		Long expiresIn,
		UserResponse user
) {
}
