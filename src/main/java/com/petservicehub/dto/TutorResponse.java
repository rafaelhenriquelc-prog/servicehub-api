package com.petservicehub.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Tutor cadastrado")
public record TutorResponse(
		@Schema(example = "1") Long id,
		@Schema(example = "Ana Souza") String nome,
		@Schema(example = "ana.souza@email.com") String email,
		@Schema(example = "11988887777") String telefone,
		@Schema(example = "true") Boolean ativo
) {
}
