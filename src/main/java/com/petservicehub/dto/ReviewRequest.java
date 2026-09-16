package com.petservicehub.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "Dados para cadastro ou atualização de uma avaliação")
public record ReviewRequest(

		@NotNull(message = "A solicitação é obrigatória")
		@Schema(description = "Identificador da solicitação concluída", example = "1")
		Long requestId,

		@NotNull(message = "O avaliador é obrigatório")
		@Schema(description = "Identificador do cliente que avalia", example = "1")
		Long reviewerId,

		@NotNull(message = "A nota é obrigatória")
		@Min(value = 1, message = "A nota mínima é 1")
		@Max(value = 5, message = "A nota máxima é 5")
		@Schema(description = "Nota de 1 a 5", example = "5")
		Integer rating,

		@Size(max = 1000, message = "O comentário deve ter no máximo 1000 caracteres")
		@Schema(description = "Comentário da avaliação", example = "Serviço pontual e muito bem feito")
		String comment
) {
}
