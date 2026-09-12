package com.petservicehub.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import com.petservicehub.dto.AgendamentoRequest;
import com.petservicehub.exception.BusinessException;
import com.petservicehub.exception.ResourceNotFoundException;
import com.petservicehub.model.Pet;
import com.petservicehub.model.Servico;
import com.petservicehub.model.StatusAgendamento;
import com.petservicehub.model.Tutor;
import com.petservicehub.repository.AgendamentoRepository;

@ExtendWith(MockitoExtension.class)
class AgendamentoServiceTest {

	@Mock
	private AgendamentoRepository agendamentoRepository;

	@Mock
	private PetService petService;

	@Mock
	private TutorService tutorService;

	@Mock
	private ServicoService servicoService;

	@InjectMocks
	private AgendamentoService agendamentoService;

	@Test
	void deveRecusarQuandoPetNaoPertenceAoTutor() {
		Tutor tutorDoPet = tutor(1L);
		Tutor outroTutor = tutor(2L);
		when(petService.buscarEntidade(10L)).thenReturn(pet(10L, tutorDoPet, true));
		when(tutorService.buscarEntidade(2L)).thenReturn(outroTutor);
		when(servicoService.buscarEntidade(30L)).thenReturn(servico(30L, true));

		AgendamentoRequest request = new AgendamentoRequest(
				10L, 2L, 30L, java.time.LocalDateTime.of(2026, 12, 15, 14, 30), null, StatusAgendamento.AGENDADO);

		assertThatThrownBy(() -> agendamentoService.criar(request))
				.isInstanceOf(BusinessException.class)
				.hasMessage("O pet informado não pertence ao tutor informado")
				.extracting(ex -> ((BusinessException) ex).getStatus())
				.isEqualTo(HttpStatus.BAD_REQUEST);
	}

	@Test
	void deveLancarExcecaoQuandoAgendamentoNaoExiste() {
		when(agendamentoRepository.findByIdWithRelations(99L)).thenReturn(java.util.Optional.empty());

		assertThatThrownBy(() -> agendamentoService.buscarPorId(99L))
				.isInstanceOf(ResourceNotFoundException.class)
				.hasMessage("Agendamento não encontrado com id 99");
	}

	private Tutor tutor(Long id) {
		return Tutor.builder().id(id).nome("Tutor").email("tutor" + id + "@email.com").telefone("11999999999").ativo(true).build();
	}

	private Pet pet(Long id, Tutor tutor, boolean ativo) {
		return Pet.builder().id(id).nome("Thor").especie("Cachorro").idade(4).tutor(tutor).ativo(ativo).build();
	}

	private Servico servico(Long id, boolean ativo) {
		return Servico.builder().id(id).nome("Banho").preco(java.math.BigDecimal.TEN).duracaoMinutos(30).ativo(ativo).build();
	}
}
