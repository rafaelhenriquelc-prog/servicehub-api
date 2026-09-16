package com.petservicehub.dto;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Avaliação de uma solicitação concluída")
public record ReviewResponse(
		@Schema(example = "1") Long id,
		@Schema(example = "1") Long requestId,
		UserSummary reviewer,
		@Schema(example = "5") Integer rating,
		@Schema(example = "Serviço pontual e muito bem feito") String comment,
		@Schema(example = "2026-09-16T18:00:00") LocalDateTime createdAt
) {
}
