package com.petservicehub.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.petservicehub.dto.EntityMapper;
import com.petservicehub.dto.TutorRequest;
import com.petservicehub.dto.TutorResponse;
import com.petservicehub.exception.BusinessException;
import com.petservicehub.exception.ResourceNotFoundException;
import com.petservicehub.model.Tutor;
import com.petservicehub.repository.AgendamentoRepository;
import com.petservicehub.repository.PetRepository;
import com.petservicehub.repository.TutorRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TutorService {

	private final TutorRepository tutorRepository;
	private final PetRepository petRepository;
	private final AgendamentoRepository agendamentoRepository;

	@Transactional
	public TutorResponse criar(TutorRequest request) {
		validarEmailUnico(request.email(), null);
		Tutor tutor = Tutor.builder()
				.nome(request.nome())
				.email(request.email())
				.telefone(request.telefone())
				.ativo(request.ativo() != null ? request.ativo() : true)
				.build();
		return EntityMapper.toResponse(tutorRepository.save(tutor));
	}

	@Transactional(readOnly = true)
	public List<TutorResponse> listar(Boolean ativo) {
		List<Tutor> tutores = ativo == null ? tutorRepository.findAll() : tutorRepository.findByAtivo(ativo);
		return tutores.stream().map(EntityMapper::toResponse).toList();
	}

	@Transactional(readOnly = true)
	public TutorResponse buscarPorId(Long id) {
		return EntityMapper.toResponse(buscarEntidade(id));
	}

	@Transactional
	public TutorResponse atualizar(Long id, TutorRequest request) {
		Tutor tutor = buscarEntidade(id);
		validarEmailUnico(request.email(), id);
		tutor.setNome(request.nome());
		tutor.setEmail(request.email());
		tutor.setTelefone(request.telefone());
		if (request.ativo() != null) {
			tutor.setAtivo(request.ativo());
		}
		return EntityMapper.toResponse(tutorRepository.save(tutor));
	}

	@Transactional
	public void excluir(Long id) {
		Tutor tutor = buscarEntidade(id);
		if (petRepository.existsByTutorId(id)) {
			throw new BusinessException(HttpStatus.CONFLICT, "Não é possível excluir o tutor porque existem pets vinculados");
		}
		if (agendamentoRepository.existsByTutorId(id)) {
			throw new BusinessException(HttpStatus.CONFLICT, "Não é possível excluir o tutor porque existem agendamentos vinculados");
		}
		tutorRepository.delete(tutor);
	}

	@Transactional(readOnly = true)
	public Tutor buscarEntidade(Long id) {
		return tutorRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Tutor não encontrado com id " + id));
	}

	private void validarEmailUnico(String email, Long idAtual) {
		boolean emailEmUso = idAtual == null
				? tutorRepository.existsByEmailIgnoreCase(email)
				: tutorRepository.existsByEmailIgnoreCaseAndIdNot(email, idAtual);
		if (emailEmUso) {
			throw new BusinessException(HttpStatus.CONFLICT, "Já existe um tutor cadastrado com o e-mail " + email);
		}
	}
}
