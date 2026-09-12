package com.petservicehub.dto;

import com.petservicehub.model.Agendamento;
import com.petservicehub.model.Pet;
import com.petservicehub.model.Servico;
import com.petservicehub.model.Tutor;

public final class EntityMapper {

	private EntityMapper() {
	}

	public static TutorResponse toResponse(Tutor tutor) {
		return new TutorResponse(tutor.getId(), tutor.getNome(), tutor.getEmail(), tutor.getTelefone(), tutor.getAtivo());
	}

	public static TutorResumo toResumo(Tutor tutor) {
		return new TutorResumo(tutor.getId(), tutor.getNome(), tutor.getEmail());
	}

	public static PetResponse toResponse(Pet pet) {
		return new PetResponse(
				pet.getId(),
				pet.getNome(),
				pet.getEspecie(),
				pet.getRaca(),
				pet.getIdade(),
				pet.getAtivo(),
				toResumo(pet.getTutor()));
	}

	public static PetResumo toResumo(Pet pet) {
		return new PetResumo(pet.getId(), pet.getNome(), pet.getEspecie());
	}

	public static ServicoResponse toResponse(Servico servico) {
		return new ServicoResponse(
				servico.getId(),
				servico.getNome(),
				servico.getDescricao(),
				servico.getPreco(),
				servico.getDuracaoMinutos(),
				servico.getAtivo());
	}

	public static ServicoResumo toResumo(Servico servico) {
		return new ServicoResumo(servico.getId(), servico.getNome(), servico.getPreco(), servico.getDuracaoMinutos());
	}

	public static AgendamentoResponse toResponse(Agendamento agendamento) {
		return new AgendamentoResponse(
				agendamento.getId(),
				toResumo(agendamento.getPet()),
				toResumo(agendamento.getTutor()),
				toResumo(agendamento.getServico()),
				agendamento.getDataHora(),
				agendamento.getObservacao(),
				agendamento.getStatus());
	}
}
