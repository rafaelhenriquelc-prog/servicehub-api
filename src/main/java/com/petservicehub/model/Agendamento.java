package com.petservicehub.model;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "agendamentos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Agendamento de um serviço para um pet")
public class Agendamento {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	@JoinColumn(name = "pet_id", nullable = false)
	private Pet pet;

	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	@JoinColumn(name = "tutor_id", nullable = false)
	private Tutor tutor;

	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	@JoinColumn(name = "servico_id", nullable = false)
	private Servico servico;

	@Column(name = "data_hora", nullable = false)
	private LocalDateTime dataHora;

	@Column(length = 500)
	private String observacao;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	@Builder.Default
	private StatusAgendamento status = StatusAgendamento.AGENDADO;
}
