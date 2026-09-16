package com.petservicehub.dto;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Dados resumidos de um serviço")
public record OfferedServiceSummary(
		@Schema(example = "1") Long id,
		@Schema(example = "Montagem de móveis") String title,
		@Schema(example = "150.00") BigDecimal price,
		@Schema(example = "Marcenaria") String category
) {
}
