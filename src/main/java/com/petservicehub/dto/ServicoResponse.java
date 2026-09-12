package com.petservicehub.dto;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Serviço cadastrado")
public record ServicoResponse(
		@Schema(example = "1") Long id,
		@Schema(example = "Banho e tosa") String nome,
		@Schema(example = "Banho completo com tosa higiênica") String descricao,
		@Schema(example = "89.90") BigDecimal preco,
		@Schema(example = "60") Integer duracaoMinutos,
		@Schema(example = "true") Boolean ativo
) {
}
