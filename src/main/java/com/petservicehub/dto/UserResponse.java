package com.petservicehub.dto;

import java.time.LocalDateTime;

import com.petservicehub.model.UserRole;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Usuário cadastrado na plataforma")
public record UserResponse(
		@Schema(example = "1") Long id,
		@Schema(example = "Ana Souza") String fullName,
		@Schema(example = "ana.souza@email.com") String email,
		@Schema(example = "11988887777") String phone,
		@Schema(example = "Designer de interiores com 8 anos de experiência") String bio,
		@Schema(example = "https://cdn.servicehub.com/avatars/ana.png") String avatarUrl,
		@Schema(example = "CLIENT") UserRole role,
		@Schema(example = "true") Boolean active,
		@Schema(example = "2026-09-16T18:00:00") LocalDateTime createdAt,
		@Schema(example = "2026-09-16T18:00:00") LocalDateTime updatedAt
) {
}
