package com.petservicehub.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Pet cadastrado")
public record PetResponse(
		@Schema(example = "1") Long id,
		@Schema(example = "Thor") String nome,
		@Schema(example = "Cachorro") String especie,
		@Schema(example = "Labrador") String raca,
		@Schema(example = "4") Integer idade,
		@Schema(example = "true") Boolean ativo,
		TutorResumo tutor
) {
}
