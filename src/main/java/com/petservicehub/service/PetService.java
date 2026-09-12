package com.petservicehub.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.petservicehub.dto.EntityMapper;
import com.petservicehub.dto.PetRequest;
import com.petservicehub.dto.PetResponse;
import com.petservicehub.exception.BusinessException;
import com.petservicehub.exception.ResourceNotFoundException;
import com.petservicehub.model.Pet;
import com.petservicehub.model.Tutor;
import com.petservicehub.repository.AgendamentoRepository;
import com.petservicehub.repository.PetRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PetService {

	private final PetRepository petRepository;
	private final TutorService tutorService;
	private final AgendamentoRepository agendamentoRepository;

	@Transactional
	public PetResponse criar(PetRequest request) {
		Tutor tutor = tutorAtivo(request.tutorId());
		Pet pet = Pet.builder()
				.nome(request.nome())
				.especie(request.especie())
				.raca(request.raca())
				.idade(request.idade())
				.tutor(tutor)
				.ativo(request.ativo() != null ? request.ativo() : true)
				.build();
		return EntityMapper.toResponse(petRepository.save(pet));
	}

	@Transactional(readOnly = true)
	public List<PetResponse> listar(Boolean ativo, Long tutorId) {
		List<Pet> pets;
		if (tutorId != null && ativo != null) {
			pets = petRepository.findByTutorIdAndAtivoWithTutor(tutorId, ativo);
		} else if (tutorId != null) {
			pets = petRepository.findByTutorIdWithTutor(tutorId);
		} else if (ativo != null) {
			pets = petRepository.findByAtivoWithTutor(ativo);
		} else {
			pets = petRepository.findAllWithTutor();
		}
		return pets.stream().map(EntityMapper::toResponse).toList();
	}

	@Transactional(readOnly = true)
	public PetResponse buscarPorId(Long id) {
		return EntityMapper.toResponse(buscarEntidade(id));
	}

	@Transactional
	public PetResponse atualizar(Long id, PetRequest request) {
		Pet pet = buscarEntidade(id);
		Tutor tutor = tutorAtivo(request.tutorId());
		pet.setNome(request.nome());
		pet.setEspecie(request.especie());
		pet.setRaca(request.raca());
		pet.setIdade(request.idade());
		pet.setTutor(tutor);
		if (request.ativo() != null) {
			pet.setAtivo(request.ativo());
		}
		return EntityMapper.toResponse(petRepository.save(pet));
	}

	@Transactional
	public void excluir(Long id) {
		Pet pet = buscarEntidade(id);
		if (agendamentoRepository.existsByPetId(id)) {
			throw new BusinessException(HttpStatus.CONFLICT, "Não é possível excluir o pet porque existem agendamentos vinculados");
		}
		petRepository.delete(pet);
	}

	@Transactional(readOnly = true)
	public Pet buscarEntidade(Long id) {
		return petRepository.findByIdWithTutor(id)
				.orElseThrow(() -> new ResourceNotFoundException("Pet não encontrado com id " + id));
	}

	private Tutor tutorAtivo(Long tutorId) {
		Tutor tutor = tutorService.buscarEntidade(tutorId);
		if (!Boolean.TRUE.equals(tutor.getAtivo())) {
			throw new BusinessException(HttpStatus.BAD_REQUEST, "O tutor informado está inativo");
		}
		return tutor;
	}
}
