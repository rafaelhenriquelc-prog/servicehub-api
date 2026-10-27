package com.petservicehub.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.petservicehub.dto.EntityMapper;
import com.petservicehub.dto.OfferedServiceRequest;
import com.petservicehub.dto.OfferedServiceResponse;
import com.petservicehub.exception.BusinessException;
import com.petservicehub.exception.ResourceNotFoundException;
import com.petservicehub.model.OfferedService;
import com.petservicehub.model.User;
import com.petservicehub.model.UserRole;
import com.petservicehub.repository.OfferedServiceRepository;
import com.petservicehub.repository.ServiceRequestRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OfferedServiceService {

	private final OfferedServiceRepository offeredServiceRepository;
	private final UserService userService;
	private final ServiceRequestRepository serviceRequestRepository;

	@Transactional
	public OfferedServiceResponse criar(OfferedServiceRequest request) {
		User provider = userService.usuarioAutenticado();
		if (provider.getRole() != UserRole.PROVIDER) {
			throw new AccessDeniedException("Somente um PROVIDER pode criar serviços");
		}
		if (!Boolean.TRUE.equals(provider.getActive())) {
			throw new BusinessException(HttpStatus.BAD_REQUEST, "O prestador autenticado está inativo");
		}
		OfferedService service = OfferedService.builder()
				.title(request.title())
				.description(request.description())
				.price(request.price())
				.category(request.category())
				.provider(provider)
				.active(request.active() != null ? request.active() : true)
				.build();
		return EntityMapper.toResponse(offeredServiceRepository.save(service));
	}

	@Transactional(readOnly = true)
	public List<OfferedServiceResponse> listar(Boolean active, Long providerId, String category) {
		return offeredServiceRepository.findAllWithProvider(active, providerId, emptyToNull(category))
				.stream()
				.map(EntityMapper::toResponse)
				.toList();
	}

	@Transactional(readOnly = true)
	public OfferedServiceResponse buscarPorId(Long id) {
		return EntityMapper.toResponse(buscarEntidade(id));
	}

	@Transactional
	public OfferedServiceResponse atualizar(Long id, OfferedServiceRequest request) {
		OfferedService service = buscarEntidade(id);
		garantirDonoOuAdmin(service);
		service.setTitle(request.title());
		service.setDescription(request.description());
		service.setPrice(request.price());
		service.setCategory(request.category());
		if (request.active() != null) {
			service.setActive(request.active());
		}
		return EntityMapper.toResponse(offeredServiceRepository.save(service));
	}

	@Transactional
	public void excluir(Long id) {
		OfferedService service = buscarEntidade(id);
		garantirDonoOuAdmin(service);
		if (serviceRequestRepository.existsByServiceId(id)) {
			throw new BusinessException(HttpStatus.CONFLICT, "Não é possível excluir o serviço porque existem solicitações vinculadas");
		}
		offeredServiceRepository.delete(service);
	}

	@Transactional(readOnly = true)
	public OfferedService buscarEntidade(Long id) {
		return offeredServiceRepository.findByIdWithProvider(id)
				.orElseThrow(() -> new ResourceNotFoundException("Serviço não encontrado com id " + id));
	}

	private void garantirDonoOuAdmin(OfferedService service) {
		User current = userService.usuarioAutenticado();
		if (current.getRole() == UserRole.ADMIN) {
			return;
		}
		if (current.getRole() != UserRole.PROVIDER || !service.getProvider().getId().equals(current.getId())) {
			throw new AccessDeniedException("Você não pode alterar o serviço de outro prestador");
		}
	}

	private String emptyToNull(String category) {
		return category == null || category.isBlank() ? null : category;
	}
}
