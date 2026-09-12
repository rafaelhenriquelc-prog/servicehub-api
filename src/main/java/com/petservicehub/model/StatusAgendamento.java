package com.petservicehub.model;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Situação do agendamento")
public enum StatusAgendamento {
	AGENDADO,
	CONCLUIDO,
	CANCELADO
}
