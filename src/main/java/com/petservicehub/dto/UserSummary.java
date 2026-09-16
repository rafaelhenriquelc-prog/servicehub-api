package com.petservicehub.dto;

import com.petservicehub.model.UserRole;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Dados resumidos de um usuário")
public record UserSummary(
		@Schema(example = "1") Long id,
		@Schema(example = "Ana Souza") String fullName,
		@Schema(example = "ana.souza@email.com") String email,
		@Schema(example = "CLIENT") UserRole role
) {
}
