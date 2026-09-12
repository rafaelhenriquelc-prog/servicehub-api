package com.petservicehub.dto;

import java.time.LocalDateTime;

import com.petservicehub.model.StatusAgendamento;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Agendamento cadastrado")
public record AgendamentoResponse(
		@Schema(example = "1") Long id,
		PetResumo pet,
		TutorResumo tutor,
		ServicoResumo servico,
		@Schema(example = "2026-12-15T14:30:00") LocalDateTime dataHora,
		@Schema(example = "Pet fica nervoso com secador") String observacao,
		@Schema(example = "AGENDADO") StatusAgendamento status
) {
}
