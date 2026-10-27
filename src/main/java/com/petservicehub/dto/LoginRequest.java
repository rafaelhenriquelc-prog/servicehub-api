package com.petservicehub.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Credenciais de autenticação")
public record LoginRequest(

		@NotBlank(message = "O e-mail é obrigatório")
		@Email(message = "O e-mail deve ser válido")
		@Schema(description = "E-mail cadastrado", example = "ana.souza@email.com")
		String email,

		@NotBlank(message = "A senha é obrigatória")
		@Schema(description = "Senha em texto", example = "senha123")
		String password
) {
}
