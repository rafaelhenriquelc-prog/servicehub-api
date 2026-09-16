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

import com.petservicehub.dto.OfferedServiceRequest;
import com.petservicehub.dto.OfferedServiceResponse;
import com.petservicehub.exception.BusinessException;
import com.petservicehub.model.OfferedService;
import com.petservicehub.model.User;
import com.petservicehub.model.UserRole;
import com.petservicehub.repository.OfferedServiceRepository;
import com.petservicehub.repository.ServiceRequestRepository;

@ExtendWith(MockitoExtension.class)
class OfferedServiceServiceTest {

	@Mock
	private OfferedServiceRepository offeredServiceRepository;

	@Mock
	private UserService userService;

	@Mock
	private ServiceRequestRepository serviceRequestRepository;

	@InjectMocks
	private OfferedServiceService offeredServiceService;

	@Test
	void deveRecusarPrestadorQueNaoEProvider() {
		when(userService.buscarEntidade(1L)).thenReturn(usuario(1L, UserRole.CLIENT, true));

		OfferedServiceRequest request = new OfferedServiceRequest(
				"Montagem", "Descrição", new BigDecimal("100.00"), "Marcenaria", 1L, true);

		assertThatThrownBy(() -> offeredServiceService.criar(request))
				.isInstanceOf(BusinessException.class)
				.hasMessage("Somente um usuário PROVIDER pode ser associado como prestador")
				.extracting(ex -> ((BusinessException) ex).getStatus())
				.isEqualTo(HttpStatus.BAD_REQUEST);
	}

	@Test
	void deveCadastrarServicoParaProviderAtivo() {
		when(userService.buscarEntidade(2L)).thenReturn(usuario(2L, UserRole.PROVIDER, true));
		when(offeredServiceRepository.save(any(OfferedService.class))).thenAnswer(invocation -> {
			OfferedService salvo = invocation.getArgument(0);
			salvo.setId(10L);
			return salvo;
		});

		OfferedServiceResponse response = offeredServiceService.criar(new OfferedServiceRequest(
				"Montagem", "Descrição", new BigDecimal("100.00"), "Marcenaria", 2L, null));

		assertThat(response.id()).isEqualTo(10L);
		assertThat(response.active()).isTrue();
		assertThat(response.provider().role()).isEqualTo(UserRole.PROVIDER);
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
}
