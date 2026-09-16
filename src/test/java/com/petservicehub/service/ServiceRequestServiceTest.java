package com.petservicehub.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import com.petservicehub.dto.ServiceRequestInput;
import com.petservicehub.dto.ServiceRequestResponse;
import com.petservicehub.exception.BusinessException;
import com.petservicehub.model.OfferedService;
import com.petservicehub.model.RequestStatus;
import com.petservicehub.model.ServiceRequest;
import com.petservicehub.model.User;
import com.petservicehub.model.UserRole;
import com.petservicehub.repository.ReviewRepository;
import com.petservicehub.repository.ServiceRequestRepository;

@ExtendWith(MockitoExtension.class)
class ServiceRequestServiceTest {

	@Mock
	private ServiceRequestRepository serviceRequestRepository;

	@Mock
	private OfferedServiceService offeredServiceService;

	@Mock
	private UserService userService;

	@Mock
	private ReviewRepository reviewRepository;

	@InjectMocks
	private ServiceRequestService serviceRequestService;

	@Test
	void devePreencherTotalPriceComPrecoDoServico() {
		OfferedService service = servico(10L, new BigDecimal("150.00"), true);
		User client = usuario(2L, UserRole.CLIENT, true);
		when(offeredServiceService.buscarEntidade(10L)).thenReturn(service);
		when(userService.buscarEntidade(2L)).thenReturn(client);
		when(serviceRequestRepository.save(any(ServiceRequest.class))).thenAnswer(invocation -> {
			ServiceRequest salvo = invocation.getArgument(0);
			salvo.setId(5L);
			return salvo;
		});
		when(serviceRequestRepository.findByIdWithRelations(5L)).thenAnswer(invocation -> {
			ServiceRequest salvo = ServiceRequest.builder()
					.id(5L)
					.service(service)
					.client(client)
					.status(RequestStatus.PENDING)
					.totalPrice(service.getPrice())
					.build();
			return Optional.of(salvo);
		});

		ServiceRequestResponse response = serviceRequestService.criar(
				new ServiceRequestInput(10L, 2L, null, null, null));

		assertThat(response.totalPrice()).isEqualByComparingTo("150.00");
		assertThat(response.status()).isEqualTo(RequestStatus.PENDING);
	}

	@Test
	void deveRecusarClienteQueNaoEClient() {
		when(offeredServiceService.buscarEntidade(10L)).thenReturn(servico(10L, BigDecimal.TEN, true));
		when(userService.buscarEntidade(1L)).thenReturn(usuario(1L, UserRole.PROVIDER, true));

		ServiceRequestInput request = new ServiceRequestInput(10L, 1L, null, null, null);

		assertThatThrownBy(() -> serviceRequestService.criar(request))
				.isInstanceOf(BusinessException.class)
				.hasMessage("Somente um usuário CLIENT pode contratar um serviço")
				.extracting(ex -> ((BusinessException) ex).getStatus())
				.isEqualTo(HttpStatus.BAD_REQUEST);
	}

	private User usuario(Long id, UserRole role, boolean active) {
		return User.builder()
				.id(id)
				.fullName("Usuário")
				.email("user" + id + "@email.com")
				.passwordHash("hash")
				.role(role)
				.active(active)
				.build();
	}

	private OfferedService servico(Long id, BigDecimal preco, boolean active) {
		return OfferedService.builder()
				.id(id)
				.title("Serviço")
				.description("Descrição")
				.price(preco)
				.category("Geral")
				.provider(usuario(99L, UserRole.PROVIDER, true))
				.active(active)
				.build();
	}
}
