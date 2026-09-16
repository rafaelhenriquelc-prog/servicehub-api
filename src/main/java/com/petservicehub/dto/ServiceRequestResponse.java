package com.petservicehub.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.petservicehub.model.RequestStatus;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Solicitação de contratação de um serviço")
public record ServiceRequestResponse(
		@Schema(example = "1") Long id,
		OfferedServiceSummary service,
		UserSummary client,
		@Schema(example = "PENDING") RequestStatus status,
		@Schema(example = "2026-09-20T14:30:00") LocalDateTime scheduledAt,
		@Schema(example = "Apartamento no 3º andar, sem elevador") String notes,
		@Schema(example = "150.00") BigDecimal totalPrice,
		@Schema(example = "2026-09-16T18:00:00") LocalDateTime createdAt,
		@Schema(example = "2026-09-16T18:00:00") LocalDateTime updatedAt
) {
}
