package com.petservicehub.dto;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "Dados para cadastro ou atualização de um serviço")
public record ServicoRequest(

		@NotBlank(message = "O nome é obrigatório")
		@Size(max = 100, message = "O nome deve ter no máximo 100 caracteres")
		@Schema(description = "Nome do serviço", example = "Banho e tosa")
		String nome,

		@Size(max = 500, message = "A descrição deve ter no máximo 500 caracteres")
		@Schema(description = "Descrição do serviço", example = "Banho completo com tosa higiênica")
		String descricao,

		@NotNull(message = "O preço é obrigatório")
		@DecimalMin(value = "0.00", message = "O preço não pode ser negativo")
		@Digits(integer = 8, fraction = 2, message = "O preço deve ter no máximo 8 dígitos e 2 casas decimais")
		@Schema(description = "Preço do serviço", example = "89.90")
		BigDecimal preco,

		@NotNull(message = "A duração é obrigatória")
		@Min(value = 1, message = "A duração deve ser de pelo menos 1 minuto")
		@Max(value = 1440, message = "A duração deve ser de no máximo 1440 minutos")
		@Schema(description = "Duração do serviço em minutos", example = "60")
		Integer duracaoMinutos,

		@Schema(description = "Indica se o serviço está ativo", example = "true")
		Boolean ativo
) {
}
