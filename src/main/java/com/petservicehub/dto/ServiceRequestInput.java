package com.petservicehub.dto;

import java.time.LocalDateTime;

import com.petservicehub.model.RequestStatus;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "Dados para cadastro ou atualização de uma solicitação de serviço")
public record ServiceRequestInput(

		@NotNull(message = "O serviço é obrigatório")
		@Schema(description = "Identificador do serviço contratado", example = "1")
		Long serviceId,

		@NotNull(message = "O cliente é obrigatório")
		@Schema(description = "Identificador do usuário cliente (CLIENT)", example = "1")
		Long clientId,

		@Schema(description = "Status da solicitação. Se omitido no cadastro, assume PENDING", example = "PENDING")
		RequestStatus status,

		@Schema(description = "Data e hora combinadas para a execução", example = "2026-09-20T14:30:00")
		LocalDateTime scheduledAt,

		@Size(max = 500, message = "As observações devem ter no máximo 500 caracteres")
		@Schema(description = "Observações do cliente", example = "Apartamento no 3º andar, sem elevador")
		String notes
) {
}
