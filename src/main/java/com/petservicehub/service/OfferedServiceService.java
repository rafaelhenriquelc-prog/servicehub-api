package com.petservicehub.service;

import java.util.List;

import org.springframework.http.HttpStatus;
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
		User provider = prestadorAtivo(request.providerId());
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
		User provider = prestadorAtivo(request.providerId());
		service.setTitle(request.title());
		service.setDescription(request.description());
		service.setPrice(request.price());
		service.setCategory(request.category());
		service.setProvider(provider);
		if (request.active() != null) {
			service.setActive(request.active());
		}
		return EntityMapper.toResponse(offeredServiceRepository.save(service));
	}

	@Transactional
	public void excluir(Long id) {
		OfferedService service = buscarEntidade(id);
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

	private User prestadorAtivo(Long providerId) {
		User provider = userService.buscarEntidade(providerId);
		if (provider.getRole() != UserRole.PROVIDER) {
			throw new BusinessException(HttpStatus.BAD_REQUEST, "Somente um usuário PROVIDER pode ser associado como prestador");
		}
		if (!Boolean.TRUE.equals(provider.getActive())) {
			throw new BusinessException(HttpStatus.BAD_REQUEST, "O prestador informado está inativo");
		}
		return provider;
	}

	private String emptyToNull(String category) {
		return category == null || category.isBlank() ? null : category;
	}
}
