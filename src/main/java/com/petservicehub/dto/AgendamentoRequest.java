package com.petservicehub.dto;

import java.time.LocalDateTime;

import com.petservicehub.model.StatusAgendamento;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "Dados para cadastro ou atualização de um agendamento")
public record AgendamentoRequest(

		@NotNull(message = "O pet é obrigatório")
		@Schema(description = "Identificador do pet", example = "1")
		Long petId,

		@NotNull(message = "O tutor é obrigatório")
		@Schema(description = "Identificador do tutor", example = "1")
		Long tutorId,

		@NotNull(message = "O serviço é obrigatório")
		@Schema(description = "Identificador do serviço", example = "1")
		Long servicoId,

		@NotNull(message = "A data e hora são obrigatórias")
		@Schema(description = "Data e hora do agendamento", example = "2026-12-15T14:30:00")
		LocalDateTime dataHora,

		@Size(max = 500, message = "A observação deve ter no máximo 500 caracteres")
		@Schema(description = "Observação do agendamento", example = "Pet fica nervoso com secador")
		String observacao,

		@Schema(description = "Status do agendamento. Se omitido no cadastro, assume AGENDADO", example = "AGENDADO")
		StatusAgendamento status
) {
}
