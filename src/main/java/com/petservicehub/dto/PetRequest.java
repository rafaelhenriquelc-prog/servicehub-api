package com.petservicehub.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "Dados para cadastro ou atualização de um pet")
public record PetRequest(

		@NotBlank(message = "O nome é obrigatório")
		@Size(max = 100, message = "O nome deve ter no máximo 100 caracteres")
		@Schema(description = "Nome do pet", example = "Thor")
		String nome,

		@NotBlank(message = "A espécie é obrigatória")
		@Size(max = 50, message = "A espécie deve ter no máximo 50 caracteres")
		@Schema(description = "Espécie do pet", example = "Cachorro")
		String especie,

		@Size(max = 80, message = "A raça deve ter no máximo 80 caracteres")
		@Schema(description = "Raça do pet", example = "Labrador")
		String raca,

		@NotNull(message = "A idade é obrigatória")
		@Min(value = 0, message = "A idade não pode ser negativa")
		@Max(value = 100, message = "A idade deve ser no máximo 100 anos")
		@Schema(description = "Idade do pet em anos", example = "4")
		Integer idade,

		@NotNull(message = "O tutor é obrigatório")
		@Schema(description = "Identificador do tutor responsável", example = "1")
		Long tutorId,

		@Schema(description = "Indica se o cadastro está ativo", example = "true")
		Boolean ativo
) {
}
