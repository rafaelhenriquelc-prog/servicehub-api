package com.petservicehub.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import com.petservicehub.dto.ReviewRequest;
import com.petservicehub.exception.BusinessException;
import com.petservicehub.model.OfferedService;
import com.petservicehub.model.RequestStatus;
import com.petservicehub.model.ServiceRequest;
import com.petservicehub.model.User;
import com.petservicehub.model.UserRole;
import com.petservicehub.repository.ReviewRepository;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

	@Mock
	private ReviewRepository reviewRepository;

	@Mock
	private ServiceRequestService serviceRequestService;

	@Mock
	private UserService userService;

	@InjectMocks
	private ReviewService reviewService;

	@Test
	void deveRecusarAvaliacaoQuandoSolicitacaoNaoEstaCompleted() {
		when(serviceRequestService.buscarEntidade(1L)).thenReturn(solicitacao(RequestStatus.PENDING, 2L));

		ReviewRequest request = new ReviewRequest(1L, 2L, 5, "Bom");

		assertThatThrownBy(() -> reviewService.criar(request))
				.isInstanceOf(BusinessException.class)
				.hasMessage("A avaliação só pode ser cadastrada para uma solicitação COMPLETED")
				.extracting(ex -> ((BusinessException) ex).getStatus())
				.isEqualTo(HttpStatus.BAD_REQUEST);
	}

	@Test
	void deveRecusarAvaliadorDiferenteDoCliente() {
		when(serviceRequestService.buscarEntidade(1L)).thenReturn(solicitacao(RequestStatus.COMPLETED, 2L));

		ReviewRequest request = new ReviewRequest(1L, 9L, 5, "Bom");

		assertThatThrownBy(() -> reviewService.criar(request))
				.isInstanceOf(BusinessException.class)
				.hasMessage("O avaliador deve ser o cliente da solicitação");
	}

	@Test
	void deveRecusarSegundaAvaliacao() {
		when(serviceRequestService.buscarEntidade(1L)).thenReturn(solicitacao(RequestStatus.COMPLETED, 2L));
		when(reviewRepository.existsByRequestId(1L)).thenReturn(true);

		ReviewRequest request = new ReviewRequest(1L, 2L, 5, "Bom");

		assertThatThrownBy(() -> reviewService.criar(request))
				.isInstanceOf(BusinessException.class)
				.hasMessage("Esta solicitação já possui uma avaliação")
				.extracting(ex -> ((BusinessException) ex).getStatus())
				.isEqualTo(HttpStatus.CONFLICT);
	}

	private ServiceRequest solicitacao(RequestStatus status, Long clientId) {
		User client = User.builder()
				.id(clientId)
				.fullName("Cliente")
				.email("cliente@email.com")
				.passwordHash("hash")
				.role(UserRole.CLIENT)
				.active(true)
				.build();
		OfferedService service = OfferedService.builder()
				.id(10L)
				.title("Serviço")
				.description("Descrição")
				.price(BigDecimal.TEN)
				.category("Geral")
				.active(true)
				.build();
		return ServiceRequest.builder()
				.id(1L)
				.service(service)
				.client(client)
				.status(status)
				.totalPrice(BigDecimal.TEN)
				.build();
	}
}
