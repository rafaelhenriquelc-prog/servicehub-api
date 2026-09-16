package com.petservicehub.dto;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "Dados para cadastro ou atualização de um serviço")
public record OfferedServiceRequest(

		@NotBlank(message = "O título é obrigatório")
		@Size(max = 120, message = "O título deve ter no máximo 120 caracteres")
		@Schema(description = "Título do serviço", example = "Montagem de móveis")
		String title,

		@NotBlank(message = "A descrição é obrigatória")
		@Size(max = 1000, message = "A descrição deve ter no máximo 1000 caracteres")
		@Schema(description = "Descrição do serviço oferecido", example = "Montagem de móveis residenciais e corporativos")
		String description,

		@NotNull(message = "O preço é obrigatório")
		@DecimalMin(value = "0.00", message = "O preço não pode ser negativo")
		@Digits(integer = 8, fraction = 2, message = "O preço deve ter no máximo 8 dígitos e 2 casas decimais")
		@Schema(description = "Preço do serviço", example = "150.00")
		BigDecimal price,

		@NotBlank(message = "A categoria é obrigatória")
		@Size(max = 80, message = "A categoria deve ter no máximo 80 caracteres")
		@Schema(description = "Categoria do serviço", example = "Marcenaria")
		String category,

		@NotNull(message = "O prestador é obrigatório")
		@Schema(description = "Identificador do usuário prestador (PROVIDER)", example = "2")
		Long providerId,

		@Schema(description = "Indica se o serviço está ativo", example = "true")
		Boolean active
) {
}
