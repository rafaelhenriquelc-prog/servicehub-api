package com.petservicehub.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Serviço oferecido por um prestador")
public record OfferedServiceResponse(
		@Schema(example = "1") Long id,
		@Schema(example = "Montagem de móveis") String title,
		@Schema(example = "Montagem de móveis residenciais e corporativos") String description,
		@Schema(example = "150.00") BigDecimal price,
		@Schema(example = "Marcenaria") String category,
		UserSummary provider,
		@Schema(example = "true") Boolean active,
		@Schema(example = "2026-09-16T18:00:00") LocalDateTime createdAt
) {
}
