package com.petservicehub.dto;

import com.petservicehub.model.UserRole;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "Dados para cadastro ou atualização de um usuário")
public record UserRequest(

		@NotBlank(message = "O nome completo é obrigatório")
		@Size(max = 120, message = "O nome completo deve ter no máximo 120 caracteres")
		@Schema(description = "Nome completo do usuário", example = "Ana Souza")
		String fullName,

		@NotBlank(message = "O e-mail é obrigatório")
		@Email(message = "O e-mail deve ser válido")
		@Size(max = 120, message = "O e-mail deve ter no máximo 120 caracteres")
		@Schema(description = "E-mail único do usuário", example = "ana.souza@email.com")
		String email,

		@NotBlank(message = "A senha é obrigatória")
		@Size(min = 6, max = 80, message = "A senha deve ter entre 6 e 80 caracteres")
		@Schema(description = "Senha em texto. É armazenada apenas como hash", example = "senha123")
		String password,

		@Size(max = 20, message = "O telefone deve ter no máximo 20 caracteres")
		@Schema(description = "Telefone de contato", example = "11988887777")
		String phone,

		@Size(max = 500, message = "A biografia deve ter no máximo 500 caracteres")
		@Schema(description = "Biografia do usuário", example = "Designer de interiores com 8 anos de experiência")
		String bio,

		@Size(max = 255, message = "A URL do avatar deve ter no máximo 255 caracteres")
		@Schema(description = "URL da foto de perfil", example = "https://cdn.servicehub.com/avatars/ana.png")
		String avatarUrl,

		@NotNull(message = "O perfil é obrigatório")
		@Schema(description = "Perfil do usuário na plataforma", example = "CLIENT")
		UserRole role,

		@Schema(description = "Indica se o cadastro está ativo", example = "true")
		Boolean active
) {
}
