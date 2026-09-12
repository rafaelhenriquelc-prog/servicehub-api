package com.petservicehub.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Dados para cadastro ou atualização de um tutor")
public record TutorRequest(

		@NotBlank(message = "O nome é obrigatório")
		@Size(max = 120, message = "O nome deve ter no máximo 120 caracteres")
		@Schema(description = "Nome do tutor", example = "Ana Souza")
		String nome,

		@NotBlank(message = "O e-mail é obrigatório")
		@Email(message = "O e-mail deve ser válido")
		@Size(max = 120, message = "O e-mail deve ter no máximo 120 caracteres")
		@Schema(description = "E-mail do tutor", example = "ana.souza@email.com")
		String email,

		@NotBlank(message = "O telefone é obrigatório")
		@Size(max = 20, message = "O telefone deve ter no máximo 20 caracteres")
		@Schema(description = "Telefone do tutor", example = "11988887777")
		String telefone,

		@Schema(description = "Indica se o cadastro está ativo", example = "true")
		Boolean ativo
) {
}
