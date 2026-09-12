package com.petservicehub.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.petservicehub.dto.AgendamentoRequest;
import com.petservicehub.dto.AgendamentoResponse;
import com.petservicehub.dto.EntityMapper;
import com.petservicehub.exception.BusinessException;
import com.petservicehub.exception.ResourceNotFoundException;
import com.petservicehub.model.Agendamento;
import com.petservicehub.model.Pet;
import com.petservicehub.model.Servico;
import com.petservicehub.model.StatusAgendamento;
import com.petservicehub.model.Tutor;
import com.petservicehub.repository.AgendamentoRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AgendamentoService {

	private final AgendamentoRepository agendamentoRepository;
	private final PetService petService;
	private final TutorService tutorService;
	private final ServicoService servicoService;

	@Transactional
	public AgendamentoResponse criar(AgendamentoRequest request) {
		Relacionamentos relacionamentos = carregarRelacionamentos(request);
		Agendamento agendamento = Agendamento.builder()
				.pet(relacionamentos.pet())
				.tutor(relacionamentos.tutor())
				.servico(relacionamentos.servico())
				.dataHora(request.dataHora())
				.observacao(request.observacao())
				.status(request.status() != null ? request.status() : StatusAgendamento.AGENDADO)
				.build();
		Agendamento salvo = agendamentoRepository.save(agendamento);
		return EntityMapper.toResponse(buscarEntidade(salvo.getId()));
	}

	@Transactional(readOnly = true)
	public List<AgendamentoResponse> listar(StatusAgendamento status, Long tutorId, Long petId, Long servicoId) {
		return agendamentoRepository.findAllWithRelations(status, tutorId, petId, servicoId)
				.stream()
				.map(EntityMapper::toResponse)
				.toList();
	}

	@Transactional(readOnly = true)
	public AgendamentoResponse buscarPorId(Long id) {
		return EntityMapper.toResponse(buscarEntidade(id));
	}

	@Transactional
	public AgendamentoResponse atualizar(Long id, AgendamentoRequest request) {
		Agendamento agendamento = buscarEntidade(id);
		Relacionamentos relacionamentos = carregarRelacionamentos(request);
		agendamento.setPet(relacionamentos.pet());
		agendamento.setTutor(relacionamentos.tutor());
		agendamento.setServico(relacionamentos.servico());
		agendamento.setDataHora(request.dataHora());
		agendamento.setObservacao(request.observacao());
		if (request.status() != null) {
			agendamento.setStatus(request.status());
		}
		agendamentoRepository.save(agendamento);
		return EntityMapper.toResponse(buscarEntidade(id));
	}

	@Transactional
	public void excluir(Long id) {
		Agendamento agendamento = buscarEntidade(id);
		agendamentoRepository.delete(agendamento);
	}

	private Relacionamentos carregarRelacionamentos(AgendamentoRequest request) {
		Pet pet = petService.buscarEntidade(request.petId());
		Tutor tutor = tutorService.buscarEntidade(request.tutorId());
		Servico servico = servicoService.buscarEntidade(request.servicoId());

		if (!pet.getTutor().getId().equals(tutor.getId())) {
			throw new BusinessException(HttpStatus.BAD_REQUEST, "O pet informado não pertence ao tutor informado");
		}
		if (!Boolean.TRUE.equals(pet.getAtivo())) {
			throw new BusinessException(HttpStatus.BAD_REQUEST, "O pet informado está inativo");
		}
		if (!Boolean.TRUE.equals(tutor.getAtivo())) {
			throw new BusinessException(HttpStatus.BAD_REQUEST, "O tutor informado está inativo");
		}
		if (!Boolean.TRUE.equals(servico.getAtivo())) {
			throw new BusinessException(HttpStatus.BAD_REQUEST, "O serviço informado está inativo");
		}
		return new Relacionamentos(pet, tutor, servico);
	}

	private Agendamento buscarEntidade(Long id) {
		return agendamentoRepository.findByIdWithRelations(id)
				.orElseThrow(() -> new ResourceNotFoundException("Agendamento não encontrado com id " + id));
	}

	private record Relacionamentos(Pet pet, Tutor tutor, Servico servico) {
	}
}
