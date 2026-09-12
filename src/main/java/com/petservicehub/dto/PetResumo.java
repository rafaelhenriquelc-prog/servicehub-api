package com.petservicehub.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Dados resumidos do pet")
public record PetResumo(
		@Schema(example = "1") Long id,
		@Schema(example = "Thor") String nome,
		@Schema(example = "Cachorro") String especie
) {
}
