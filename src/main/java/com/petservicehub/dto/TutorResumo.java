package com.petservicehub.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Dados resumidos do tutor")
public record TutorResumo(
		@Schema(example = "1") Long id,
		@Schema(example = "Ana Souza") String nome,
		@Schema(example = "ana.souza@email.com") String email
) {
}
