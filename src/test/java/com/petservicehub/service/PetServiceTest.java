package com.petservicehub.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import com.petservicehub.dto.PetRequest;
import com.petservicehub.dto.PetResponse;
import com.petservicehub.exception.BusinessException;
import com.petservicehub.exception.ResourceNotFoundException;
import com.petservicehub.model.Pet;
import com.petservicehub.model.Tutor;
import com.petservicehub.repository.AgendamentoRepository;
import com.petservicehub.repository.PetRepository;

@ExtendWith(MockitoExtension.class)
class PetServiceTest {

	@Mock
	private PetRepository petRepository;

	@Mock
	private TutorService tutorService;

	@Mock
	private AgendamentoRepository agendamentoRepository;

	@InjectMocks
	private PetService petService;

	@Test
	void deveDefinirAtivoComoTrueQuandoNaoInformado() {
		Tutor tutor = tutor(1L, true);
		when(tutorService.buscarEntidade(1L)).thenReturn(tutor);
		when(petRepository.save(any(Pet.class))).thenAnswer(invocation -> {
			Pet salvo = invocation.getArgument(0);
			salvo.setId(1L);
			return salvo;
		});

		PetResponse criado = petService.criar(new PetRequest("Mimi", "Gato", "Siamês", 2, 1L, null));

		assertThat(criado.id()).isEqualTo(1L);
		assertThat(criado.ativo()).isTrue();
		assertThat(criado.tutor().id()).isEqualTo(1L);
	}

	@Test
	void deveRecusarPetComTutorInativo() {
		when(tutorService.buscarEntidade(1L)).thenReturn(tutor(1L, false));

		assertThatThrownBy(() -> petService.criar(new PetRequest("Mimi", "Gato", "Siamês", 2, 1L, true)))
				.isInstanceOf(BusinessException.class)
				.hasMessage("O tutor informado está inativo")
				.extracting(ex -> ((BusinessException) ex).getStatus())
				.isEqualTo(HttpStatus.BAD_REQUEST);
	}

	@Test
	void deveListarApenasPetsAtivosQuandoFiltroInformado() {
		when(petRepository.findByAtivoWithTutor(true))
				.thenReturn(List.of(Pet.builder().nome("Thor").ativo(true).tutor(tutor(1L, true)).build()));

		List<PetResponse> pets = petService.listar(true, null);

		assertThat(pets).hasSize(1);
		assertThat(pets.getFirst().nome()).isEqualTo("Thor");
	}

	@Test
	void deveLancarExcecaoQuandoPetNaoExiste() {
		when(petRepository.findByIdWithTutor(99L)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> petService.buscarPorId(99L))
				.isInstanceOf(ResourceNotFoundException.class)
				.hasMessage("Pet não encontrado com id 99");
	}

	@Test
	void deveExcluirPetExistente() {
		Pet pet = Pet.builder().id(1L).nome("Thor").tutor(tutor(1L, true)).build();
		when(petRepository.findByIdWithTutor(1L)).thenReturn(Optional.of(pet));
		when(agendamentoRepository.existsByPetId(1L)).thenReturn(false);

		petService.excluir(1L);

		verify(petRepository).delete(pet);
	}

	@Test
	void deveRecusarExclusaoQuandoPetPossuiAgendamentos() {
		Pet pet = Pet.builder().id(1L).nome("Thor").tutor(tutor(1L, true)).build();
		when(petRepository.findByIdWithTutor(1L)).thenReturn(Optional.of(pet));
		when(agendamentoRepository.existsByPetId(1L)).thenReturn(true);

		assertThatThrownBy(() -> petService.excluir(1L))
				.isInstanceOf(BusinessException.class)
				.extracting(ex -> ((BusinessException) ex).getStatus())
				.isEqualTo(HttpStatus.CONFLICT);
	}

	private Tutor tutor(Long id, boolean ativo) {
		return Tutor.builder()
				.id(id)
				.nome("João")
				.email("joao@email.com")
				.telefone("11999999999")
				.ativo(ativo)
				.build();
	}
}
